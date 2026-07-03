package realization.runtime.cache

import governance.budget.contract.CostCenter
import realization.identity.interning.PlanCacheKey
import realization.planning.session.PlannerSession
import realization.runtime.dispatch.L2JoinDispatchPlane
import realization.runtime.time.MonotonicTimeSource
import stage.lowering.boundary.BuildHandle
import stage.lowering.boundary.JoinContinuation
import stage.lowering.boundary.JoinHandle
import stage.lowering.boundary.JoinRegistrationDecision
import stage.lowering.boundary.JoinResumeStep
import stage.lowering.boundary.PlanInternStep
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import stage.lowering.material.CanonicalPlanNode
import statemachine.transition.diagnostics.L2FaultKind
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicLong

/**
 * Contention-reduction shard inside one partition region.
 *
 * Architectural role:
 * - deterministic shard-local routing across primitive tables
 * - exact-hit pre-screening
 * - in-flight slot acquisition
 * - builder vs join branching
 * - governance reaction mapping
 * - authoritative success-path bucket re-verification
 *
 * It does NOT own:
 * - shared-slot lifecycle truth
 * - waiter lifecycle truth
 * - builder-handle lifecycle truth
 * - region lifecycle truth
 * - callback execution policy
 *
 * Those remain with:
 * - InFlightSlot
 * - WaiterCell
 * - BuilderHandleCell
 * - PartitionRegion
 * - L2JoinDispatchPlane
 *
 * Important current boundary:
 * - abortAllInFlight() guarantees visible shared-slot terminalization
 * - full primitive-table slot reclamation still depends on LongKeyTable capabilities
 */
internal class L2Shard private constructor(
    private val owner: PartitionRegion,
    private val shardIndex: Int,
    bucketTableCapacity: Int,
    inflightTableCapacity: Int,
    private val maxWaitersPerKey: Int,
    private val joinWaitTimeoutNanos: Long,
    private val dispatchPlane: L2JoinDispatchPlane,
    private val timeSource: MonotonicTimeSource,
) {
    private val buckets = LongKeyTable.issue<L2Bucket>(capacity = bucketTableCapacity)
    private val inflight = LongKeyTable.issue<InFlightSlot<CanonicalPlanNode>>(capacity = inflightTableCapacity)

    /**
     * Monotonic shard-local lifecycle episode source.
     */
    private val episodeGenerationSeq = AtomicLong(0L)

    fun getOrIntern(
        key: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternStep {
        try {
            session.step(CostCenter.L2_PRE_SCREEN_GET)

            val bucket = buckets.get(routeKeyBits)
            if (bucket != null) {
                session.step(CostCenter.L2_BUCKET_SCAN)
                val exact = bucket.findExact(key)
                if (exact != null) {
                    session.step(CostCenter.L2_HIT)
                    return PlanInternStep.hit(exact)
                }
            }

            session.step(CostCenter.L2_INFLIGHT_ACQUIRE)

            val freshSlot =
                InFlightSlot.issue<CanonicalPlanNode>(
                    maxAttachedWaiters = maxWaitersPerKey,
                    startedAtNanos = timeSource.nowNanos().coerceAtLeast(0L),
                    generation = nextEpisodeGeneration(),
                )

            val existing = inflight.putIfAbsent(routeKeyBits, freshSlot)

            return if (existing == null) {
                enterBuilderPath(
                    key = key,
                    routeKeyBits = routeKeyBits,
                    slot = freshSlot,
                    session = session,
                )
            } else {
                enterJoinPath(
                    key = key,
                    routeKeyBits = routeKeyBits,
                    slot = existing,
                    session = session,
                )
            }
        } catch (_: L2TableSegmentSaturatedException) {
            owner.forceCircuitOpen(session)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }
    }

    /**
     * Administrative partition-drop sweep.
     *
     * This method only performs shard-lawful work:
     * - terminalize visible shared slots through the slot lifecycle host
     * - enqueue terminal sweeps to the adapter-owned dispatch plane
     *
     * It intentionally does NOT pretend that primitive routing-table entry reuse is
     * already solved if the underlying LongKeyTable has not yet provided a dedicated
     * key-aware partition-drop removal sweep.
     */
    fun abortAllInFlight() {
        inflight.forEachOccupiedValueForClosedPartitionDrop { slot ->
            if (slot.tryDropShared(CancellationException("Partition dropped."))) {
                dispatchPlane.enqueueTerminalSweep(
                    shardIndex = shardIndex,
                    slot = slot,
                )
            }
        }
    }

    // =========================================================================
    // Builder path
    // =========================================================================

    private fun enterBuilderPath(
        key: PlanCacheKey,
        routeKeyBits: Long,
        slot: InFlightSlot<CanonicalPlanNode>,
        session: PlannerSession,
    ): PlanInternStep {
        val builderHandle =
            BuilderHandleCell.issue(
                supervisoryDeadlineNanos =
                    computeBuilderSupervisoryDeadlineNanos(
                        nowNanos = timeSource.nowNanos().coerceAtLeast(0L),
                    ),
                generation = nextEpisodeGeneration(),
            )

        when (slot.registerBuilderHandle(builderHandle)) {
            is BuilderHandleRegisterDecision.RejectedSlotTerminal -> {
                builderHandle.tryAbort()
                inflight.removeIfSame(routeKeyBits, slot)

                return consumeTerminalResolution(
                    targetKey = key,
                    routeKeyBits = routeKeyBits,
                    resolution =
                        slot.resolveSharedTerminalAcquire()
                            ?: throw PlanningProtocolIntegrityException(
                                "Builder registration was rejected without visible shared terminal truth.",
                            ),
                    session = session,
                )
            }

            BuilderHandleRegisterDecision.RegisteredPending -> Unit
        }

        if (!owner.allowBuilderAfterAcquire(session)) {
            builderHandle.tryAbort()

            if (slot.tryDropShared(CancellationException("Builder admission rejected after region close/circuit-open."))) {
                dispatchPlane.enqueueTerminalSweep(
                    shardIndex = shardIndex,
                    slot = slot,
                )
            }

            inflight.removeIfSame(routeKeyBits, slot)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }

        if (!slot.tryClaimCommitRight()) {
            builderHandle.tryAbort()
            return PlanInternStep.fault(L2FaultKind.TRANSIENT)
        }

        return PlanInternStep.build(
            ShardBuildHandle(
                key = key,
                routeKeyBits = routeKeyBits,
                slot = slot,
                builderHandle = builderHandle,
            ),
        )
    }

    // =========================================================================
    // Join path
    // =========================================================================

    private fun enterJoinPath(
        key: PlanCacheKey,
        routeKeyBits: Long,
        slot: InFlightSlot<CanonicalPlanNode>,
        session: PlannerSession,
    ): PlanInternStep {
        val waiter =
            WaiterCell.issue(
                generation = nextEpisodeGeneration(),
            )

        session.step(CostCenter.L2_INFLIGHT_ATTACH)

        return when (val decision = slot.tryAttachWaiter(waiter)) {
            JoinAdmissionDecision.AttachedPending -> {
                PlanInternStep.join(
                    ShardJoinHandle(
                        targetKey = key,
                        routeKeyBits = routeKeyBits,
                        slot = slot,
                        waiter = waiter,
                        deadlineNanos =
                            computeJoinDeadlineNanos(
                                slotStartedAtNanos = slot.readStartedAtNanos(),
                            ),
                    ),
                )
            }

            JoinAdmissionDecision.WaiterCapExceeded -> {
                if (owner.isBypassRequired()) {
                    PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
                } else {
                    PlanInternStep.fault(L2FaultKind.TRANSIENT)
                }
            }

            is JoinAdmissionDecision.AlreadyTerminal -> {
                consumeTerminalResolution(
                    targetKey = key,
                    routeKeyBits = routeKeyBits,
                    resolution = decision.resolution,
                    session = session,
                )
            }
        }
    }

    // =========================================================================
    // Terminal-resolution consumption
    // =========================================================================

    private fun consumeTerminalResolution(
        targetKey: PlanCacheKey,
        routeKeyBits: Long,
        resolution: SharedTerminalResolution,
        session: PlannerSession,
    ): PlanInternStep =
        when (resolution) {
            SharedTerminalResolution.SuccessRequiresBucketReverification -> {
                when (val verified = verifyBucketWinnerOrFault(targetKey, routeKeyBits, session)) {
                    is BucketVerificationResult.Hit -> PlanInternStep.hit(verified.node)
                    is BucketVerificationResult.Fault -> PlanInternStep.fault(verified.kind)
                }
            }

            SharedTerminalResolution.SharedFailureTerminal -> {
                PlanInternStep.fault(L2FaultKind.TRANSIENT)
            }

            SharedTerminalResolution.SharedDropTerminal -> {
                PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
            }
        }

    /**
     * Single authoritative helper for success-path bucket re-verification.
     *
     * Request-scope path and resumed path both converge here.
     * Cost accounting belongs to whichever session is actually performing the work.
     */
    private fun verifyBucketWinnerOrFault(
        targetKey: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): BucketVerificationResult {
        session.step(CostCenter.L2_BUCKET_SCAN)

        val bucket = buckets.get(routeKeyBits)
        val exact = bucket?.findExact(targetKey)

        return if (exact != null) {
            session.step(CostCenter.L2_HIT)
            BucketVerificationResult.Hit(exact)
        } else if (owner.isBypassRequired()) {
            BucketVerificationResult.Fault(L2FaultKind.CIRCUIT_OPEN)
        } else {
            BucketVerificationResult.Fault(L2FaultKind.TRANSIENT)
        }
    }

    private fun nextEpisodeGeneration(): Long {
        val next = episodeGenerationSeq.incrementAndGet()
        if (next <= 0L) {
            throw PlanningProtocolIntegrityException(
                "L2Shard episode generation overflowed the positive Long domain.",
            )
        }
        return next
    }

    private fun computeBuilderSupervisoryDeadlineNanos(nowNanos: Long): Long {
        if (nowNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "Builder supervisory deadline base nanos must be >= 0: $nowNanos",
            )
        }

        return try {
            Math.addExact(nowNanos, joinWaitTimeoutNanos)
        } catch (_: ArithmeticException) {
            throw PlanningProtocolIntegrityException(
                "Builder supervisory deadline overflow: base=$nowNanos delta=$joinWaitTimeoutNanos",
            )
        }
    }

    private fun computeJoinDeadlineNanos(slotStartedAtNanos: Long): Long {
        if (slotStartedAtNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "Join deadline base nanos must be >= 0: $slotStartedAtNanos",
            )
        }

        return try {
            Math.addExact(slotStartedAtNanos, joinWaitTimeoutNanos)
        } catch (_: ArithmeticException) {
            throw PlanningProtocolIntegrityException(
                "Join deadline overflow: base=$slotStartedAtNanos delta=$joinWaitTimeoutNanos",
            )
        }
    }

    // =========================================================================
    // Build handle
    // =========================================================================

    private inner class ShardBuildHandle(
        private val key: PlanCacheKey,
        private val routeKeyBits: Long,
        private val slot: InFlightSlot<CanonicalPlanNode>,
        private val builderHandle: BuilderHandleCell,
    ) : BuildHandle {
        override fun commit(
            localNode: CanonicalPlanNode,
            session: PlannerSession,
        ): CanonicalPlanNode {
            try {
                when (slot.decidePublicationEntryAcquire()) {
                    PublicationEntryDecision.Allowed -> Unit

                    PublicationEntryDecision.CommitRightNotClaimed -> {
                        builderHandle.tryAbort()
                        throw PlanningProtocolIntegrityException(
                            "Publication entry requires CLAIMED commit-right.",
                        )
                    }

                    PublicationEntryDecision.SlotNotPending -> {
                        builderHandle.tryAbort()
                        return localNode
                    }
                }

                if (!owner.isPublishAllowed()) {
                    builderHandle.tryAbort()

                    if (slot.tryDropShared(CancellationException("Publication rejected after region close/circuit-open."))) {
                        dispatchPlane.enqueueTerminalSweep(
                            shardIndex = shardIndex,
                            slot = slot,
                        )
                    }

                    return localNode
                }

                session.step(CostCenter.L2_PUBLISH_PUT_IF_ABSENT)

                val freshBucket =
                    L2Bucket(
                        initialKey = key,
                        initialNode = localNode,
                    )

                val existingBucket = buckets.putIfAbsent(routeKeyBits, freshBucket)

                val winner =
                    if (existingBucket == null) {
                        owner.onEntryCommitted(session)
                        localNode
                    } else {
                        session.step(CostCenter.L2_BUCKET_SCAN)

                        val put = existingBucket.putIfAbsentOrGet(key, localNode)

                        if (put.inserted) {
                            owner.onEntryCommitted(session)
                        } else {
                            session.step(CostCenter.L2_HIT)
                        }

                        put.winner
                    }

                if (slot.tryPublishSuccess(winner)) {
                    dispatchPlane.enqueueTerminalSweep(
                        shardIndex = shardIndex,
                        slot = slot,
                    )
                }

                builderHandle.tryCommit()
                slot.tryReleaseCommitRight()
                return winner
            } catch (_: L2TableSegmentSaturatedException) {
                owner.forceCircuitOpen(session)
                builderHandle.tryAbort()

                if (slot.tryDropShared(CancellationException("Primitive table saturated during publication."))) {
                    dispatchPlane.enqueueTerminalSweep(
                        shardIndex = shardIndex,
                        slot = slot,
                    )
                }

                return localNode
            } catch (t: Throwable) {
                builderHandle.tryAbort()

                if (slot.tryFailShared(t)) {
                    dispatchPlane.enqueueTerminalSweep(
                        shardIndex = shardIndex,
                        slot = slot,
                    )
                }

                throw t
            } finally {
                inflight.removeIfSame(routeKeyBits, slot)
            }
        }

        override fun abort(reason: Throwable) {
            try {
                builderHandle.tryAbort()
                if (slot.tryFailShared(reason)) {
                    dispatchPlane.enqueueTerminalSweep(
                        shardIndex = shardIndex,
                        slot = slot,
                    )
                }
            } finally {
                inflight.removeIfSame(routeKeyBits, slot)
            }
        }
    }

    // =========================================================================
    // Join handle
    // =========================================================================

    private inner class ShardJoinHandle(
        private val targetKey: PlanCacheKey,
        private val routeKeyBits: Long,
        private val slot: InFlightSlot<CanonicalPlanNode>,
        private val waiter: WaiterCell,
        private val deadlineNanos: Long,
    ) : JoinHandle {
        override fun registerContinuation(continuation: JoinContinuation): JoinRegistrationDecision =
            dispatchPlane.registerOrDeliverImmediate(
                shardIndex = shardIndex,
                slot = slot,
                waiter = waiter,
                continuation = continuation,
                deadlineNanos = deadlineNanos,
            )

        override fun consumeReadyResult(session: PlannerSession): JoinResumeStep {
            val resolution =
                slot.resolveSharedTerminalAcquire()
                    ?: throw PlanningProtocolIntegrityException(
                        "consumeReadyResult(session) requires visible shared terminal truth.",
                    )

            return when (resolution) {
                SharedTerminalResolution.SuccessRequiresBucketReverification -> {
                    when (val verified = verifyBucketWinnerOrFault(targetKey, routeKeyBits, session)) {
                        is BucketVerificationResult.Hit -> JoinResumeStep.hit(verified.node)
                        is BucketVerificationResult.Fault -> JoinResumeStep.fault(verified.kind)
                    }
                }

                SharedTerminalResolution.SharedFailureTerminal -> {
                    JoinResumeStep.fault(L2FaultKind.TRANSIENT)
                }

                SharedTerminalResolution.SharedDropTerminal -> {
                    JoinResumeStep.fault(L2FaultKind.CIRCUIT_OPEN)
                }
            }
        }

        override fun cancel(reason: Throwable): Boolean {
            val accepted = waiter.tryCancel(reason)
            if (accepted) {
                /*
                 * Cancellation truth is waiter-owned.
                 * Dispatch cleanup is best-effort and adapter-owned.
                 */
                dispatchPlane.enqueueCancellation(
                    shardIndex = shardIndex,
                    waiter = waiter,
                )
            }
            return accepted
        }

        override fun deadlineNanos(): Long = deadlineNanos
    }

    private sealed interface BucketVerificationResult {
        class Hit(
            val node: CanonicalPlanNode,
        ) : BucketVerificationResult

        class Fault(
            val kind: L2FaultKind,
        ) : BucketVerificationResult
    }

    companion object {
        @JvmStatic
        internal fun issue(
            owner: PartitionRegion,
            shardIndex: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxWaitersPerKey: Int,
            joinWaitTimeoutNanos: Long,
            dispatchPlane: L2JoinDispatchPlane,
            timeSource: MonotonicTimeSource,
        ): L2Shard {
            validate(
                shardIndex = shardIndex,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxWaitersPerKey = maxWaitersPerKey,
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
            )

            return L2Shard(
                owner = owner,
                shardIndex = shardIndex,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxWaitersPerKey = maxWaitersPerKey,
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
                dispatchPlane = dispatchPlane,
                timeSource = timeSource,
            )
        }

        private fun validate(
            shardIndex: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxWaitersPerKey: Int,
            joinWaitTimeoutNanos: Long,
        ) {
            if (shardIndex < 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.shardIndex must be >= 0: $shardIndex",
                )
            }
            if (bucketTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.bucketTableCapacity must be positive: $bucketTableCapacity",
                )
            }
            if (inflightTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.inflightTableCapacity must be positive: $inflightTableCapacity",
                )
            }
            if (maxWaitersPerKey <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.maxWaitersPerKey must be positive: $maxWaitersPerKey",
                )
            }
            if (joinWaitTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos",
                )
            }
        }
    }
}

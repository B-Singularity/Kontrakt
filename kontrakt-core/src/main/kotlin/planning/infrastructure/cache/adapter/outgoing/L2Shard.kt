package planning.infrastructure.cache.adapter.outgoing

import ir.plan.node.CanonicalPlanNode
import ir.plan.signature.PlanCacheKey
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.BuildHandle
import planning.domain.port.outgoing.JoinContinuation
import planning.domain.port.outgoing.JoinHandle
import planning.domain.port.outgoing.JoinRegistrationDecision
import planning.domain.port.outgoing.JoinResumeStep
import planning.domain.port.outgoing.PlanInternStep
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.infrastructure.cache.BuilderHandleCell
import planning.infrastructure.cache.BuilderHandleRegisterDecision
import planning.infrastructure.cache.InFlightSlot
import planning.infrastructure.cache.JoinAdmissionDecision
import planning.infrastructure.cache.L2TableSegmentSaturatedException
import planning.infrastructure.cache.LongKeyTable
import planning.infrastructure.cache.PublicationEntryDecision
import planning.infrastructure.cache.SharedTerminalResolution
import planning.infrastructure.cache.WaiterCell
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicLong

/**
 * Contention-reduction strip inside one partition region.
 *
 * -----------------------------------------------------------------------------
 * ARCHITECTURAL ROLE
 * -----------------------------------------------------------------------------
 *
 * This class is orchestration-only.
 *
 * It owns:
 * - deterministic hot-path routing across shard-local primitive tables
 * - exact-match pre-screening
 * - in-flight slot acquisition
 * - builder vs join branching
 * - governance reaction mapping
 * - authoritative bucket re-verification
 *
 * It does NOT own:
 * - shared-slot lifecycle truth
 * - waiter lifecycle truth
 * - builder-handle lifecycle truth
 * - partition-region lifecycle truth
 *
 * Those remain owned by:
 * - InFlightSlot
 * - WaiterCell
 * - BuilderHandleCell
 * - PartitionRegion
 * - L2LifecycleLaw
 *
 * -----------------------------------------------------------------------------
 * NON-BLOCKING JOIN RULE
 * -----------------------------------------------------------------------------
 *
 * Join never blocks here.
 *
 * If attach succeeds and the slot remains pending:
 * - return PlanInternStep.Join(handle) immediately
 * - let adapter-owned dispatch infrastructure perform delivery later
 * - let the runtime boundary resume through a fresh PlannerSession
 *
 * -----------------------------------------------------------------------------
 * GENERATION SOURCE
 * -----------------------------------------------------------------------------
 *
 * This shard owns a local monotonic episode-generation source used for:
 * - waiter episodes
 * - builder-handle episodes
 *
 * The generation is:
 * - monotonic within the shard instance
 * - opaque
 * - not wall-clock based
 * - suitable for stale-reference defense and future reclamation hardening
 *
 * -----------------------------------------------------------------------------
 * CURRENT HARDENING BOUNDARY
 * -----------------------------------------------------------------------------
 *
 * This cut does NOT yet perform generation-based consume-time validation for
 * resumed joins.
 *
 * That omission is intentional for now because current lifecycle hosts still
 * assume:
 * - no immediate pooling
 * - no reuse before grace completion
 *
 * A generation validity gate becomes meaningful only once true reuse/reclamation
 * machinery is introduced.
 */
class L2Shard private constructor(
    private val owner: PartitionRegion,
    bucketTableCapacity: Int,
    inflightTableCapacity: Int,
    private val maxWaitersPerKey: Int,
    private val joinWaitTimeoutNanos: Long,
    private val dispatchPlane: L2JoinDispatchPlane,
) {
    private val buckets = LongKeyTable.issue<L2Bucket>(capacity = bucketTableCapacity)
    private val inflight = LongKeyTable.issue<InFlightSlot<CanonicalPlanNode>>(capacity = inflightTableCapacity)

    /**
     * Shard-local monotonic episode generation source.
     */
    private val episodeGenerationSeq = AtomicLong(0L)

    /**
     * Resolves the next lawful interning step for this shard.
     */
    fun getOrIntern(
        key: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternStep {
        try {
            // -----------------------------------------------------------------
            // 1. Pre-screen exact hit
            // -----------------------------------------------------------------
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

            // -----------------------------------------------------------------
            // 2. In-flight acquire
            // -----------------------------------------------------------------
            session.step(CostCenter.L2_INFLIGHT_ACQUIRE)

            val freshSlot = InFlightSlot.issue<CanonicalPlanNode>(
                maxAttachedWaiters = maxWaitersPerKey,
                startedAtNanos = System.nanoTime().coerceAtLeast(0L),
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
        } catch (e: L2TableSegmentSaturatedException) {
            owner.forceCircuitOpen(session)
            session.step(CostCenter.L2_CIRCUIT_OPEN_TRANSITION)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }
    }

    /**
     * Administrative partition-drop sweep.
     *
     * Partition lifecycle ownership remains with PartitionRegion.
     * This method only terminalizes visible shared-slot hosts and delegates
     * joined-waiter delivery to the adapter-owned dispatch plane.
     */
    fun abortAllInFlight() {
        inflight.forEachOccupiedValueForClosedPartitionDrop { slot ->
            if (slot.tryDropShared(CancellationException("Partition dropped."))) {
                dispatchPlane.enqueueTerminalSweep(slot)
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
        val builderHandle = BuilderHandleCell.issue(
            supervisoryDeadlineNanos = computeBuilderSupervisoryDeadlineNanos(
                nowNanos = System.nanoTime().coerceAtLeast(0L),
            ),
            generation = nextEpisodeGeneration(),
        )

        when (slot.registerBuilderHandle(builderHandle)) {
            is BuilderHandleRegisterDecision.RejectedSlotTerminal -> {
                /*
                 * The handle was never durably registered into the slot-local builder
                 * registry, so supervision scans cannot discover it later.
                 *
                 * Therefore we must converge it immediately here rather than leaving an
                 * orphaned OPEN builder handle behind.
                 */
                builderHandle.tryAbort()
                inflight.removeIfSame(routeKeyBits, slot)
                return consumeTerminalResolution(
                    targetKey = key,
                    routeKeyBits = routeKeyBits,
                    resolution = slot.resolveSharedTerminalAcquire()
                        ?: throw PlanningProtocolIntegrityException(
                            "Builder registration rejected without visible terminal resolution."
                        ),
                    session = session,
                )
            }

            BuilderHandleRegisterDecision.RegisteredPending -> Unit
        }

        if (!owner.allowBuilderAfterAcquire(session)) {
            builderHandle.tryAbort()
            if (slot.tryDropShared(CancellationException("Builder admission rejected after governance close/open."))) {
                dispatchPlane.enqueueTerminalSweep(slot)
            }
            inflight.removeIfSame(routeKeyBits, slot)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }

        if (!slot.tryClaimCommitRight()) {
            /*
             * Commit-right claim failure does NOT imply that this slot should be
             * removed from the in-flight routing table.
             *
             * Another contender may already own or still converge the publication
             * episode for this very slot. Removing the slot here would make later
             * joiners unable to discover the still-live in-flight coordination host.
             *
             * Therefore:
             * - abort only this builder-handle episode
             * - leave the slot discoverable in inflight
             * - surface a degrade signal to the caller
             */
            builderHandle.tryAbort()
            return PlanInternStep.fault(L2FaultKind.TRANSIENT)
        }

        return PlanInternStep.build(
            ShardBuildHandle(
                key = key,
                routeKeyBits = routeKeyBits,
                slot = slot,
                builderHandle = builderHandle,
            )
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
        val waiter = WaiterCell.issue(
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
                        deadlineNanos = computeJoinDeadlineNanos(
                            slotStartedAtNanos = slot.readStartedAtNanos(),
                        ),
                        dispatchPlane = dispatchPlane,
                    )
                )
            }

            JoinAdmissionDecision.WaiterCapExceeded -> {
                /*
                 * Governance reaction belongs here.
                 * Waiter-cap exhaustion is not itself a shared terminal taxonomy.
                 */
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
    ): PlanInternStep {
        return when (resolution) {
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
    }

    /**
     * Single authoritative helper for success-path bucket re-verification.
     *
     * Important:
     * - immediate path uses the currently active request-scope session
     * - resumed path uses a fresh restart session
     *
     * This is intentional. Cost accounting belongs to the session that is
     * actually performing the verification work.
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

    /**
     * Returns the next shard-local lifecycle episode generation.
     *
     * Fail-closed on overflow rather than silently wrapping into a reused space.
     */
    private fun nextEpisodeGeneration(): Long {
        val next = episodeGenerationSeq.incrementAndGet()
        if (next <= 0L) {
            throw PlanningProtocolIntegrityException(
                "L2Shard episode generation overflowed the positive Long domain."
            )
        }
        return next
    }

    /**
     * Computes the supervisory deadline for a builder-handle episode.
     *
     * The builder supervisory deadline is semantically distinct from the joined-waiter
     * deadline even though both currently use the same joinWaitTimeoutNanos policy input.
     *
     * We intentionally keep this as a dedicated helper rather than a generic
     * string-labeled addition utility so that:
     * - the meaning is fixed by the function name,
     * - diagnostics remain closed and typo-safe,
     * - call sites do not carry arbitrary label strings.
     */
    private fun computeBuilderSupervisoryDeadlineNanos(
        nowNanos: Long,
    ): Long {
        if (nowNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "Builder supervisory deadline base nanos must be >= 0: $nowNanos"
            )
        }
        if (joinWaitTimeoutNanos <= 0L) {
            throw PlanningProtocolIntegrityException(
                "L2Shard.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos"
            )
        }

        return try {
            Math.addExact(nowNanos, joinWaitTimeoutNanos)
        } catch (_: ArithmeticException) {
            throw PlanningProtocolIntegrityException(
                "Builder supervisory deadline overflow: base=$nowNanos delta=$joinWaitTimeoutNanos"
            )
        }
    }

    /**
     * Computes the joined-waiter deadline for one slot episode.
     *
     * This helper is intentionally separate from builder supervisory deadline
     * computation because the two deadlines belong to different lifecycle meanings:
     *
     * - builder supervisory deadline -> builder-handle convergence authority
     * - join deadline                -> waiter/orchestration restart timing
     *
     * Keeping them separate avoids diagnostic drift and removes the need for
     * call-site string labels.
     */
    private fun computeJoinDeadlineNanos(
        slotStartedAtNanos: Long,
    ): Long {
        if (slotStartedAtNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "Join deadline base nanos must be >= 0: $slotStartedAtNanos"
            )
        }
        if (joinWaitTimeoutNanos <= 0L) {
            throw PlanningProtocolIntegrityException(
                "L2Shard.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos"
            )
        }

        return try {
            Math.addExact(slotStartedAtNanos, joinWaitTimeoutNanos)
        } catch (_: ArithmeticException) {
            throw PlanningProtocolIntegrityException(
                "Join deadline overflow: base=$slotStartedAtNanos delta=$joinWaitTimeoutNanos"
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

        /**
         * Request-scope rule:
         *
         * This build handle must be committed or aborted within the same request
         * scope as the interning step that issued it.
         *
         * The handle intentionally does not retain PlannerSession.
         * The live request-scope session is supplied explicitly at commit time.
         */
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
                            "Publication entry requires CLAIMED commit-right."
                        )
                    }

                    PublicationEntryDecision.SlotNotPending -> {
                        builderHandle.tryAbort()
                        return localNode
                    }
                }

                if (!owner.isPublishAllowed()) {
                    builderHandle.tryAbort()
                    if (slot.tryDropShared(CancellationException("Publication rejected after governance close/open."))) {
                        dispatchPlane.enqueueTerminalSweep(slot)
                    }
                    return localNode
                }

                session.step(CostCenter.L2_PUBLISH_PUT_IF_ABSENT)

                val freshBucket = L2Bucket(
                    initialKey = key,
                    initialNode = localNode,
                )

                val existingBucket = buckets.putIfAbsent(routeKeyBits, freshBucket)

                val winner = if (existingBucket == null) {
                    owner.onEntryCommitted(session)
                    localNode
                } else {
                    val put = existingBucket.putIfAbsentOrGet(key, localNode)
                    if (put.inserted) {
                        owner.onEntryCommitted(session)
                    }
                    put.winner
                }

                if (slot.tryPublishSuccess(winner)) {
                    dispatchPlane.enqueueTerminalSweep(slot)
                }

                builderHandle.tryCommit()
                slot.tryReleaseCommitRight()
                return winner
            } catch (e: L2TableSegmentSaturatedException) {
                owner.forceCircuitOpen(session)
                builderHandle.tryAbort()
                if (slot.tryDropShared(CancellationException("Primitive routing table saturated during publication."))) {
                    dispatchPlane.enqueueTerminalSweep(slot)
                }
                return localNode
            } catch (t: Throwable) {
                builderHandle.tryAbort()
                if (slot.tryFailShared(t)) {
                    dispatchPlane.enqueueTerminalSweep(slot)
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
                    dispatchPlane.enqueueTerminalSweep(slot)
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
        private val dispatchPlane: L2JoinDispatchPlane,
    ) : JoinHandle {

        /**
         * Delegates both "already terminal" and "future delivery" cases to the
         * adapter-owned dispatch plane.
         *
         * The shard never invokes the continuation directly.
         */
        override fun registerContinuation(
            continuation: JoinContinuation,
        ): JoinRegistrationDecision {
            return dispatchPlane.registerOrDeliverImmediate(
                slot = slot,
                waiter = waiter,
                continuation = continuation,
            )
        }

        /**
         * Consumes the ready join result through a fresh restart session.
         *
         * The supplied session is the session that pays:
         * - resumed bucket-scan accounting
         * - resumed hit accounting
         * - resumed fault-degrade accounting
         *
         * No stale pre-suspension session is reused here.
         */
        override fun consumeReadyResult(
            session: PlannerSession,
        ): JoinResumeStep {
            val resolution = slot.resolveSharedTerminalAcquire()
                ?: throw PlanningProtocolIntegrityException(
                    "consumeReadyResult(session) requires visible shared terminal truth."
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
            return waiter.tryCancel(reason)
        }

        override fun deadlineNanos(): Long = deadlineNanos
    }

    /**
     * Internal canonical result for success-path bucket re-verification.
     */
    private sealed interface BucketVerificationResult {
        class Hit(val node: CanonicalPlanNode) : BucketVerificationResult
        class Fault(val kind: L2FaultKind) : BucketVerificationResult
    }

    companion object {

        @JvmStatic
        internal fun issue(
            owner: PartitionRegion,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxWaitersPerKey: Int,
            joinWaitTimeoutNanos: Long,
            dispatchPlane: L2JoinDispatchPlane,
        ): L2Shard {
            validate(
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxWaitersPerKey = maxWaitersPerKey,
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
            )

            return L2Shard(
                owner = owner,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxWaitersPerKey = maxWaitersPerKey,
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
                dispatchPlane = dispatchPlane,
            )
        }

        private fun validate(
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxWaitersPerKey: Int,
            joinWaitTimeoutNanos: Long,
        ) {
            if (bucketTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.bucketTableCapacity must be positive: $bucketTableCapacity"
                )
            }
            if (inflightTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.inflightTableCapacity must be positive: $inflightTableCapacity"
                )
            }
            if (maxWaitersPerKey <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.maxWaitersPerKey must be positive: $maxWaitersPerKey"
                )
            }
            if (joinWaitTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos"
                )
            }
        }
    }
}

/**
 * Adapter-owned completion dispatch plane for non-blocking joined-waiter delivery.
 *
 * -----------------------------------------------------------------------------
 * OWNERSHIP
 * -----------------------------------------------------------------------------
 *
 * This plane owns:
 * - continuation registration
 * - already-terminal delivery path
 * - delivery-thread / queue ownership
 * - terminal sweep scheduling
 *
 * It does NOT own:
 * - slot lifecycle authority
 * - waiter lifecycle authority
 * - governance taxonomy
 *
 * -----------------------------------------------------------------------------
 * CRITICAL RULE
 * -----------------------------------------------------------------------------
 *
 * Even when terminal truth is already visible, delivery-path execution policy
 * remains adapter-owned.
 *
 * The shard must not invoke continuations directly.
 */
internal interface L2JoinDispatchPlane {

    /**
     * Registers a continuation or performs already-terminal delivery through the
     * dispatch plane's own execution-path policy.
     */
    fun registerOrDeliverImmediate(
        slot: InFlightSlot<CanonicalPlanNode>,
        waiter: WaiterCell,
        continuation: JoinContinuation,
    ): JoinRegistrationDecision

    /**
     * Enqueues a terminal sweep for a slot whose authoritative terminal truth
     * is already visible.
     */
    fun enqueueTerminalSweep(
        slot: InFlightSlot<CanonicalPlanNode>,
    )
}
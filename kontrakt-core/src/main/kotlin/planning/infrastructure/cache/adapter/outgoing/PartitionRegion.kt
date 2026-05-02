package planning.infrastructure.cache.adapter.outgoing

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.fault.L2FaultKind
import planning.domain.interner.PlanCacheKey
import planning.domain.port.outgoing.PlanInternStep
import planning.domain.protocol.CostCenter
import planning.domain.runtime.lifecycle.L2LifecycleLaw
import planning.domain.runtime.lifecycle.PartitionRegionState
import planning.domain.session.PlannerSession
import planning.domain.session.policy.ResolvedJoinGovernance
import planning.domain.session.policy.ResolvedStorageGovernance
import planning.domain.vo.PartitionId
import planning.infrastructure.cache.adapter.outgoing.dispatch.L2JoinDispatchPlane
import planning.infrastructure.runtime.time.MonotonicTimeSource
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder

/**
 * Physical governance boundary for one partition.
 *
 * Responsibilities:
 * - partition-scoped storage governance
 * - explicit region lifecycle hosting
 * - deterministic shard routing
 * - close / reclaim gate for partition-wide mutable lifecycle state
 *
 * This type intentionally does NOT own:
 * - shared-slot lifecycle truth
 * - waiter lifecycle truth
 * - builder-handle lifecycle truth
 * - callback execution policy
 * - exact-match verification semantics
 *
 * Those remain delegated to shard / slot / waiter / dispatch-plane collaborators.
 */
internal class PartitionRegion private constructor(
    val id: PartitionId,
    private val joinGovernance: ResolvedJoinGovernance,
    private val storageGovernance: ResolvedStorageGovernance,
    shardCount: Int,
    bucketTableCapacity: Int,
    inflightTableCapacity: Int,
    dispatchPlane: L2JoinDispatchPlane,
    timeSource: MonotonicTimeSource,
) {
    /**
     * Explicit region lifecycle:
     *
     * OPEN -> CLOSE_PUBLISHED -> RECLAIMED
     */
    private val stateCode = AtomicInteger(PartitionRegionState.OPEN.code)

    /**
     * Storage-governance circuit-open flag.
     *
     * Orthogonal to region lifecycle:
     * - a region may still be OPEN but circuit-open
     * - once not OPEN, new work must bypass regardless
     */
    private val circuitOpen = AtomicInteger(0)

    /**
     * Storage accounting.
     *
     * We track both entry count and approximate bytes because storage governance is
     * defined along both axes.
     */
    private val entryCount = LongAdder()
    private val approxByteCount = LongAdder()

    private val shardMask = shardCount - 1

    private val shards: Array<L2Shard> =
        Array(shardCount) { shardIndex ->
            L2Shard.issue(
                owner = this,
                shardIndex = shardIndex,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxWaitersPerKey = joinGovernance.maxWaitersPerKey,
                joinWaitTimeoutNanos = joinGovernance.joinWaitTimeoutNanos,
                dispatchPlane = dispatchPlane,
                timeSource = timeSource,
            )
        }

    fun resolveOrIntern(
        key: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternStep {
        if (readLifecycle() != PartitionRegionState.OPEN) {
            session.step(CostCenter.L2_BYPASS_READ)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }

        if (storageGovernance.circuitOpenOnStorageExhaustion && isStorageExceeded()) {
            forceCircuitOpen(session)
        }

        if (isCircuitOpen()) {
            session.step(CostCenter.L2_BYPASS_READ)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }

        session.step(CostCenter.L2_SHARD_ROUTE)

        val shardIndex = routeKeyBits.toInt() and shardMask
        return shards[shardIndex].getOrIntern(
            key = key,
            routeKeyBits = routeKeyBits,
            session = session,
        )
    }

    /**
     * Region-level builder-path admission check after shard-local acquisition.
     *
     * This remains region-owned because:
     * - close publication is partition-wide
     * - storage circuit-open is partition-wide governance
     */
    fun allowBuilderAfterAcquire(session: PlannerSession): Boolean {
        session.step(CostCenter.L2_CAPACITY_CHECK)

        if (readLifecycle() != PartitionRegionState.OPEN) {
            return false
        }

        if (storageGovernance.circuitOpenOnStorageExhaustion && isStorageExceeded()) {
            forceCircuitOpen(session)
        }

        return readLifecycle() == PartitionRegionState.OPEN && !isCircuitOpen()
    }

    /**
     * Accounting hook after authoritative entry commit.
     *
     * [approxBytesDelta] is intentionally approximate.
     * Storage governance is a survivability/degradation contract, not an exact semantic
     * size law.
     */
    fun onEntryCommitted(
        session: PlannerSession,
        approxBytesDelta: Long = 0L,
    ) {
        if (approxBytesDelta < 0L) {
            throw PlanningProtocolIntegrityException(
                "PartitionRegion.onEntryCommitted.approxBytesDelta must be >= 0: $approxBytesDelta",
            )
        }

        entryCount.increment()
        approxByteCount.add(approxBytesDelta)
        session.step(CostCenter.L2_CAPACITY_CHECK)

        if (storageGovernance.circuitOpenOnStorageExhaustion && isStorageExceeded()) {
            forceCircuitOpen(session)
        }
    }

    /**
     * One-way storage-governance degradation transition.
     */
    fun forceCircuitOpen(session: PlannerSession) {
        if (circuitOpen.compareAndSet(0, 1)) {
            session.step(CostCenter.L2_CIRCUIT_OPEN_TRANSITION)
        }
    }

    fun isPublishAllowed(): Boolean = readLifecycle() == PartitionRegionState.OPEN && !isCircuitOpen()

    fun isBypassRequired(): Boolean = readLifecycle() != PartitionRegionState.OPEN || isCircuitOpen()

    /**
     * Publish close.
     *
     * After this point, fresh lifecycle admission must not rely on region openness.
     */
    fun closePublished() {
        while (true) {
            when (val current = readLifecycle()) {
                PartitionRegionState.OPEN -> {
                    val target = PartitionRegionState.CLOSE_PUBLISHED
                    L2LifecycleLaw.requireTransition(current, target)
                    if (stateCode.compareAndSet(current.code, target.code)) {
                        return
                    }
                }

                PartitionRegionState.CLOSE_PUBLISHED,
                PartitionRegionState.RECLAIMED,
                -> return
            }
        }
    }

    /**
     * Backward-compatibility alias for older call sites.
     */
    fun close() {
        closePublished()
    }

    /**
     * Partition-wide abort of visible in-flight activity.
     *
     * This assumes close publication has already happened, or is being published
     * immediately before this call.
     */
    fun abortAllInFlight() {
        closePublished()
        for (shard in shards) {
            shard.abortAllInFlight()
        }
    }

    /**
     * Stronger lifecycle advancement after external convergence/quiescence barrier.
     */
    fun markReclaimed() {
        while (true) {
            when (val current = readLifecycle()) {
                PartitionRegionState.OPEN -> {
                    throw PlanningProtocolIntegrityException(
                        "PartitionRegion.markReclaimed requires CLOSE_PUBLISHED first.",
                    )
                }

                PartitionRegionState.CLOSE_PUBLISHED -> {
                    val target = PartitionRegionState.RECLAIMED
                    L2LifecycleLaw.requireTransition(current, target)
                    if (stateCode.compareAndSet(current.code, target.code)) {
                        return
                    }
                }

                PartitionRegionState.RECLAIMED -> return
            }
        }
    }

    private fun readLifecycle(): PartitionRegionState = PartitionRegionState.fromCode(stateCode.get())

    private fun isCircuitOpen(): Boolean = circuitOpen.get() != 0

    private fun isStorageExceeded(): Boolean {
        val entriesExceeded =
            entryCount.sum() >= storageGovernance.maxEntriesPerPartition.toLong()

        val approxBytesExceeded =
            approxByteCount.sum() >= storageGovernance.maxApproxBytesPerPartition

        return entriesExceeded || approxBytesExceeded
    }

    companion object {
        @JvmStatic
        fun issue(
            id: PartitionId,
            joinGovernance: ResolvedJoinGovernance,
            storageGovernance: ResolvedStorageGovernance,
            shardCount: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            dispatchPlane: L2JoinDispatchPlane,
            timeSource: MonotonicTimeSource,
        ): PartitionRegion {
            validate(
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
            )

            return PartitionRegion(
                id = id,
                joinGovernance = joinGovernance,
                storageGovernance = storageGovernance,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                dispatchPlane = dispatchPlane,
                timeSource = timeSource,
            )
        }

        private fun validate(
            shardCount: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
        ) {
            if (shardCount <= 0 || shardCount.countOneBits() != 1) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.shardCount must be a positive power-of-two: $shardCount",
                )
            }

            if (bucketTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.bucketTableCapacity must be positive: $bucketTableCapacity",
                )
            }

            if (inflightTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.inflightTableCapacity must be positive: $inflightTableCapacity",
                )
            }
        }
    }
}

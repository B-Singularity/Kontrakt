package realization.runtime.cache

import governance.budget.contract.CostCenter
import migration.quarantine.ResolvedDispatchLanePolicy
import migration.quarantine.ResolvedJoinGovernance
import migration.quarantine.ResolvedStorageGovernance
import realization.cache.lifecycle.AdapterAdminLifecycleLaw
import realization.cache.lifecycle.AdapterAdminState
import realization.identity.interning.PlanCacheKey
import realization.planning.session.PlannerSession
import realization.runtime.dispatch.DeterministicL2JoinDispatchPlane
import realization.runtime.dispatch.L2JoinDispatchPlane
import realization.runtime.time.MonotonicTimeSource
import realization.runtime.time.SystemMonotonicTimeSource
import stage.lowering.boundary.PlanInternRepository
import stage.lowering.boundary.PlanInternStep
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import stage.lowering.material.PartitionId
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * In-memory Tier-2 outbound adapter.
 *
 * Architectural role:
 * - partition-first physical composition root
 * - owner of the partition-region directory
 * - owner of dispatch-plane installation and partition-drop / adapter-close orchestration
 *
 * Deliberate design properties:
 * - no polling-era join knobs remain on the public surface
 * - no public method exposes internal dispatch/time collaborators
 * - no ConcurrentHashMap
 * - no striped lock directory
 * - no monitor-based admin directory coordination
 *
 * The partition directory is intentionally implemented as an immutable-snapshot CAS map.
 *
 * Rationale:
 * - partition lookup is not the primitive shard-local hot path
 * - region creation/removal is expected to be rare compared to steady-state lookups
 * - immutable snapshot replacement is easier to reason about than CHM or striped locks
 *
 * Administrative coordination is modeled as an explicit adapter-local lifecycle:
 * - OPEN
 * - DROP_IN_PROGRESS
 * - CLOSING
 * - CLOSED
 *
 * This keeps drop/close serialization explicit rather than burying it in ad hoc branches.
 */
class InMemoryPlanInternRepositoryAdapter private constructor(
    private val joinGovernance: ResolvedJoinGovernance,
    private val storageGovernance: ResolvedStorageGovernance,
    private val dispatchLanePolicy: ResolvedDispatchLanePolicy,
    private val shardCount: Int,
    private val bucketTableCapacity: Int,
    private val inflightTableCapacity: Int,
    private val dispatchPlane: L2JoinDispatchPlane,
    private val timeSource: MonotonicTimeSource,
) : PlanInternRepository,
    AutoCloseable {
    private val regions = PartitionRegionDirectory()

    /**
     * Adapter-local administrative lifecycle host.
     *
     * This state is intentionally orthogonal to:
     * - partition-region lifecycle
     * - dispatch-lane lifecycle
     * - planning-run lifecycle
     *
     * It exists only to serialize:
     * - partition drop
     * - whole-adapter close
     */
    private val adminStateCode = AtomicInteger(AdapterAdminState.OPEN.code)

    override fun resolveOrIntern(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
    ): PlanInternStep {
        if (adminStateAcquire() == AdapterAdminState.CLOSED) {
            throw PlanningProtocolIntegrityException(
                "resolveOrIntern() is illegal after adapter close completion.",
            )
        }

        session.step(CostCenter.L2_REGION_LOOKUP)

        val region =
            regions.getOrCreate(partitionId) {
                PartitionRegion.issue(
                    id = partitionId,
                    joinGovernance = joinGovernance,
                    storageGovernance = storageGovernance,
                    shardCount = shardCount,
                    bucketTableCapacity = bucketTableCapacity,
                    inflightTableCapacity = inflightTableCapacity,
                    dispatchPlane = dispatchPlane,
                    timeSource = timeSource,
                )
            }

        return region.resolveOrIntern(
            key = key,
            routeKeyBits = key.route64,
            session = session,
        )
    }

    /**
     * Administrative bulk drop for one partition.
     *
     * Sequence:
     * 1. acquire exclusive admin authority
     * 2. publish region close
     * 3. abort visible in-flight activity
     * 4. wait for dispatch-plane quiescence using policy-managed grace
     * 5. mark reclaimed
     * 6. remove by identity from the directory
     *
     * If quiescence is not reached in time, the region remains close-published and
     * discoverable in the directory. This is deliberate fail-closed behavior.
     */
    fun dropPartition(partitionId: PartitionId): Boolean {
        if (!beginDrop()) {
            return when (adminStateAcquire()) {
                AdapterAdminState.CLOSING,
                AdapterAdminState.CLOSED,
                    -> false

                AdapterAdminState.DROP_IN_PROGRESS -> {
                    throw PlanningProtocolIntegrityException(
                        "Concurrent partition-drop operations are forbidden.",
                    )
                }

                AdapterAdminState.OPEN -> false
            }
        }

        try {
            val region = regions.get(partitionId) ?: return true

            region.closePublished()
            region.abortAllInFlight()

            val quiesced =
                dispatchPlane.awaitQuiescence(
                    timeout = dispatchLanePolicy.partitionDropQuiescenceTimeoutNanos,
                    unit = TimeUnit.NANOSECONDS,
                )
            if (!quiesced) {
                return false
            }

            region.markReclaimed()
            regions.removeIfSame(partitionId, region)
            return true
        } finally {
            finishDrop()
        }
    }

    /**
     * Whole-adapter shutdown.
     *
     * Shutdown is intentionally strong and conservative:
     * - no concurrent partition-drop may overlap
     * - all visible regions are close-published
     * - all visible in-flight activity is aborted
     * - dispatch convergence is awaited under policy-managed grace
     * - regions are marked reclaimed
     * - directory is cleared
     * - dispatch plane is closed last
     *
     * If quiescence grace expires, plane.close() must still force lane-owned
     * abandonment/final clear from within the lane worker threads themselves.
     */
    override fun close() {
        if (!beginClose()) {
            when (adminStateAcquire()) {
                AdapterAdminState.CLOSED,
                AdapterAdminState.CLOSING,
                    -> return

                AdapterAdminState.DROP_IN_PROGRESS -> {
                    throw PlanningProtocolIntegrityException(
                        "Adapter close cannot start while a partition-drop operation is in progress.",
                    )
                }

                AdapterAdminState.OPEN -> return
            }
        }

        try {
            val snapshot = regions.snapshot()

            for (region in snapshot) {
                region.closePublished()
            }
            for (region in snapshot) {
                region.abortAllInFlight()
            }

            dispatchPlane.awaitQuiescence(
                timeout = dispatchLanePolicy.adapterCloseQuiescenceTimeoutNanos,
                unit = TimeUnit.NANOSECONDS,
            )

            for (region in snapshot) {
                region.markReclaimed()
            }

            regions.clear()
            dispatchPlane.close()
        } finally {
            finishClose()
        }
    }

    private fun adminStateAcquire(): AdapterAdminState = AdapterAdminState.fromCode(adminStateCode.get())

    private fun beginDrop(): Boolean {
        while (true) {
            val from = adminStateAcquire()

            if (!AdapterAdminLifecycleLaw.canStartDrop(from)) {
                return false
            }

            val to = AdapterAdminState.DROP_IN_PROGRESS
            AdapterAdminLifecycleLaw.requireTransition(from, to)

            if (adminStateCode.compareAndSet(from.code, to.code)) {
                return true
            }
        }
    }

    private fun finishDrop() {
        while (true) {
            val from = adminStateAcquire()

            if (from == AdapterAdminState.OPEN) {
                return
            }

            val to = AdapterAdminState.OPEN
            AdapterAdminLifecycleLaw.requireTransition(from, to)

            if (adminStateCode.compareAndSet(from.code, to.code)) {
                return
            }
        }
    }

    private fun beginClose(): Boolean {
        while (true) {
            val from = adminStateAcquire()

            if (!AdapterAdminLifecycleLaw.canStartClose(from)) {
                return false
            }

            val to = AdapterAdminState.CLOSING
            AdapterAdminLifecycleLaw.requireTransition(from, to)

            if (adminStateCode.compareAndSet(from.code, to.code)) {
                return true
            }
        }
    }

    private fun finishClose() {
        while (true) {
            val from = adminStateAcquire()

            if (from == AdapterAdminState.CLOSED) {
                return
            }

            val to = AdapterAdminState.CLOSED
            AdapterAdminLifecycleLaw.requireTransition(from, to)

            if (adminStateCode.compareAndSet(from.code, to.code)) {
                return
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            joinGovernance: ResolvedJoinGovernance,
            storageGovernance: ResolvedStorageGovernance,
            dispatchLanePolicy: ResolvedDispatchLanePolicy,
            shardCount: Int = 16,
            bucketTableCapacity: Int = 1 shl 16,
            inflightTableCapacity: Int = 1 shl 10,
        ): InMemoryPlanInternRepositoryAdapter {
            validate(
                dispatchLanePolicy = dispatchLanePolicy,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
            )

            val timeSource = SystemMonotonicTimeSource
            val dispatchPlane =
                DeterministicL2JoinDispatchPlane.issue(
                    policy = dispatchLanePolicy,
                    shardCount = shardCount,
                    timeSource = timeSource,
                )

            return InMemoryPlanInternRepositoryAdapter(
                joinGovernance = joinGovernance,
                storageGovernance = storageGovernance,
                dispatchLanePolicy = dispatchLanePolicy,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                dispatchPlane = dispatchPlane,
                timeSource = timeSource,
            )
        }

        private fun validate(
            dispatchLanePolicy: ResolvedDispatchLanePolicy,
            shardCount: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
        ) {
            if (shardCount <= 0 || shardCount.countOneBits() != 1) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.shardCount must be a positive power-of-two: $shardCount",
                )
            }

            if (bucketTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.bucketTableCapacity must be positive: $bucketTableCapacity",
                )
            }

            if (inflightTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.inflightTableCapacity must be positive: $inflightTableCapacity",
                )
            }

            val effectiveLaneCount = minOf(dispatchLanePolicy.laneCount, shardCount)
            if (effectiveLaneCount <= 0 || effectiveLaneCount.countOneBits() != 1) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.effectiveLaneCount must be a positive power-of-two: " +
                            effectiveLaneCount,
                )
            }

            val maxShardsOwnedByOneLane = shardCount / effectiveLaneCount
            val minRequiredDeadlineHeapCapacity =
                dispatchLanePolicy.registrationStoreCapacityPerShard * maxShardsOwnedByOneLane

            if (dispatchLanePolicy.deadlineHeapCapacity < minRequiredDeadlineHeapCapacity) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.deadlineHeapCapacity is too small for the maximum lane-owned " +
                            "registration population: ${dispatchLanePolicy.deadlineHeapCapacity} < " +
                            minRequiredDeadlineHeapCapacity,
                )
            }
        }
    }

    /**
     * Immutable-snapshot CAS directory.
     *
     * This avoids:
     * - ConcurrentHashMap
     * - striped locks
     * - mixed visibility through shared mutable map state
     *
     * Mutation is expected to be relatively rare.
     * We therefore prefer explicit immutable replacement over hidden shared mutation.
     */
    private class PartitionRegionDirectory {
        private val state =
            AtomicReference(DirectoryState(emptyMap()))

        fun get(partitionId: PartitionId): PartitionRegion? = state.get().regions[partitionId]

        fun getOrCreate(
            partitionId: PartitionId,
            factory: () -> PartitionRegion,
        ): PartitionRegion {
            while (true) {
                val observed = state.get()
                observed.regions[partitionId]?.let { return it }

                val created = factory()
                val nextMap = HashMap(observed.regions)
                nextMap[partitionId] = created
                val updated = DirectoryState(nextMap)

                if (state.compareAndSet(observed, updated)) {
                    return created
                }
            }
        }

        fun removeIfSame(
            partitionId: PartitionId,
            expected: PartitionRegion,
        ): Boolean {
            while (true) {
                val observed = state.get()
                val current = observed.regions[partitionId] ?: return false
                if (current !== expected) {
                    return false
                }

                val nextMap = HashMap(observed.regions)
                nextMap.remove(partitionId)
                val updated = DirectoryState(nextMap)

                if (state.compareAndSet(observed, updated)) {
                    return true
                }
            }
        }

        fun snapshot(): List<PartitionRegion> = ArrayList(state.get().regions.values)

        fun clear() {
            state.set(DirectoryState(emptyMap()))
        }

        private class DirectoryState(
            val regions: Map<PartitionId, PartitionRegion>,
        )
    }
}

package planning.infrastructure.cache.adapter.outgoing

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.fault.L2FaultKind
import planning.domain.interner.PlanCacheKey
import planning.domain.port.outgoing.PlanInternStep
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.LongAdder

/**
 * Physical governance boundary for one partition.
 *
 * Responsibilities:
 * - partition-scoped capacity governance
 * - circuit-open state management
 * - deterministic shard routing
 * - partition-level sealing for bulk drop
 */
class PartitionRegion private constructor(
    val id: PartitionId,
    private val maxEntries: Long,
    shardCount: Int,
    bucketTableCapacity: Int,
    inflightTableCapacity: Int,
    maxJoinPolls: Int,
    joinPollNanos: Long,
) {
    private val closed = AtomicBoolean(false)
    private val circuitOpen = AtomicBoolean(false)
    private val entryCount = LongAdder()

    private val shardMask = shardCount - 1

    private val shards: Array<L2Shard> = Array(shardCount) {
        L2Shard.issue(
            owner = this,
            bucketTableCapacity = bucketTableCapacity,
            inflightTableCapacity = inflightTableCapacity,
            maxJoinPolls = maxJoinPolls,
            joinPollNanos = joinPollNanos,
        )
    }

    fun resolveOrIntern(
        key: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternStep {
        if (closed.get()) {
            session.step(CostCenter.L2_BYPASS_READ)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternStep.fault(L2FaultKind.CIRCUIT_OPEN)
        }

        if (isCapacityExceeded()) {
            forceCircuitOpen(session)
        }

        if (circuitOpen.get()) {
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

    fun allowBuilderAfterAcquire(session: PlannerSession): Boolean {
        session.step(CostCenter.L2_CAPACITY_CHECK)

        if (closed.get()) {
            return false
        }

        if (isCapacityExceeded()) {
            forceCircuitOpen(session)
        }

        return !circuitOpen.get()
    }

    fun onEntryCommitted(session: PlannerSession) {
        entryCount.increment()
        session.step(CostCenter.L2_CAPACITY_CHECK)

        if (isCapacityExceeded()) {
            forceCircuitOpen(session)
        }
    }

    fun forceCircuitOpen(session: PlannerSession) {
        if (circuitOpen.compareAndSet(false, true)) {
            session.step(CostCenter.L2_CIRCUIT_OPEN_TRANSITION)
        }
    }

    fun isPublishAllowed(): Boolean = !closed.get() && !circuitOpen.get()

    fun isBypassRequired(): Boolean = closed.get() || circuitOpen.get()

    fun close() {
        closed.set(true)
    }

    fun abortAllInFlight() {
        for (shard in shards) {
            shard.abortAllInFlight()
        }
    }

    private fun isCapacityExceeded(): Boolean = entryCount.sum() >= maxEntries

    companion object {
        @JvmStatic
        fun issue(
            id: PartitionId,
            maxEntries: Long,
            shardCount: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxJoinPolls: Int,
            joinPollNanos: Long,
        ): PartitionRegion {
            validate(
                maxEntries = maxEntries,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )

            return PartitionRegion(
                id = id,
                maxEntries = maxEntries,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )
        }

        private fun validate(
            maxEntries: Long,
            shardCount: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxJoinPolls: Int,
            joinPollNanos: Long,
        ) {
            if (maxEntries <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.maxEntries must be positive: $maxEntries"
                )
            }
            if (shardCount <= 0 || shardCount.countOneBits() != 1) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.shardCount must be a positive power-of-two: $shardCount"
                )
            }
            if (bucketTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.bucketTableCapacity must be positive: $bucketTableCapacity"
                )
            }
            if (inflightTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.inflightTableCapacity must be positive: $inflightTableCapacity"
                )
            }
            if (maxJoinPolls <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.maxJoinPolls must be positive: $maxJoinPolls"
                )
            }
            if (joinPollNanos < 0L) {
                throw PlanningProtocolIntegrityException(
                    "PartitionRegion.joinPollNanos must be >= 0: $joinPollNanos"
                )
            }
        }
    }
}
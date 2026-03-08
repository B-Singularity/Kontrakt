package planning.infrastructure.cache.adapter.outgoing

import ir.plan.signature.PlanCacheKey
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.PlanInternResult
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
internal class PartitionRegion(
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
        L2Shard(
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
    ): PlanInternResult {
        if (closed.get()) {
            session.step(CostCenter.L2_BYPASS_READ)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
        }

        if (isCapacityExceeded()) {
            forceCircuitOpen(session)
        }

        if (circuitOpen.get()) {
            session.step(CostCenter.L2_BYPASS_READ)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
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
     * Builder-path admission gate executed after the in-flight slot is acquired.
     *
     * This is the proactive capacity checkpoint required by the design:
     * the expensive build must not start if the region is already sealed/open or
     * if capacity has already been breached.
     */
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

    /**
     * Called only when an actual new canonical entry was appended to Tier-2.
     */
    fun onEntryCommitted(session: PlannerSession) {
        entryCount.increment()

        /*
         * Post-commit governance:
         * the entry is already published; now we update the survival policy.
         */
        session.step(CostCenter.L2_CAPACITY_CHECK)
        if (isCapacityExceeded()) {
            forceCircuitOpen(session)
        }
    }

    /**
     * Idempotent circuit-open transition.
     */
    fun forceCircuitOpen(session: PlannerSession) {
        if (circuitOpen.compareAndSet(false, true)) {
            session.step(CostCenter.L2_CIRCUIT_OPEN_TRANSITION)
        }
    }

    /**
     * Returns true when new Tier-2 publication is still allowed.
     */
    fun isPublishAllowed(): Boolean = !closed.get() && !circuitOpen.get()

    /**
     * Returns true when the caller should stop relying on Tier-2 and fall back
     * to Domain-level bypass/degrade policy.
     */
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
}
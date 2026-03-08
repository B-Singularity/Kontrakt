package planning.infrastructure.cache.adapter.outgoing

import ir.plan.node.CanonicalPlanNode
import ir.plan.signature.PlanCacheKey
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.InternHandle
import planning.domain.port.outgoing.PlanInternResult
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.infrastructure.cache.InFlightSlot
import planning.infrastructure.cache.L2TableSegmentSaturatedException
import planning.infrastructure.cache.LongKeyTable
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionException
import java.util.concurrent.locks.LockSupport

/**
 * Contention-reduction strip inside one partition region.
 *
 * Hot-path routing uses primitive Long-key tables:
 * - buckets: committed canonical storage
 * - inflight: per-key builder gate
 */
class L2Shard private constructor(
    private val owner: PartitionRegion,
    bucketTableCapacity: Int,
    inflightTableCapacity: Int,
    private val maxJoinPolls: Int,
    private val joinPollNanos: Long,
) {
    private val buckets = LongKeyTable.issue<L2Bucket>(capacity = bucketTableCapacity)
    private val inflight = LongKeyTable.issue<InFlightSlot<CanonicalPlanNode>>(capacity = inflightTableCapacity)

    fun getOrIntern(
        key: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternResult {
        try {
            session.step(CostCenter.L2_PRE_SCREEN_GET)
            val bucket = buckets.get(routeKeyBits)
            if (bucket != null) {
                session.step(CostCenter.L2_BUCKET_SCAN)
                val exact = bucket.findExact(key)
                if (exact != null) {
                    session.step(CostCenter.L2_HIT)
                    return PlanInternResult.hit(exact)
                }
            }

            session.step(CostCenter.L2_INFLIGHT_ACQUIRE)
            val mySlot = InFlightSlot.issue<CanonicalPlanNode>()
            val existing = inflight.putIfAbsent(routeKeyBits, mySlot)

            if (existing == null) {
                if (!owner.allowBuilderAfterAcquire(session)) {
                    session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                    mySlot.future.completeExceptionally(
                        CancellationException("Tier-2 builder admission rejected.")
                    )
                    inflight.removeIfSame(routeKeyBits, mySlot)
                    return PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
                }

                return PlanInternResult.miss(
                    L2InternHandle(
                        key = key,
                        routeKeyBits = routeKeyBits,
                        slot = mySlot,
                        session = session,
                    )
                )
            }

            return joinInFlight(
                slot = existing,
                targetKey = key,
                routeKeyBits = routeKeyBits,
                session = session,
            )
        } catch (e: L2TableSegmentSaturatedException) {
            owner.forceCircuitOpen(session)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
        }
    }

    private fun joinInFlight(
        slot: InFlightSlot<CanonicalPlanNode>,
        targetKey: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternResult {
        slot.incrementWaiters()
        var polls = 0

        try {
            while (polls < maxJoinPolls) {
                session.step(CostCenter.L2_INFLIGHT_WAIT)

                val immediate = slot.future.getNow(null)
                if (immediate != null) {
                    return reverifyCommittedWinner(targetKey, routeKeyBits, session)
                }

                if (slot.future.isCompletedExceptionally) {
                    return mapExceptionalJoin(slot, session)
                }

                polls += 1
                if (joinPollNanos > 0L) {
                    LockSupport.parkNanos(joinPollNanos)
                }
            }

            return if (owner.isBypassRequired()) {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
            } else {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                PlanInternResult.fault(L2FaultKind.TRANSIENT)
            }
        } finally {
            slot.decrementWaiters()
        }
    }

    private fun reverifyCommittedWinner(
        targetKey: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternResult {
        session.step(CostCenter.L2_BUCKET_SCAN)

        val bucket = buckets.get(routeKeyBits)
        val exact = bucket?.findExact(targetKey)

        return if (exact != null) {
            session.step(CostCenter.L2_HIT)
            PlanInternResult.hit(exact)
        } else if (owner.isBypassRequired()) {
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
        } else {
            session.step(CostCenter.L2_FAULT_TRANSIENT)
            PlanInternResult.fault(L2FaultKind.TRANSIENT)
        }
    }

    private fun mapExceptionalJoin(
        slot: InFlightSlot<CanonicalPlanNode>,
        session: PlannerSession,
    ): PlanInternResult {
        return try {
            slot.future.join()
            session.step(CostCenter.L2_FAULT_TRANSIENT)
            PlanInternResult.fault(L2FaultKind.TRANSIENT)
        } catch (e: CancellationException) {
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
        } catch (e: CompletionException) {
            val cause = e.cause
            if (cause is CancellationException || owner.isBypassRequired()) {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
            } else {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                PlanInternResult.fault(L2FaultKind.TRANSIENT)
            }
        } catch (e: Throwable) {
            if (owner.isBypassRequired()) {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                PlanInternResult.fault(L2FaultKind.CIRCUIT_OPEN)
            } else {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                PlanInternResult.fault(L2FaultKind.TRANSIENT)
            }
        }
    }

    fun abortAllInFlight() {
        inflight.forEachOccupiedValueForClosedPartitionDrop { slot ->
            slot.future.completeExceptionally(
                CancellationException("Partition dropped.")
            )
        }
    }

    private inner class L2InternHandle(
        private val key: PlanCacheKey,
        private val routeKeyBits: Long,
        private val slot: InFlightSlot<CanonicalPlanNode>,
        private val session: PlannerSession,
    ) : InternHandle {

        override fun commit(localNode: CanonicalPlanNode): CanonicalPlanNode {
            try {
                if (!owner.isPublishAllowed()) {
                    session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                    slot.future.completeExceptionally(
                        CancellationException("Tier-2 publication rejected after seal/open.")
                    )
                    return localNode
                }

                session.step(CostCenter.L2_PUBLISH_PUT_IF_ABSENT)

                val freshBucket = L2Bucket(
                    initialKey = key,
                    initialNode = localNode,
                )

                val existingBucket = buckets.putIfAbsent(routeKeyBits, freshBucket)

                if (existingBucket == null) {
                    owner.onEntryCommitted(session)
                    slot.future.complete(localNode)
                    return localNode
                }

                val result = existingBucket.putIfAbsentOrGet(key, localNode)
                if (result.inserted) {
                    owner.onEntryCommitted(session)
                }

                slot.future.complete(result.winner)
                return result.winner
            } catch (e: L2TableSegmentSaturatedException) {
                owner.forceCircuitOpen(session)
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                slot.future.completeExceptionally(
                    CancellationException("Tier-2 publication failed: primitive table saturated.")
                )
                return localNode
            } catch (e: Throwable) {
                slot.future.completeExceptionally(e)
                throw e
            } finally {
                inflight.removeIfSame(routeKeyBits, slot)
            }
        }

        override fun abort(reason: Throwable) {
            try {
                slot.future.completeExceptionally(reason)
            } finally {
                inflight.removeIfSame(routeKeyBits, slot)
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            owner: PartitionRegion,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxJoinPolls: Int,
            joinPollNanos: Long,
        ): L2Shard {
            validate(
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )

            return L2Shard(
                owner = owner,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )
        }

        private fun validate(
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxJoinPolls: Int,
            joinPollNanos: Long,
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
            if (maxJoinPolls <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.maxJoinPolls must be positive: $maxJoinPolls"
                )
            }
            if (joinPollNanos < 0L) {
                throw PlanningProtocolIntegrityException(
                    "L2Shard.joinPollNanos must be >= 0: $joinPollNanos"
                )
            }
        }
    }
}
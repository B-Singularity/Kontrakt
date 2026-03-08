package planning.infrastructure.cache.adapter.outgoing

import ir.plan.node.CanonicalPlanNode
import ir.plan.signature.PlanCacheKey
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
internal class L2Shard(
    private val owner: PartitionRegion,
    bucketTableCapacity: Int,
    inflightTableCapacity: Int,
    private val maxJoinPolls: Int,
    private val joinPollNanos: Long,
) {
    private val buckets = LongKeyTable<L2Bucket>(capacity = bucketTableCapacity)
    private val inflight = LongKeyTable<InFlightSlot<CanonicalPlanNode>>(capacity = inflightTableCapacity)

    fun getOrIntern(
        key: PlanCacheKey,
        routeKeyBits: Long,
        session: PlannerSession,
    ): PlanInternResult {
        try {
            /*
             * Step 1: pre-screen exact hit from committed storage.
             */
            session.step(CostCenter.L2_PRE_SCREEN_GET)
            val bucket = buckets.get(routeKeyBits)
            if (bucket != null) {
                session.step(CostCenter.L2_BUCKET_SCAN)
                val exact = bucket.findExact(key)
                if (exact != null) {
                    session.step(CostCenter.L2_HIT)
                    return PlanInternResult.Hit(exact)
                }
            }

            /*
             * Step 2: acquire the per-key in-flight gate.
             */
            session.step(CostCenter.L2_INFLIGHT_ACQUIRE)
            val mySlot = InFlightSlot<CanonicalPlanNode>()
            val existing = inflight.putIfAbsent(routeKeyBits, mySlot)

            if (existing == null) {
                /*
                 * Builder path.
                 * We own the in-flight slot, but we must still apply the proactive
                 * governance gate before the expensive build starts.
                 */
                if (!owner.allowBuilderAfterAcquire(session)) {
                    session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                    mySlot.future.completeExceptionally(
                        CancellationException("Tier-2 builder admission rejected."),
                    )
                    inflight.removeIfSame(routeKeyBits, mySlot)
                    return PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
                }

                return PlanInternResult.Miss(
                    L2InternHandle(
                        key = key,
                        routeKeyBits = routeKeyBits,
                        slot = mySlot,
                        session = session,
                    ),
                )
            }

            /*
             * Joiner path.
             */
            return joinInFlight(
                slot = existing,
                targetKey = key,
                routeKeyBits = routeKeyBits,
                session = session,
            )
        } catch (e: L2TableSegmentSaturatedException) {
            owner.forceCircuitOpen(session)
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            return PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
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
                    return reverifyCommittedWinner(
                        targetKey = targetKey,
                        routeKeyBits = routeKeyBits,
                        session = session,
                    )
                }

                if (slot.future.isCompletedExceptionally) {
                    return mapExceptionalJoin(slot, session)
                }

                polls += 1
                if (joinPollNanos > 0L) {
                    LockSupport.parkNanos(joinPollNanos)
                }
            }

            /*
             * Bounded wait exhaustion.
             *
             * If the region is already open/closed, report CircuitOpen.
             * Otherwise report Transient so the Domain can degrade to a miss.
             */
            return if (owner.isBypassRequired()) {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
            } else {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                PlanInternResult.Fault(L2FaultKind.TRANSIENT)
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
        /*
         * The future result itself is not trusted as the final correctness seal
         * because route-key collisions are resolved only by exact full-key match.
         */
        session.step(CostCenter.L2_BUCKET_SCAN)

        val bucket = buckets.get(routeKeyBits)
        val exact = bucket?.findExact(targetKey)

        return if (exact != null) {
            session.step(CostCenter.L2_HIT)
            PlanInternResult.Hit(exact)
        } else if (owner.isBypassRequired()) {
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
        } else {
            session.step(CostCenter.L2_FAULT_TRANSIENT)
            PlanInternResult.Fault(L2FaultKind.TRANSIENT)
        }
    }

    private fun mapExceptionalJoin(
        slot: InFlightSlot<CanonicalPlanNode>,
        session: PlannerSession,
    ): PlanInternResult {
        return try {
            /*
             * join() is safe here because the future is already completed exceptionally.
             * We use it only to inspect the terminal cause.
             */
            slot.future.join()

            /*
             * Defensive fallback. A correctly completed-exceptional future should
             * not reach this path normally.
             */
            session.step(CostCenter.L2_FAULT_TRANSIENT)
            PlanInternResult.Fault(L2FaultKind.TRANSIENT)
        } catch (e: CancellationException) {
            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
            PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
        } catch (e: CompletionException) {
            val cause = e.cause
            if (cause is CancellationException || owner.isBypassRequired()) {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
            } else {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                PlanInternResult.Fault(L2FaultKind.TRANSIENT)
            }
        } catch (e: Throwable) {
            if (owner.isBypassRequired()) {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                PlanInternResult.Fault(L2FaultKind.CIRCUIT_OPEN)
            } else {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                PlanInternResult.Fault(L2FaultKind.TRANSIENT)
            }
        }
    }

    /**
     * Wakes all in-flight waiters during partition drop.
     *
     * This is intentionally linear and used only on the administrative drop path.
     */
    fun abortAllInFlight() {
        inflight.forEachOccupiedValueForClosedPartitionDrop { slot ->
            slot.future.completeExceptionally(
                CancellationException("Partition dropped."),
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
                /*
                 * A builder may finish after a concurrent drop or open transition.
                 * In that case, we must not publish into Tier-2 anymore.
                 *
                 * Returning localNode is valid because the Domain still needs a
                 * usable immutable node even when interning is bypassed.
                 */
                if (!owner.isPublishAllowed()) {
                    session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                    slot.future.completeExceptionally(
                        CancellationException("Tier-2 publication rejected after seal/open."),
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
                    /*
                     * First bucket install is itself a successful canonical commit.
                     */
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
                    CancellationException("Tier-2 publication failed: primitive table saturated."),
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
}
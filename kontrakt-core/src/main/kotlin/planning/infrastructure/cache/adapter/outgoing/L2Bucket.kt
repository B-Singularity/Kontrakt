package planning.infrastructure.cache.adapter.outgoing

import ir.plan.node.CanonicalPlanNode
import planning.domain.interner.PlanCacheKey

/**
 * Small collision bucket keyed by routeKeyBits at the shard-table level.
 *
 * Correctness is sealed here by exact comparison of the full PlanCacheKey tuple.
 *
 * The bucket is intentionally tiny:
 * - routing is done by routeKeyBits first
 * - collisions and co-residents are expected to remain small
 * - insertion/scan is protected by a short monitor section
 */
internal class L2Bucket(
    initialKey: PlanCacheKey,
    initialNode: CanonicalPlanNode,
) {
    private val lock = Any()

    private val entries =
        ArrayList<Entry>(2).apply {
            add(
                Entry(
                    key = initialKey,
                    node = initialNode,
                    approxBytes = 0,
                ),
            )
        }

    /**
     * Result of an insertion attempt.
     *
     * - winner   : the exact canonical instance to return
     * - inserted : true iff a new canonical entry was appended
     */
    internal class PutResult(
        val winner: CanonicalPlanNode,
        val inserted: Boolean,
    )

    private class Entry(
        val key: PlanCacheKey,
        val node: CanonicalPlanNode,
        val approxBytes: Int,
    )

    /**
     * Exact-key scan under the bucket lock.
     */
    fun findExact(targetKey: PlanCacheKey): CanonicalPlanNode? {
        synchronized(lock) {
            for (entry in entries) {
                if (entry.key == targetKey) {
                    return entry.node
                }
            }
            return null
        }
    }

    /**
     * Exact-match append-or-return under the bucket lock.
     *
     * This is the bucket-local linearization point for exact-key publication.
     */
    fun putIfAbsentOrGet(
        key: PlanCacheKey,
        node: CanonicalPlanNode,
    ): PutResult {
        synchronized(lock) {
            for (entry in entries) {
                if (entry.key == key) {
                    return PutResult(
                        winner = entry.node,
                        inserted = false,
                    )
                }
            }

            entries.add(
                Entry(
                    key = key,
                    node = node,
                    approxBytes = 0,
                ),
            )

            return PutResult(
                winner = node,
                inserted = true,
            )
        }
    }
}

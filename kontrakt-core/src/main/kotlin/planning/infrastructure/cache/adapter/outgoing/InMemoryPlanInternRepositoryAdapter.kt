package planning.infrastructure.cache.adapter.outgoing

import ir.plan.signature.PlanCacheKey
import planning.domain.port.outgoing.PlanInternRepository
import planning.domain.port.outgoing.PlanInternResult
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory Tier-2 adapter.
 *
 * Top-level routing is intentionally partition-first:
 * regions: ConcurrentHashMap<PartitionId, PartitionRegion>
 *
 * This map is not the hot routing surface.
 * Hot-path routing by 64-bit plan keys is delegated to primitive shard-local tables.
 */
class InMemoryPlanInternRepositoryAdapter(
    private val maxEntriesPerPartition: Long = 1_000_000L,
    private val shardCount: Int = 16,
    private val bucketTableCapacity: Int = 1 shl 16,
    private val inflightTableCapacity: Int = 1 shl 10,
    private val maxJoinPolls: Int = 128,
    private val joinPollNanos: Long = 50_000L,
) : PlanInternRepository {

    private val regions = ConcurrentHashMap<PartitionId, PartitionRegion>()

    init {
        if (maxEntriesPerPartition <= 0L) {
            throw IllegalStateException("maxEntriesPerPartition must be positive.")
        }
        if (shardCount <= 0 || shardCount.countOneBits() != 1) {
            throw IllegalStateException("shardCount must be a positive power-of-two.")
        }
        if (bucketTableCapacity <= 0) {
            throw IllegalStateException("bucketTableCapacity must be positive.")
        }
        if (inflightTableCapacity <= 0) {
            throw IllegalStateException("inflightTableCapacity must be positive.")
        }
        if (maxJoinPolls <= 0) {
            throw IllegalStateException("maxJoinPolls must be positive.")
        }
        if (joinPollNanos < 0L) {
            throw IllegalStateException("joinPollNanos must be >= 0.")
        }
    }

    override fun resolveOrIntern(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
    ): PlanInternResult {
        session.step(CostCenter.L2_REGION_LOOKUP)

        val routeKeyBits = PlanCacheRouteKeyDeriver.derive(key, session)

        val region = regions.computeIfAbsent(partitionId) {
            PartitionRegion(
                id = it,
                maxEntries = maxEntriesPerPartition,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )
        }

        return region.resolveOrIntern(
            key = key,
            routeKeyBits = routeKeyBits,
            session = session,
        )
    }

    /**
     * Administrative bulk reclamation for a single partition.
     *
     * Strict drop sequence:
     * 1. seal the region against new entrants
     * 2. wake all in-flight waiters exceptionally
     * 3. remove the region from the registry
     */
    fun dropPartition(partitionId: PartitionId) {
        val region = regions[partitionId] ?: return

        region.close()
        region.abortAllInFlight()

        /*
         * Remove by identity to avoid deleting a freshly recreated region
         * if a new request races after close.
         */
        regions.remove(partitionId, region)
    }
}
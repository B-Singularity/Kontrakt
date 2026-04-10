package planning.infrastructure.cache.adapter.outgoing

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.interner.PlanCacheKey
import planning.domain.port.outgoing.PlanInternRepository
import planning.domain.port.outgoing.PlanInternStep
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
 * This registry is not the hot routing surface.
 * Hot-path routing by 64-bit plan keys is delegated to shard-local primitive tables.
 *
 * This adapter is a Planning-protocol-specific outbound adapter.
 * Therefore, issuance-time structural violations are treated as
 * protocol-integrity failures and fail closed.
 */
class InMemoryPlanInternRepositoryAdapter private constructor(
    private val maxEntriesPerPartition: Long,
    private val shardCount: Int,
    private val bucketTableCapacity: Int,
    private val inflightTableCapacity: Int,
    private val maxJoinPolls: Int,
    private val joinPollNanos: Long,
) : PlanInternRepository {

    private val regions = ConcurrentHashMap<PartitionId, PartitionRegion>()

    override fun resolveOrIntern(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
    ): PlanInternStep {
        session.step(CostCenter.L2_REGION_LOOKUP)

        val routeKeyBits = key.route64

        val region = regions.computeIfAbsent(partitionId) {
            PartitionRegion.issue(
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

    companion object {
        @JvmStatic
        fun issue(
            maxEntriesPerPartition: Long = 1_000_000L,
            shardCount: Int = 16,
            bucketTableCapacity: Int = 1 shl 16,
            inflightTableCapacity: Int = 1 shl 10,
            maxJoinPolls: Int = 128,
            joinPollNanos: Long = 50_000L,
        ): InMemoryPlanInternRepositoryAdapter {
            validate(
                maxEntriesPerPartition = maxEntriesPerPartition,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )

            return InMemoryPlanInternRepositoryAdapter(
                maxEntriesPerPartition = maxEntriesPerPartition,
                shardCount = shardCount,
                bucketTableCapacity = bucketTableCapacity,
                inflightTableCapacity = inflightTableCapacity,
                maxJoinPolls = maxJoinPolls,
                joinPollNanos = joinPollNanos,
            )
        }

        private fun validate(
            maxEntriesPerPartition: Long,
            shardCount: Int,
            bucketTableCapacity: Int,
            inflightTableCapacity: Int,
            maxJoinPolls: Int,
            joinPollNanos: Long,
        ) {
            if (maxEntriesPerPartition <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.maxEntriesPerPartition must be positive: $maxEntriesPerPartition"
                )
            }

            if (shardCount <= 0 || shardCount.countOneBits() != 1) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.shardCount must be a positive power-of-two: $shardCount"
                )
            }

            if (bucketTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.bucketTableCapacity must be positive: $bucketTableCapacity"
                )
            }

            if (inflightTableCapacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.inflightTableCapacity must be positive: $inflightTableCapacity"
                )
            }

            if (maxJoinPolls <= 0) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.maxJoinPolls must be positive: $maxJoinPolls"
                )
            }

            if (joinPollNanos < 0L) {
                throw PlanningProtocolIntegrityException(
                    "InMemoryPlanInternRepositoryAdapter.joinPollNanos must be >= 0: $joinPollNanos"
                )
            }
        }
    }
}
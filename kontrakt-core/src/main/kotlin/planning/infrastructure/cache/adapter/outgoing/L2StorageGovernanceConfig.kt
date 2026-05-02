package planning.infrastructure.cache.adapter.outgoing

import planning.domain.session.policy.ResolvedStorageGovernance

/**
 * Adapter-local immutable bridge for L2 storage governance.
 *
 * This bridge exists because storage governance is a distinct runtime-policy
 * contract from both:
 * - planner-core structural sizing
 * - waiter/join governance
 */
class L2StorageGovernanceConfig private constructor(
    val maxApproxBytes: Long,
    val maxEntries: Int,
    val circuitOpenOnExhaustion: Boolean,
) {
    companion object {
        @JvmStatic
        fun from(policy: ResolvedStorageGovernance): L2StorageGovernanceConfig =
            L2StorageGovernanceConfig(
                maxApproxBytes = policy.maxApproxBytesPerPartition,
                maxEntries = policy.maxEntriesPerPartition,
                circuitOpenOnExhaustion = policy.circuitOpenOnStorageExhaustion,
            )
    }
}

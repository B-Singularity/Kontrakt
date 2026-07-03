package realization.runtime.cache

import migration.quarantine.ResolvedJoinGovernance

/**
 * Adapter-local immutable bridge for join/wait governance.
 *
 * This bridge keeps adapter mechanics decoupled from the policy snapshot family
 * while preserving session-fixed resolved values.
 */
class L2GovernanceConfig private constructor(
    val joinWaitTimeoutNanos: Long,
    val maxWaitersPerKey: Int,
    val maxSpeculativeBuildersPerKey: Int,
    val failFastOnQuotaExhaustion: Boolean,
) {
    companion object {
        @JvmStatic
        fun from(policy: ResolvedJoinGovernance): L2GovernanceConfig =
            L2GovernanceConfig(
                joinWaitTimeoutNanos = policy.joinWaitTimeoutNanos,
                maxWaitersPerKey = policy.maxWaitersPerKey,
                maxSpeculativeBuildersPerKey = policy.maxSpeculativeBuildersPerKey,
                failFastOnQuotaExhaustion = policy.failFastOnQuotaExhaustion,
            )
    }
}

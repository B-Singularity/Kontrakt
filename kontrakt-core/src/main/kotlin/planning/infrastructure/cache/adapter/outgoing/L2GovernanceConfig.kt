package planning.infrastructure.cache.adapter.outgoing

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.session.policy.ResolvedJoinGovernance

/**
 * Adapter-local immutable config derived from ResolvedJoinGovernance.
 *
 * This type is intentionally factory-issued:
 * - no public primary constructor
 * - no data-class copy() backdoor
 *
 * It remains adapter-local and preserves one-to-one semantic meaning with
 * the resolved governance contract consumed at runtime.
 */
internal class L2GovernanceConfig private constructor(
    val joinWaitTimeoutNanos: Long,
    val maxWaitersPerKey: Int,
    val maxSpeculativeBuildersPerKey: Int,
    val failFastOnQuotaExhaustion: Boolean,
) {

    override fun toString(): String {
        return "L2GovernanceConfig(" +
                "joinWaitTimeoutNanos=$joinWaitTimeoutNanos, " +
                "maxWaitersPerKey=$maxWaitersPerKey, " +
                "maxSpeculativeBuildersPerKey=$maxSpeculativeBuildersPerKey, " +
                "failFastOnQuotaExhaustion=$failFastOnQuotaExhaustion" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is L2GovernanceConfig) return false

        return joinWaitTimeoutNanos == other.joinWaitTimeoutNanos &&
                maxWaitersPerKey == other.maxWaitersPerKey &&
                maxSpeculativeBuildersPerKey == other.maxSpeculativeBuildersPerKey &&
                failFastOnQuotaExhaustion == other.failFastOnQuotaExhaustion
    }

    override fun hashCode(): Int {
        var result = joinWaitTimeoutNanos.hashCode()
        result = 31 * result + maxWaitersPerKey
        result = 31 * result + maxSpeculativeBuildersPerKey
        result = 31 * result + failFastOnQuotaExhaustion.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun issue(
            joinWaitTimeoutNanos: Long,
            maxWaitersPerKey: Int,
            maxSpeculativeBuildersPerKey: Int,
            failFastOnQuotaExhaustion: Boolean,
        ): L2GovernanceConfig {
            validate(
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
                maxWaitersPerKey = maxWaitersPerKey,
                maxSpeculativeBuildersPerKey = maxSpeculativeBuildersPerKey,
            )

            return L2GovernanceConfig(
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
                maxWaitersPerKey = maxWaitersPerKey,
                maxSpeculativeBuildersPerKey = maxSpeculativeBuildersPerKey,
                failFastOnQuotaExhaustion = failFastOnQuotaExhaustion,
            )
        }

        @JvmStatic
        fun fromResolved(resolved: ResolvedJoinGovernance): L2GovernanceConfig {
            return issue(
                joinWaitTimeoutNanos = resolved.joinWaitTimeoutNanos,
                maxWaitersPerKey = resolved.maxWaitersPerKey,
                maxSpeculativeBuildersPerKey = resolved.maxSpeculativeBuildersPerKey,
                failFastOnQuotaExhaustion = resolved.failFastOnQuotaExhaustion,
            )
        }

        private fun validate(
            joinWaitTimeoutNanos: Long,
            maxWaitersPerKey: Int,
            maxSpeculativeBuildersPerKey: Int,
        ) {
            if (joinWaitTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "L2GovernanceConfig.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos"
                )
            }
            if (maxWaitersPerKey <= 0) {
                throw PlanningProtocolIntegrityException(
                    "L2GovernanceConfig.maxWaitersPerKey must be > 0: $maxWaitersPerKey"
                )
            }
            if (maxSpeculativeBuildersPerKey < 0) {
                throw PlanningProtocolIntegrityException(
                    "L2GovernanceConfig.maxSpeculativeBuildersPerKey must be >= 0: $maxSpeculativeBuildersPerKey"
                )
            }
        }
    }
}
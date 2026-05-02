package planning.domain.session.policy

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Immutable snapshot of L2 waiter/join governance.
 *
 * This contract is intentionally independent from L1 worker-byte profile scaling.
 * It governs bounded waiting, admission, and degrade behavior only.
 *
 * Design rules:
 * - Factory-issued only.
 * - No data-class copy() backdoor.
 * - Timeout is a monotonic elapsed-time waiter deadline.
 * - Timeout/cancellation are waiter lifecycle events and MUST NOT fail the shared slot.
 *
 * ADR alignment:
 * - ADR-0032: L2 governance boundary distinct from L1 structural sizing.
 * - ADR-0033: profile-independent bootstrap join governance for v1.
 */
class ResolvedJoinGovernance private constructor(
    val joinWaitTimeoutNanos: Long,
    val maxWaitersPerKey: Int,
    val maxSpeculativeBuildersPerKey: Int,
    val failFastOnQuotaExhaustion: Boolean,
) {
    companion object {
        @JvmStatic
        fun issue(
            joinWaitTimeoutNanos: Long,
            maxWaitersPerKey: Int,
            maxSpeculativeBuildersPerKey: Int,
            failFastOnQuotaExhaustion: Boolean,
        ): ResolvedJoinGovernance {
            if (joinWaitTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedJoinGovernance.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos",
                )
            }
            if (maxWaitersPerKey <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedJoinGovernance.maxWaitersPerKey must be > 0: $maxWaitersPerKey",
                )
            }
            if (maxSpeculativeBuildersPerKey < 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedJoinGovernance.maxSpeculativeBuildersPerKey must be >= 0: $maxSpeculativeBuildersPerKey",
                )
            }

            return ResolvedJoinGovernance(
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
                maxWaitersPerKey = maxWaitersPerKey,
                maxSpeculativeBuildersPerKey = maxSpeculativeBuildersPerKey,
                failFastOnQuotaExhaustion = failFastOnQuotaExhaustion,
            )
        }
    }
}

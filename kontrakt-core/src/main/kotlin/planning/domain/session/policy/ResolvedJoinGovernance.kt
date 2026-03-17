package planning.domain.session.policy

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Governs L2 join / wait / degrade behavior.
 *
 * Normative:
 * - planner-budget-resolution-and-worker-lifecycle.md, Section 4.4
 * - l2-plan-interner-partitioned-tier2-with-governance.md (AMENDED)
 *
 * These policies may alter latency, throughput, reuse rate, and survivability.
 * They MUST NOT alter semantic output.
 */
data class ResolvedJoinGovernance(
    /**
     * Monotonic elapsed-time deadline for in-flight join waiting.
     *
     * This is interpreted as a waiter-lifecycle event, not as shared-slot failure.
     */
    val joinWaitTimeoutNanos: Long,

    /** Maximum number of waiters that may attach to one in-flight key. */
    val maxWaitersPerKey: Int,

    /** Maximum speculative builders allowed after waiter timeout. */
    val maxSpeculativeBuildersPerKey: Int,

    /** If true, fail fast or bypass immediately when speculative quota is exhausted. */
    val failFastOnQuotaExhaustion: Boolean,
) {
    init {
        if (joinWaitTimeoutNanos <= 0L) {
            throw PlanningProtocolIntegrityException(
                "ResolvedJoinGovernance.joinWaitTimeoutNanos must be > 0: $joinWaitTimeoutNanos"
            )
        }
        if (maxWaitersPerKey <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedJoinGovernance.maxWaitersPerKey must be > 0: $maxWaitersPerKey"
            )
        }
        if (maxSpeculativeBuildersPerKey < 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedJoinGovernance.maxSpeculativeBuildersPerKey must be >= 0: $maxSpeculativeBuildersPerKey"
            )
        }
    }
}
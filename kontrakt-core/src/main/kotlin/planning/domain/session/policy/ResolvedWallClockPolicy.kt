package planning.domain.session.policy

// ResolvedWallClockPolicy.kt

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Optional runtime-boundary watchdog policy for total session elapsed time.
 *
 * Normative:
 * - planner-budget-resolution-and-worker-lifecycle.md, Section 18 (AMENDED)
 *
 * This policy is intentionally separate from:
 * - structural byte budgets
 * - step/fuel semantics
 * - L2 waiter timeout semantics
 */
data class ResolvedWallClockPolicy(
    val maxSessionElapsedNanos: Long,
) {
    init {
        if (maxSessionElapsedNanos <= 0L) {
            throw PlanningProtocolIntegrityException(
                "ResolvedWallClockPolicy.maxSessionElapsedNanos must be > 0: $maxSessionElapsedNanos"
            )
        }
    }
}
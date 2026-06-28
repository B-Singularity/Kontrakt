package governance.policy

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Optional runtime-boundary wall-clock watchdog policy.
 *
 * This contract is deliberately separated from:
 * - ResolvedSessionBudget
 * - ResolvedJoinGovernance
 * - ResolvedStorageGovernance
 * - step(costCenter) semantics
 *
 * It exists only if the runtime chooses to install a session-level elapsed-time watchdog.
 */
class ResolvedWallClockPolicy private constructor(
    val maxSessionElapsedNanos: Long,
) {
    companion object {
        @JvmStatic
        fun issue(maxSessionElapsedNanos: Long): ResolvedWallClockPolicy {
            if (maxSessionElapsedNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedWallClockPolicy.maxSessionElapsedNanos must be > 0: $maxSessionElapsedNanos",
                )
            }

            return ResolvedWallClockPolicy(
                maxSessionElapsedNanos = maxSessionElapsedNanos,
            )
        }
    }
}

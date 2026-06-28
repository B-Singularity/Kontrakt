package planning.domain.runtime.orchestration

import governance.policy.ResolvedSessionBudget
import stage.lowering.diagnostics.CapacityExceededException
import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Immutable run-scoped remaining execution budget ledger.
 *
 * This is intentionally narrower than the full runtime-policy snapshot.
 *
 * It owns only the carry-forward execution counters that can be depleted across
 * fresh-session restart:
 * - remainingPhysicalSteps
 * - remainingSemanticWorkUnits
 *
 * It does NOT own:
 * - planner bytes
 * - signature/headroom policy
 * - join/storage/wall-clock governance
 */
class PlanningRunRemainingBudget private constructor(
    val remainingPhysicalSteps: Int,
    val remainingSemanticWorkUnits: Int,
) {
    fun debitPhysicalSteps(units: Int): PlanningRunRemainingBudget {
        if (units <= 0) {
            throw PlanningProtocolIntegrityException(
                "PlanningRunRemainingBudget.debitPhysicalSteps requires units > 0: $units",
            )
        }
        if (units > remainingPhysicalSteps) {
            throw CapacityExceededException(
                limitType = "RUN_SCOPED_PHYSICAL_STEPS",
                value = units.toLong(),
            )
        }

        return PlanningRunRemainingBudget(
            remainingPhysicalSteps = remainingPhysicalSteps - units,
            remainingSemanticWorkUnits = remainingSemanticWorkUnits,
        )
    }

    fun debitSemanticWorkUnits(units: Int): PlanningRunRemainingBudget {
        if (units <= 0) {
            throw PlanningProtocolIntegrityException(
                "PlanningRunRemainingBudget.debitSemanticWorkUnits requires units > 0: $units",
            )
        }
        if (units > remainingSemanticWorkUnits) {
            throw CapacityExceededException(
                limitType = "RUN_SCOPED_SEMANTIC_WORK_UNITS",
                value = units.toLong(),
            )
        }

        return PlanningRunRemainingBudget(
            remainingPhysicalSteps = remainingPhysicalSteps,
            remainingSemanticWorkUnits = remainingSemanticWorkUnits - units,
        )
    }

    fun hasRemainingPhysicalSteps(): Boolean = remainingPhysicalSteps > 0

    override fun toString(): String =
        "PlanningRunRemainingBudget(remainingPhysicalSteps=$remainingPhysicalSteps, remainingSemanticWorkUnits=$remainingSemanticWorkUnits)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanningRunRemainingBudget) return false

        return remainingPhysicalSteps == other.remainingPhysicalSteps &&
                remainingSemanticWorkUnits == other.remainingSemanticWorkUnits
    }

    override fun hashCode(): Int {
        var result = remainingPhysicalSteps
        result = 31 * result + remainingSemanticWorkUnits
        return result
    }

    companion object {
        @JvmStatic
        fun issue(
            remainingPhysicalSteps: Int,
            remainingSemanticWorkUnits: Int,
        ): PlanningRunRemainingBudget {
            if (remainingPhysicalSteps < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlanningRunRemainingBudget.remainingPhysicalSteps must be >= 0: $remainingPhysicalSteps",
                )
            }
            if (remainingSemanticWorkUnits < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlanningRunRemainingBudget.remainingSemanticWorkUnits must be >= 0: $remainingSemanticWorkUnits",
                )
            }

            return PlanningRunRemainingBudget(
                remainingPhysicalSteps = remainingPhysicalSteps,
                remainingSemanticWorkUnits = remainingSemanticWorkUnits,
            )
        }

        @JvmStatic
        fun issueFrom(sessionBudget: ResolvedSessionBudget): PlanningRunRemainingBudget =
            issue(
                remainingPhysicalSteps = sessionBudget.maxPhysicalSteps,
                remainingSemanticWorkUnits = sessionBudget.maxSemanticWorkUnits,
            )
    }
}

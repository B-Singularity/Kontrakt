package realization.runtime.orchestration

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import statemachine.transition.material.move.PlanningRunEpoch

/**
 * Runtime-boundary lease proving which worker-local session currently owns
 * execution for one PlanningRunEpoch.
 *
 * This is not a worker-backing epoch and not a policy epoch.
 * It is a run-local admission/ownership token.
 *
 * Exactly one live lease may exist for a run while it is in RUNNING state.
 */
class PlanningRunWorkerSessionLease private constructor(
    val runEpoch: PlanningRunEpoch,
    val ordinal: Long,
) {
    override fun toString(): String = "PlanningRunWorkerSessionLease(runEpoch=$runEpoch, ordinal=$ordinal)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanningRunWorkerSessionLease) return false

        return runEpoch == other.runEpoch && ordinal == other.ordinal
    }

    override fun hashCode(): Int {
        var result = runEpoch.hashCode()
        result = 31 * result + ordinal.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun issue(
            runEpoch: PlanningRunEpoch,
            ordinal: Long,
        ): PlanningRunWorkerSessionLease {
            if (ordinal <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "PlanningRunWorkerSessionLease.ordinal must be > 0: $ordinal",
                )
            }

            return PlanningRunWorkerSessionLease(
                runEpoch = runEpoch,
                ordinal = ordinal,
            )
        }
    }
}

package statemachine.transition.contract

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import statemachine.state.material.PlanningRunState

/**
 * Normative transition law for PlanningRunState.
 *
 * This object exists so that orchestration legality is not silently encoded
 * ad hoc across multiple runtime-boundary branches.
 */
object PlanningRunLifecycleLaw {
    @JvmStatic
    fun requireTransition(
        from: PlanningRunState,
        to: PlanningRunState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal PlanningRunState transition: $from -> $to",
            )
        }
    }

    @JvmStatic
    fun canTransition(
        from: PlanningRunState,
        to: PlanningRunState,
    ): Boolean =
        when (from) {
            PlanningRunState.INITIALIZED ->
                to == PlanningRunState.RUNNING ||
                        to == PlanningRunState.ABORTED ||
                        to == PlanningRunState.PANIC_ISOLATED

            PlanningRunState.RUNNING ->
                to == PlanningRunState.SUSPENDED_ON_JOIN ||
                        to == PlanningRunState.COMPLETED ||
                        to == PlanningRunState.ABORTED ||
                        to == PlanningRunState.PANIC_ISOLATED

            PlanningRunState.SUSPENDED_ON_JOIN ->
                to == PlanningRunState.READY_TO_RESTART ||
                        to == PlanningRunState.ABORTED ||
                        to == PlanningRunState.PANIC_ISOLATED

            PlanningRunState.READY_TO_RESTART ->
                to == PlanningRunState.RUNNING ||
                        to == PlanningRunState.ABORTED ||
                        to == PlanningRunState.PANIC_ISOLATED

            PlanningRunState.COMPLETED,
            PlanningRunState.ABORTED,
            PlanningRunState.PANIC_ISOLATED,
                -> false
        }
}

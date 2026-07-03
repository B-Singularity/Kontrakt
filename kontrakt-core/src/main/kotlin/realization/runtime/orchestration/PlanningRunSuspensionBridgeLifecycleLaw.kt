package realization.runtime.orchestration

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import statemachine.state.material.condition.PlanningRunSuspensionBridgeState

/**
 * Transition law for the runtime-boundary joined-wait bridge lifecycle.
 *
 * This law keeps the bridge explicit and closed instead of scattering legality
 * across ad hoc boolean/CAS checks.
 */
object PlanningRunSuspensionBridgeLifecycleLaw {
    @JvmStatic
    fun canTransition(
        from: PlanningRunSuspensionBridgeState,
        to: PlanningRunSuspensionBridgeState,
    ): Boolean =
        when (from) {
            PlanningRunSuspensionBridgeState.INITIAL ->
                to == PlanningRunSuspensionBridgeState.CALLBACK_REGISTERED ||
                        to == PlanningRunSuspensionBridgeState.CANCELLED

            PlanningRunSuspensionBridgeState.CALLBACK_REGISTERED ->
                to == PlanningRunSuspensionBridgeState.READY_PUBLISHED ||
                        to == PlanningRunSuspensionBridgeState.CANCELLED

            PlanningRunSuspensionBridgeState.READY_PUBLISHED ->
                to == PlanningRunSuspensionBridgeState.CONSUME_PERMITTED

            PlanningRunSuspensionBridgeState.CONSUME_PERMITTED ->
                to == PlanningRunSuspensionBridgeState.CONSUMING

            PlanningRunSuspensionBridgeState.CONSUMING ->
                to == PlanningRunSuspensionBridgeState.CONSUMED

            PlanningRunSuspensionBridgeState.CONSUMED,
            PlanningRunSuspensionBridgeState.CANCELLED,
                ->
                false
        }

    @JvmStatic
    fun requireTransition(
        from: PlanningRunSuspensionBridgeState,
        to: PlanningRunSuspensionBridgeState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal PlanningRunSuspensionBridgeState transition: $from -> $to",
            )
        }
    }

    @JvmStatic
    fun requireInitialForCallbackRegistration(state: PlanningRunSuspensionBridgeState) {
        if (state != PlanningRunSuspensionBridgeState.INITIAL) {
            throw PlanningProtocolIntegrityException(
                "Callback registration requires INITIAL bridge state: $state",
            )
        }
    }

    @JvmStatic
    fun canPublishReadiness(state: PlanningRunSuspensionBridgeState): Boolean =
        state == PlanningRunSuspensionBridgeState.CALLBACK_REGISTERED

    @JvmStatic
    fun requireReadyPublishedForConsumePermit(state: PlanningRunSuspensionBridgeState) {
        if (state != PlanningRunSuspensionBridgeState.READY_PUBLISHED) {
            throw PlanningProtocolIntegrityException(
                "Fresh-session consume permit requires READY_PUBLISHED bridge state: $state",
            )
        }
    }

    @JvmStatic
    fun requireConsumePermittedForConsumeStart(state: PlanningRunSuspensionBridgeState) {
        if (state != PlanningRunSuspensionBridgeState.CONSUME_PERMITTED) {
            throw PlanningProtocolIntegrityException(
                "Ready-result consume requires CONSUME_PERMITTED bridge state: $state",
            )
        }
    }

    /**
     * Best-effort cancellation is lawful only before readiness publication.
     *
     * Once readiness has been published, cancellation does not revoke that
     * already-published readiness retroactively.
     */
    @JvmStatic
    fun canTransitionToCancelled(state: PlanningRunSuspensionBridgeState): Boolean =
        state == PlanningRunSuspensionBridgeState.INITIAL ||
                state == PlanningRunSuspensionBridgeState.CALLBACK_REGISTERED
}
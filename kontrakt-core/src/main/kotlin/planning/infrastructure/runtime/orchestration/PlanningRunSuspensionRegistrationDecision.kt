package planning.infrastructure.runtime.orchestration

/**
 * Registration result for one runtime-boundary suspension handle.
 *
 * This is intentionally separate from the lower-tier JoinRegistrationDecision
 * because the runtime boundary should speak in terms of planning-run orchestration,
 * not raw adapter join mechanics.
 */
enum class PlanningRunSuspensionRegistrationDecision {
    /**
     * Ready callback registration completed and future readiness will arrive
     * asynchronously through the registered callback.
     */
    REGISTERED,

    /**
     * Readiness was already available at registration time and the runtime may
     * transition to READY_TO_RESTART immediately.
     */
    ALREADY_READY,
}
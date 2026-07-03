package realization.planning.expansion

import stage.canonicalization.material.representation.TypeReference

/**
 * Per-expansion work-meter bridge.
 *
 * The pipeline does not own PlannerSession directly.
 * Instead, StructuralPlannerCore passes a run/session-bound meter that translates
 * these expansion events into ratified CostCenter values and then calls the
 * session's authoritative accounting gate.
 *
 * The subject is included deliberately even though the initial session-bound
 * implementation only needs the event. It preserves an extensibility point for
 * deterministic diagnostics, structured trace, and future per-subject metering
 * without changing the pipeline contract.
 */
fun interface TypeExpansionWorkMeter {
    fun record(
        event: TypeExpansionWorkEvent,
        subject: TypeReference,
    )
}

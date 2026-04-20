package planning.domain.expansion

import metamodel.domain.vo.TypeReference

/**
 * Per-expansion work-meter bridge.
 *
 * The pipeline does not own PlannerSession directly. Instead, StructuralPlannerCore
 * passes a run/session-bound meter that translates these expansion events into
 * the ratified cost centers and physical/semantic budget debits.
 *
 * This prevents TypeExpansionPipeline from becoming session orchestration while
 * still making accounting mandatory at every expansion stage.
 */
fun interface TypeExpansionWorkMeter {

    fun record(
        event: TypeExpansionWorkEvent,
        subject: TypeReference,
    )
}
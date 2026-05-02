package planning.domain.expansion

import planning.domain.protocol.CostCenter

/**
 * Single mapping authority from TypeExpansionWorkEvent to CostCenter.
 *
 * TypeExpansionWorkEvent is the stage vocabulary.
 * CostCenter is the budget protocol vocabulary.
 *
 * Keeping the mapping here prevents TypeExpansionPipeline from directly owning
 * PlannerSession metering mutation while still making the event-to-cost mapping
 * explicit and exhaustive.
 */
object TypeExpansionCostCenterMapper {
    fun map(event: TypeExpansionWorkEvent): CostCenter =
        when (event) {
            TypeExpansionWorkEvent.TYPE_SHAPE_RESOLUTION ->
                CostCenter.TYPE_SHAPE_RESOLUTION

            TypeExpansionWorkEvent.TYPE_SHAPE_LOWERING ->
                CostCenter.TYPE_SHAPE_LOWERING

            TypeExpansionWorkEvent.TYPE_CYCLE_IDENTITY_RESOLUTION ->
                CostCenter.TYPE_CYCLE_IDENTITY_RESOLUTION

            TypeExpansionWorkEvent.TYPE_CYCLE_IDENTITY_CONTINUITY_CHECK ->
                CostCenter.TYPE_CYCLE_IDENTITY_CONTINUITY_CHECK

            TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_CACHE_HIT ->
                CostCenter.COMPOSITE_RAW_FACT_CACHE_HIT

            TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_RESOLVE ->
                CostCenter.COMPOSITE_RAW_FACT_RESOLVE

            TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK ->
                CostCenter.COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK

            TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_PROJECTION ->
                CostCenter.COMPOSITE_ACTIVE_MEMBER_PROJECTION

            TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_ORDERING ->
                CostCenter.COMPOSITE_ACTIVE_MEMBER_ORDERING

            TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION ->
                CostCenter.CONTAINER_EXPANSION_DECISION

            TypeExpansionWorkEvent.ATOMIC_EXPANSION_DECISION ->
                CostCenter.ATOMIC_EXPANSION_DECISION
        }
}

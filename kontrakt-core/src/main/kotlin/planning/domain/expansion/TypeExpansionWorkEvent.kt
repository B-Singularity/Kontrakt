package planning.domain.expansion

/**
 * Closed work-event vocabulary for type-expansion metering.
 *
 * This is not telemetry.
 * This is the domain-visible accounting surface that StructuralPlannerCore can
 * bridge to PlannerSession.step(...) or another session budget ledger.
 */
enum class TypeExpansionWorkEvent {
    TYPE_SHAPE_RESOLUTION,
    TYPE_SHAPE_LOWERING,

    COMPOSITE_RAW_FACT_RESOLUTION,
    COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK,
    COMPOSITE_ACTIVE_MEMBER_PROJECTION,
    COMPOSITE_ACTIVE_MEMBER_ORDERING,
    COMPOSITE_EXPANSION_PLAN_ISSUE,

    CONTAINER_EXPANSION_DECISION,
    ATOMIC_EXPANSION_DECISION,
    INTERFACE_EXPANSION_DECISION,
}
package planning.domain.expansion

/**
 * Closed work-event vocabulary for type-expansion metering.
 *
 * This is not telemetry.
 * This is the domain-visible accounting surface that StructuralPlannerCore can
 * bridge to PlannerSession.step(...) through a session-bound work meter.
 *
 * The event is recorded after the corresponding stage succeeds.
 *
 * This vocabulary intentionally does not include:
 * - COMPOSITE_EXPANSION_PLAN_ISSUE
 * - INTERFACE_EXPANSION_DECISION
 *
 * Reasons:
 * - plan issuance is currently covered by projection/order + dispatch/lowering cost;
 * - interface implementation-resolution is not yet an executable planner path.
 */
enum class TypeExpansionWorkEvent {
    TYPE_SHAPE_RESOLUTION,
    TYPE_SHAPE_LOWERING,

    COMPOSITE_RAW_FACT_CACHE_HIT,
    COMPOSITE_RAW_FACT_RESOLVE,
    COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK,
    COMPOSITE_ACTIVE_MEMBER_PROJECTION,
    COMPOSITE_ACTIVE_MEMBER_ORDERING,

    CONTAINER_EXPANSION_DECISION,
    ATOMIC_EXPANSION_DECISION,
}
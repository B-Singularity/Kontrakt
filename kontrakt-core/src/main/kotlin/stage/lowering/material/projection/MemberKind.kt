package stage.lowering.material.projection

/**
 * Planner-semantic member kind after Active Member projection.
 *
 * This is not a raw adapter fact.
 * It is assigned by the Core projection algorithm.
 */
enum class MemberKind {
    CTOR_PARAM,
    PROPERTY,
}

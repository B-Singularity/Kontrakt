package planning.domain.interner

/**
 * Closed vocabulary for semantic interner invocation sites inside planner-core.
 *
 * This type exists so that planner/interner handoff diagnostics do not rely on
 * ad hoc string literals.
 *
 * The values here are not lifecycle states.
 * They identify *where* the interner was invoked from.
 */
enum class InternerInvocationSite(
    val diagnosticLabel: String,
) {
    /**
     * Ordinary structural payload interning for a fully assembled passive node.
     */
    ORDINARY_PAYLOAD(
        diagnosticLabel = "ordinary-payload interning",
    ),

    /**
     * Cycle-break payload interning for deterministic breakpoint substitution.
     */
    CYCLE_BREAK(
        diagnosticLabel = "cycle-break interning",
    ),
}
package planning.domain.session.policy

// ResolvedRuntimePolicy.kt

/**
 * Top-level immutable policy bundle installed at session bootstrap.
 *
 * Normative:
 * - planner-budget-resolution-and-worker-lifecycle.md, Section 4.7
 *
 * The bundle keeps structural/runtime budget, L2 join governance, and optional
 * wall-clock watchdog policy separate but compatible.
 */
data class ResolvedRuntimePolicy(
    val sessionBudget: ResolvedSessionBudget,
    val joinGovernance: ResolvedJoinGovernance,
    val wallClockPolicy: ResolvedWallClockPolicy? = null,
)
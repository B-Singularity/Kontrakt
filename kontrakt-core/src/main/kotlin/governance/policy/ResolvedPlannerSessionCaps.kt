package governance.policy

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Concrete capacity output derived from the resolved structural budget.
 *
 * Normative:
 * - planner-budget-resolution-and-worker-lifecycle.md, Section 6.1
 *
 * These caps are consumed directly by worker-local L1 structures.
 */
data class ResolvedPlannerSessionCaps(
    val maxNodeIdCap: Int,
    val maxDepthCap: Int,
    val indexerTableCap: Int,
    val undoLogCap: Int,
    val maxSignatureBytes: Int,
    val structBudgetBytes: Long,
) {
    init {
        if (maxNodeIdCap <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.maxNodeIdCap must be > 0: $maxNodeIdCap",
            )
        }
        if (maxDepthCap <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.maxDepthCap must be > 0: $maxDepthCap",
            )
        }
        if (maxDepthCap > Int.MAX_VALUE - 2) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.maxDepthCap must be <= Int.MAX_VALUE - 2: $maxDepthCap",
            )
        }
        if (indexerTableCap <= 0 || indexerTableCap.countOneBits() != 1) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.indexerTableCap must be a positive power-of-two: $indexerTableCap",
            )
        }
        if (undoLogCap <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.undoLogCap must be > 0: $undoLogCap",
            )
        }
        if (maxSignatureBytes <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.maxSignatureBytes must be > 0: $maxSignatureBytes",
            )
        }
        if (structBudgetBytes <= 0L) {
            throw PlanningProtocolIntegrityException(
                "ResolvedPlannerSessionCaps.structBudgetBytes must be > 0: $structBudgetBytes",
            )
        }
    }
}

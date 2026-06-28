package governance.policy

/**
 * Deterministic solver that translates resolved numeric budget into concrete L1 caps.
 *
 * Normative:
 * - planner-budget-resolution-and-worker-lifecycle.md, Section 7
 * - l1-planner-session-primitive-data-structures.md (AMENDED)
 *
 * Identical input budget + identical internal calibration MUST produce identical output caps.
 */
interface PlannerCapacityResolver {
    fun resolve(budget: ResolvedSessionBudget): ResolvedPlannerSessionCaps
}

/**
 * Internal sizing calibration used only by the capacity resolver.
 *
 * This is NOT part of the public / cross-boundary contract.
 * These values are policy/calibration defaults, not order SSOT constants.
 */
internal data class ResolvedSizingCalibration(
    val signatureReserveRatio: Double = 0.20,
    val preferredDepthDivisor: Int = 8,
    val undoRecordsPerDepth: Int = 4,
    val secureWipeOnReset: Boolean = false,
)

package planning.domain.session.policy

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Represents the resolved structural/runtime budget for a single planner worker.
 *
 * Normative:
 * - ADR-0032
 * - planner-budget-resolution-and-worker-lifecycle.md, Section 4.1
 *
 * This contract is environment-agnostic.
 * The Domain Core must consume only these already-resolved numeric limits.
 */
data class ResolvedSessionBudget(
    /** Maximum bytes allowed for all worker-local L1 structures. */
    val maxPlannerBytesPerWorker: Long,

    /** Maximum physical steps allowed (monotonic physical counter cap). */
    val maxPhysicalSteps: Int,

    /** Maximum semantic work units allowed (monotonic semantic counter cap). */
    val maxSemanticWorkUnits: Int,

    /** Maximum allowed length for any single canonical signature. */
    val maxSignatureLen: Int,

    /** Reserved headroom for non-ledger runtime overhead. */
    val fixedHeadroomBytes: Long,
) {
    init {
        if (maxPlannerBytesPerWorker <= 0L) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSessionBudget.maxPlannerBytesPerWorker must be > 0: $maxPlannerBytesPerWorker"
            )
        }
        if (maxPhysicalSteps <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSessionBudget.maxPhysicalSteps must be > 0: $maxPhysicalSteps"
            )
        }
        if (maxSemanticWorkUnits <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSessionBudget.maxSemanticWorkUnits must be > 0: $maxSemanticWorkUnits"
            )
        }
        if (maxSignatureLen <= 0) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSessionBudget.maxSignatureLen must be > 0: $maxSignatureLen"
            )
        }
        if (fixedHeadroomBytes < 0L) {
            throw PlanningProtocolIntegrityException(
                "ResolvedSessionBudget.fixedHeadroomBytes must be >= 0: $fixedHeadroomBytes"
            )
        }
    }
}
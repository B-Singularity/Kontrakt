package planning.domain.session.policy

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Immutable snapshot of planner-core structural/runtime budget for a single worker.
 *
 * This snapshot is issued outside the planner core and remains fixed for the
 * lifetime of a session.
 *
 * Design rules:
 * - Factory-issued only (no public constructor).
 * - No data-class copy() backdoor.
 * - Validates only snapshot-integrity invariants.
 * - Does NOT perform structural feasibility checks; those belong to the capacity solver.
 *
 * ADR alignment:
 * - ADR-0032: policy/protocol separation, resolved budget outside core.
 * - ADR-0033: snapshot family integrity, explicit bootstrap constants.
 */
class ResolvedSessionBudget private constructor(
    val maxPlannerBytesPerWorker: Long,
    val maxPhysicalSteps: Int,
    val maxSemanticWorkUnits: Int,
    val maxSignatureLen: Int,
    val fixedHeadroomBytes: Long,
    val physicalStepMultiplier: Int,
    val semanticWorkMultiplier: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxPlannerBytesPerWorker: Long,
            maxPhysicalSteps: Int,
            maxSemanticWorkUnits: Int,
            maxSignatureLen: Int,
            fixedHeadroomBytes: Long,
            physicalStepMultiplier: Int,
            semanticWorkMultiplier: Int,
        ): ResolvedSessionBudget {
            if (maxPlannerBytesPerWorker <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxPlannerBytesPerWorker must be > 0: $maxPlannerBytesPerWorker",
                )
            }
            if (maxPhysicalSteps <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxPhysicalSteps must be > 0: $maxPhysicalSteps",
                )
            }
            if (maxSemanticWorkUnits <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxSemanticWorkUnits must be > 0: $maxSemanticWorkUnits",
                )
            }
            if (maxSignatureLen <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxSignatureLen must be > 0: $maxSignatureLen",
                )
            }
            if (fixedHeadroomBytes < 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.fixedHeadroomBytes must be >= 0: $fixedHeadroomBytes",
                )
            }
            if (physicalStepMultiplier <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.physicalStepMultiplier must be > 0: $physicalStepMultiplier",
                )
            }
            if (semanticWorkMultiplier <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.semanticWorkMultiplier must be > 0: $semanticWorkMultiplier",
                )
            }

            return ResolvedSessionBudget(
                maxPlannerBytesPerWorker = maxPlannerBytesPerWorker,
                maxPhysicalSteps = maxPhysicalSteps,
                maxSemanticWorkUnits = maxSemanticWorkUnits,
                maxSignatureLen = maxSignatureLen,
                fixedHeadroomBytes = fixedHeadroomBytes,
                physicalStepMultiplier = physicalStepMultiplier,
                semanticWorkMultiplier = semanticWorkMultiplier,
            )
        }
    }
}

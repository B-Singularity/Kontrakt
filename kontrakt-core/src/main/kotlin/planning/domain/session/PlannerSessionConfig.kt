package planning.domain.session

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.session.policy.ResolvedPlannerSessionCaps
import planning.domain.session.policy.ResolvedSessionBudget

/**
 * Lean core runtime configuration for PlannerSession.
 *
 * This object is a consumer of already-resolved immutable artifacts:
 * - version tuple
 * - session budget
 * - planner structural caps
 *
 * It MUST NOT perform capacity solving or environment discovery.
 */
class PlannerSessionConfig private constructor(
    val versions: PlannerVersionBundle,
    val budget: ResolvedSessionBudget,
    val caps: ResolvedPlannerSessionCaps,
) {
    companion object {
        @JvmStatic
        fun issue(
            versions: PlannerVersionBundle,
            budget: ResolvedSessionBudget,
            caps: ResolvedPlannerSessionCaps,
        ): PlannerSessionConfig {
            if (caps.maxNodeIdCap <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerSessionConfig.caps.maxNodeIdCap must be > 0: ${caps.maxNodeIdCap}"
                )
            }
            if (caps.maxDepthCap <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerSessionConfig.caps.maxDepthCap must be > 0: ${caps.maxDepthCap}"
                )
            }
            if (caps.indexerTableCap <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerSessionConfig.caps.indexerTableCap must be > 0: ${caps.indexerTableCap}"
                )
            }
            if (caps.undoLogCap <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerSessionConfig.caps.undoLogCap must be > 0: ${caps.undoLogCap}"
                )
            }
            if (caps.maxSignatureBytes < budget.maxSignatureLen) {
                throw PlanningProtocolIntegrityException(
                    "PlannerSessionConfig slab contract violated: " +
                            "caps.maxSignatureBytes (${caps.maxSignatureBytes}) < " +
                            "budget.maxSignatureLen (${budget.maxSignatureLen})"
                )
            }
            if (caps.structBudgetBytes <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "PlannerSessionConfig.caps.structBudgetBytes must be > 0: ${caps.structBudgetBytes}"
                )
            }

            return PlannerSessionConfig(
                versions = versions,
                budget = budget,
                caps = caps,
            )
        }
    }
}
package realization.planning

import stage.input.presentation.dto.MemberFact
import stage.input.presentation.dto.TypeFactsDTO
import stage.lowering.diagnostics.FaultKind

/**
 * Resolves semantic fault attribution from domain-visible evidence.
 *
 * Responsibility:
 * - classify USER vs FRAMEWORK attribution
 * - keep fault-kind policy out of orchestration code
 *
 * This is intentionally modeled as a dedicated domain collaborator so that
 * StructuralPlannerCore does not embed attribution policy inline.
 */
interface FaultKindResolver {
    fun resolveForCollision(
        facts: TypeFactsDTO,
        offendingMembers: List<MemberFact>,
        expectedNormalizationVersion: Long,
    ): FaultKind
}

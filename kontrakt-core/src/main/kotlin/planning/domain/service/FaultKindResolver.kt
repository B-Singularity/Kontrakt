package planning.domain.service

import stage.lowering.diagnostics.FaultKind
import stage.input.material.MemberFact
import stage.input.material.TypeFactsDTO

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

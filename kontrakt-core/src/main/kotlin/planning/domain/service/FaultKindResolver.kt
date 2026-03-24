package planning.domain.service

import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO
import planning.domain.exception.FaultKind

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
package planning.domain.service.derivation

import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO
import planning.domain.vo.ActiveEdgeDescriptor

/**
 * Planner-visible edge semantics.
 *
 * Implementations MUST provide:
 * - stable unsigned 64-bit edge ordering key (`edgeRank`)
 * - traversal disposition
 * - breakpoint stage classification
 */
interface CycleEdgeSemanticsProvider {
    fun describe(
        ownerFacts: TypeFactsDTO,
        member: MemberFact,
    ): ActiveEdgeDescriptor
}

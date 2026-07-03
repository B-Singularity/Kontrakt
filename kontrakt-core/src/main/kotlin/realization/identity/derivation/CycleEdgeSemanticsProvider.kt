package realization.identity.derivation

import stage.input.presentation.dto.MemberFact
import stage.input.presentation.dto.TypeFactsDTO
import stage.lowering.material.ActiveEdgeDescriptor

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

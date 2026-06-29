package realization.identity.derivation

import stage.lowering.material.ActiveEdgeDescriptor
import stage.input.material.MemberFact
import stage.input.material.TypeFactsDTO

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

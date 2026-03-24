package planning.domain.port.outgoing

import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO

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

class ActiveEdgeDescriptor private constructor(
    val edgeRank: Long,
    val traversalDisposition: TraversalDisposition,
    val breakpointStage: BreakpointStage,
) {
    companion object {
        @JvmStatic
        fun issue(
            edgeRank: Long,
            traversalDisposition: TraversalDisposition,
            breakpointStage: BreakpointStage,
        ): ActiveEdgeDescriptor {
            return ActiveEdgeDescriptor(
                edgeRank = edgeRank,
                traversalDisposition = traversalDisposition,
                breakpointStage = breakpointStage,
            )
        }
    }
}

enum class TraversalDisposition {
    EXPAND,
    SKIP,
}
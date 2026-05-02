package planning.domain.vo

import planning.domain.protocol.BreakpointStage
import planning.domain.protocol.TraversalDisposition

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
        ): ActiveEdgeDescriptor =
            ActiveEdgeDescriptor(
                edgeRank = edgeRank,
                traversalDisposition = traversalDisposition,
                breakpointStage = breakpointStage,
            )
    }
}

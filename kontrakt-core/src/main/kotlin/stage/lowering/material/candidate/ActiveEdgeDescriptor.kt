package stage.lowering.material.candidate

import stage.lowering.contract.BreakpointStage
import stage.lowering.contract.TraversalDisposition

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
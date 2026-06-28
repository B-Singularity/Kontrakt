package statemachine.transition.material

import planning.domain.interner.PlanCacheKey
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import stage.lowering.material.CanonicalPlanNode

/**
 * Internal assembly state used before commit/intern.
 *
 * Contract:
 * - MUST remain internal to the session/runtime layer
 * - MUST transition to a committed result through a single typed path
 */
internal class LocalPlanNode private constructor(
    val irNode: CanonicalPlanNode,
    private val children: List<CommittedPlanNode>,
    private val selfSemanticCost: Long,
) {
    /**
     * Deterministic semantic-cost aggregation.
     *
     * Current SSOT rule:
     * - self-cost = 1
     * - total cost = self-cost + sum(children)
     */
    fun commit(cacheKey: PlanCacheKey): FinalCommittedPlanNode {
        val totalCost = selfSemanticCost + children.sumOf { it.treeSemanticCostUpperBound }
        return FinalCommittedPlanNode.issue(
            irNode = irNode,
            cacheKey = cacheKey,
            treeSemanticCostUpperBound = totalCost,
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            irNode: CanonicalPlanNode,
            children: List<CommittedPlanNode>,
            selfSemanticCost: Long = 1L,
        ): LocalPlanNode {
            if (selfSemanticCost < 0L) {
                throw PlanningProtocolIntegrityException(
                    "LocalPlanNode.selfSemanticCost must be >= 0: $selfSemanticCost",
                )
            }
            return LocalPlanNode(
                irNode = irNode,
                children = ArrayList(children),
                selfSemanticCost = selfSemanticCost,
            )
        }
    }
}

package planning.domain.service

import ir.plan.node.CanonicalPlanNode
import planning.domain.interner.PlanCacheKey
import planning.domain.runtime.CommittedPlanNode
import planning.domain.vo.CycleBreakAssembly

/**
 * Factory for committed runtime nodes.
 *
 * Responsibility:
 * - create committed runtime wrappers
 * - keep wrapper-selection logic out of orchestration
 * - prevent StructuralPlannerCore from deciding concrete node wrapper policy
 */
interface CommittedPlanNodeFactory {
    fun createFinal(
        irNode: CanonicalPlanNode,
        cacheKey: PlanCacheKey,
        treeSemanticCostUpperBound: Long,
    ): CommittedPlanNode

    fun createCycleBreak(
        irNode: CanonicalPlanNode,
        cacheKey: PlanCacheKey,
        assembly: CycleBreakAssembly,
    ): CommittedPlanNode
}

package planning.domain.runtime

import stage.lowering.material.CanonicalPlanNode
import stage.lowering.diagnostics.FaultKind
import planning.domain.interner.PlanCacheKey
import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Runtime wrapper around a passive IR node.
 *
 * The IR layer remains passive.
 * Planning/runtime governance metadata MUST NOT leak into IR.
 */
sealed interface CommittedPlanNode {
    val irNode: CanonicalPlanNode
    val cacheKey: PlanCacheKey
    val treeSemanticCostUpperBound: Long
}

/**
 * Fully materialized committed planning result.
 */
class FinalCommittedPlanNode private constructor(
    override val irNode: CanonicalPlanNode,
    override val cacheKey: PlanCacheKey,
    override val treeSemanticCostUpperBound: Long,
) : CommittedPlanNode {
    companion object {
        @JvmStatic
        fun issue(
            irNode: CanonicalPlanNode,
            cacheKey: PlanCacheKey,
            treeSemanticCostUpperBound: Long,
        ): FinalCommittedPlanNode {
            if (treeSemanticCostUpperBound < 0L) {
                throw PlanningProtocolIntegrityException(
                    "FinalCommittedPlanNode.treeSemanticCostUpperBound must be >= 0: $treeSemanticCostUpperBound",
                )
            }
            return FinalCommittedPlanNode(
                irNode = irNode,
                cacheKey = cacheKey,
                treeSemanticCostUpperBound = treeSemanticCostUpperBound,
            )
        }
    }
}

/**
 * Stage-1 deterministic break result.
 */
class DeferredCommittedPlanNode private constructor(
    override val irNode: CanonicalPlanNode,
    override val cacheKey: PlanCacheKey,
    val structuralPath: String,
    val faultKind: FaultKind,
) : CommittedPlanNode {
    override val treeSemanticCostUpperBound: Long = 1L

    companion object {
        @JvmStatic
        fun issue(
            irNode: CanonicalPlanNode,
            cacheKey: PlanCacheKey,
            structuralPath: String,
            faultKind: FaultKind,
        ): DeferredCommittedPlanNode {
            if (structuralPath.isEmpty()) {
                throw PlanningProtocolIntegrityException(
                    "DeferredCommittedPlanNode.structuralPath must not be empty.",
                )
            }
            return DeferredCommittedPlanNode(
                irNode = irNode,
                cacheKey = cacheKey,
                structuralPath = structuralPath,
                faultKind = faultKind,
            )
        }
    }
}

/**
 * Stage-2 deterministic substitution result.
 */
class SubstitutionCommittedPlanNode private constructor(
    override val irNode: CanonicalPlanNode,
    override val cacheKey: PlanCacheKey,
    val target: CommittedPlanNode,
    val reason: String,
) : CommittedPlanNode {
    override val treeSemanticCostUpperBound: Long = target.treeSemanticCostUpperBound

    companion object {
        @JvmStatic
        fun issue(
            irNode: CanonicalPlanNode,
            cacheKey: PlanCacheKey,
            target: CommittedPlanNode,
            reason: String,
        ): SubstitutionCommittedPlanNode {
            if (reason.isEmpty()) {
                throw PlanningProtocolIntegrityException(
                    "SubstitutionCommittedPlanNode.reason must not be empty.",
                )
            }
            return SubstitutionCommittedPlanNode(
                irNode = irNode,
                cacheKey = cacheKey,
                target = target,
                reason = reason,
            )
        }
    }
}

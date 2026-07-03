package realization.planning.assembly

import stage.lowering.material.candidate.CanonicalPlanNode

/**
 * Zero-allocation cursor over committed child results.
 *
 * The passive IR assembler reads children through this cursor instead of receiving
 * a freshly allocated List<ChildDescriptor>.
 */
interface ChildResultSlice {
    fun size(): Int

    fun canonicalIrNodeAt(index: Int): CanonicalPlanNode

    fun semanticCostUpperBoundAt(index: Int): Long
}

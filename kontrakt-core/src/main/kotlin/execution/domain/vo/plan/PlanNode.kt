package execution.domain.vo.plan

import metamodel.domain.vo.TypeReference

/**
 * [Base Contract] Root interface for all plan nodes (Unlinked, Executable).
 *
 * ## Architectural Role
 * - **Type Identity**: Every node MUST point to a specific pure TypeReference.
 * - **Attribute Container**: Every node MUST carry attributes (merged from Decl/Use/Inherited)
 * in a deterministic [Map] structure to support the Last-Wins policy.
 */
interface PlanNode {
    val type: TypeReference

    /**
     * Effective attributes applied to this node.
     * Changed from Set<String> (Legacy) to Map<String, Attribute> (Strict)
     * to support value-carrying attributes (e.g. min=1, max=10).
     */
    val attributes: Map<String, Attribute>
}
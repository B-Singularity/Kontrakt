package execution.domain.vo.plan

import metamodel.domain.vo.TypeReference

/**
 * [Abstract Plan]
 * Represents the structural skeleton of the object graph before strategies are selected.
 *
 * ### Architectural Role
 * - **Simplified Metadata:** Carries attributes as `Set<String>` (e.g. "@Size(min=1)") to decouple the Linker/VM from the heavy Metamodel.
 * - **Structural Intent:** Defines *what* structure needs to be built (Skeleton), but not *how* (Generator is missing).
 */
sealed interface UnlinkedNode : PlanNode

// Matches TypeKind.VALUE (Int, String, Enum, etc.)
data class UnlinkedAtomicNode(
    override val type: TypeReference,
    override val attributes: Set<String> = emptySet()
) : UnlinkedNode

// Matches TypeKind.STRUCTURAL (Class, Data Class)
data class UnlinkedCompositeNode(
    override val type: TypeReference,
    /**
     * The structural fields that need to be linked.
     */
    val fields: Map<String, UnlinkedNode>,
    override val attributes: Set<String> = emptySet()
) : UnlinkedNode

// Matches TypeKind.CONTAINER, TypeKind.ARRAY
data class UnlinkedCollectionNode(
    override val type: TypeReference,
    val elementNode: UnlinkedNode,
    val isFixedSize: Boolean, // True if TypeKind.ARRAY
    override val attributes: Set<String> = emptySet()
) : UnlinkedNode

// Matches TypeKind.MAP
data class UnlinkedMapNode(
    override val type: TypeReference,
    val keyNode: UnlinkedNode,
    val valueNode: UnlinkedNode,
    override val attributes: Set<String> = emptySet()
) : UnlinkedNode

// Matches TypeKind.INTERFACE or Abstract Classes (Polymorphism candidates)
// Note: This node is a placeholder that the Linker will resolve into
// either an ExecutableInterfaceNode or ExecutablePolymorphicNode.
data class UnlinkedInterfaceNode(
    override val type: TypeReference,
    override val attributes: Set<String> = emptySet()
) : UnlinkedNode

// Matches Reference (Cycle detection result)
// Represents a leaf node where recursion was detected and cut by the Planner.
data class UnlinkedReferenceNode(
    override val type: TypeReference,
    val recursionDepth: Int,
    override val attributes: Set<String> = emptySet()
) : UnlinkedNode
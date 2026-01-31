package execution.domain.vo.plan

import metamodel.domain.vo.TypeReference

sealed interface UnlinkedNode : PlanNode

// Matches TypeKind.VALUE
data class UnlinkedAtomicNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute> = emptySet()
) : UnlinkedNode

// Matches TypeKind.STRUCTURAL
data class UnlinkedCompositeNode(
    override val type: TypeReference,
    val fields: Map<String, UnlinkedNode>,
    override val attributes: Set<Attribute> = emptySet()
) : UnlinkedNode

// Matches TypeKind.CONTAINER, TypeKind.ARRAY
data class UnlinkedCollectionNode(
    override val type: TypeReference,
    val elementNode: UnlinkedNode,
    val isFixedSize: Boolean, // True if TypeKind.ARRAY
    override val attributes: Set<Attribute> = emptySet()
) : UnlinkedNode

// Matches TypeKind.MAP (New!)
data class UnlinkedMapNode(
    override val type: TypeReference,
    val keyNode: UnlinkedNode,
    val valueNode: UnlinkedNode,
    override val attributes: Set<Attribute> = emptySet()
) : UnlinkedNode

// Matches TypeKind.INTERFACE
data class UnlinkedInterfaceNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute> = emptySet()
) : UnlinkedNode

// Matches Reference (Cycle detection result)
data class UnlinkedReferenceNode(
    override val type: TypeReference,
    val recursionDepth: Int,
    override val attributes: Set<Attribute> = emptySet()
) : UnlinkedNode
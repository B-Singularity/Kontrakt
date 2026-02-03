package execution.domain.vo.plan

import execution.domain.strategy.generation.Generator
import metamodel.domain.vo.TypeReference

/**
 * [Execution Blueprint]
 * Represents a fully resolved, deterministic execution plan ready for the Virtual Machine.
 */
sealed class ExecutableNode : PlanNode {
    abstract val generator: Generator<*>
    abstract val source: DecisionSource

    /**
     * [Preserved Metadata] ("The Receipt")
     * Constraints and annotations that were applied to this node.
     *
     * ### Design Intent
     * - **Not Consumed:** The Linker has already 'consumed' structural constraints (like @Size) to build the tree.
     * - **Preserved:** These are kept purely for:
     * 1. **Verification:** The VM can assert these rules post-generation.
     * 2. **Auditing:** The report can show "Why this value was generated" (e.g., "Because of @Email").
     */
    abstract override val attributes: Set<String> // [Fix] Added 'override' modifier
}

data class ExecutableAtomicNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableCompositeNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    val fields: Map<String, ExecutableNode>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableCollectionNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    val children: List<ExecutableNode>, // Expanded children
    val isFixedSize: Boolean,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableMapNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    val entries: List<Pair<ExecutableNode, ExecutableNode>>, // Expanded Entries
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

/**
 * [Polymorphic Branch Node]
 * Represents a Sealed Class or Interface where execution delegates to one of several concrete implementations.
 * (This was missing in your snippet but is required for the new architecture)
 */
data class ExecutablePolymorphicNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    val implementations: List<ExecutableNode>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableInterfaceNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    val concreteType: TypeReference,
    val implementationNode: ExecutableNode,
    override val source: DecisionSource
) : ExecutableNode() {
    override val generator: Generator<*> = implementationNode.generator
}

data class ExecutableReferenceNode(
    override val type: TypeReference,
    override val attributes: Set<String>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()
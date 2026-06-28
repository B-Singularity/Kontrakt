package execution.domain.vo.plan

import execution.domain.strategy.generation.Generator
import ir.Attribute
import stage.canonicalization.material.TypeReference

/**
 * [Execution Blueprint]
 * Represents a fully resolved, deterministic execution plan ready for the Virtual Machine.
 *
 * ## Architectural Changes (Legacy vs New)
 * 1. **Attributes:** Changed to [Map] to carry values (e.g. min=1) merged by the Planner.
 * 2. **Structure:** Collections hold a [recipe] (elementNode) instead of expanded instances (children) to allow runtime sizing.
 * 3. **Polymorphism:** [ExecutableInterfaceNode] is merged into [ExecutablePolymorphicNode].
 */
sealed class ExecutableNode : PlanNode {
    abstract val generator: Generator<*>
    abstract val source: DecisionSource

    /**
     * [Preserved Metadata]
     * Attributes merged from declaration and usage sites.
     * Used by the VM for verification (Assertions) and Auditing.
     */
    abstract override val attributes: Map<String, Attribute>
}

data class ExecutableAtomicNode(
    override val type: TypeReference,
    override val attributes: Map<String, Attribute>,
    override val generator: Generator<*>,
    override val source: DecisionSource,
) : ExecutableNode()

data class ExecutableCompositeNode(
    override val type: TypeReference,
    override val attributes: Map<String, Attribute>,
    val fields: Map<String, ExecutableNode>,
    override val generator: Generator<*>,
    override val source: DecisionSource,
) : ExecutableNode()

/**
 * Represents a Collection (List, Set, Array).
 * Holds the 'elementNode' recipe instead of expanded children.
 */
data class ExecutableCollectionNode(
    override val type: TypeReference,
    override val attributes: Map<String, Attribute>,
    val elementNode: ExecutableNode, // The recipe for elements
    val isFixedSize: Boolean,
    override val generator: Generator<*>,
    override val source: DecisionSource,
) : ExecutableNode()

/**
 * Represents a Map.
 * Holds 'keyNode' and 'valueNode' recipes.
 */
data class ExecutableMapNode(
    override val type: TypeReference,
    override val attributes: Map<String, Attribute>,
    val keyNode: ExecutableNode, // Recipe for keys
    val valueNode: ExecutableNode, // Recipe for values
    override val generator: Generator<*>,
    override val source: DecisionSource,
) : ExecutableNode()

/**
 * [Polymorphic Node] (Supersedes ExecutableInterfaceNode)
 * Handles Interfaces, Abstract Classes, and Sealed Classes.
 */
data class ExecutablePolymorphicNode(
    override val type: TypeReference,
    override val attributes: Map<String, Attribute>,
    val implementations: List<ExecutableNode>, // Candidates
    override val generator: Generator<*>,
    override val source: DecisionSource,
) : ExecutableNode()

/**
 * [Cycle Node] (Supersedes ExecutableReferenceNode)
 * Explicitly marks where a cycle was broken (ADR-027).
 */
data class ExecutableCycleNode(
    override val type: TypeReference,
    override val attributes: Map<String, Attribute>,
    override val generator: Generator<*>, // Usually CycleBreakingGenerator
    override val source: DecisionSource,
) : ExecutableNode()

package execution.domain.vo.plan

import execution.domain.strategy.generation.Generator
import metamodel.domain.vo.TypeReference

sealed class ExecutableNode : PlanNode {
    abstract val generator: Generator<*>
    abstract val source: DecisionSource
}

data class ExecutableAtomicNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableCompositeNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute>,
    val fields: Map<String, ExecutableNode>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableCollectionNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute>,
    val children: List<ExecutableNode>, // Expanded children
    val isFixedSize: Boolean,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

// New for MAP
data class ExecutableMapNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute>,
    val entries: List<Pair<ExecutableNode, ExecutableNode>>, // Expanded Entries
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()

data class ExecutableInterfaceNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute>,
    val concreteType: TypeReference,
    val implementationNode: ExecutableNode,
    override val source: DecisionSource
) : ExecutableNode() {
    override val generator: Generator<*> = implementationNode.generator
}

data class ExecutableReferenceNode(
    override val type: TypeReference,
    override val attributes: Set<Attribute>,
    override val generator: Generator<*>,
    override val source: DecisionSource
) : ExecutableNode()
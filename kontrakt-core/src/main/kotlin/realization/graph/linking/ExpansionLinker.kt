package realization.graph.linking

import execution.domain.vo.plan.UnlinkedAtomicNode
import execution.domain.vo.plan.UnlinkedCollectionNode
import execution.domain.vo.plan.UnlinkedCompositeNode
import execution.domain.vo.plan.UnlinkedCycleNode
import execution.domain.vo.plan.UnlinkedMapNode
import execution.domain.vo.plan.UnlinkedNode
import ir.Attribute
import ir.AttributeOrigin
import realization.execution.generation.CycleBreakingGenerator
import realization.execution.plan.DecisionSource
import realization.execution.plan.ExecutableAtomicNode
import realization.execution.plan.ExecutableCollectionNode
import realization.execution.plan.ExecutableCompositeNode
import realization.execution.plan.ExecutableCycleNode
import realization.execution.plan.ExecutableMapNode
import realization.execution.plan.ExecutableNode
import realization.runtime.support.RuntimeInstantiator
import realization.runtime.support.RuntimeTypeResolver
import stage.normalization.material.AnnotationDescriptor

/**
 * [Domain Service] Expansion Linker.
 * Converts UnlinkedNode tree into ExecutableNode tree.
 * Matches the strict constructor signatures of ExecutableNodes.
 */
class ExpansionLinker(
    private val typeResolver: TypeResolver,
    private val runtimeResolver: RuntimeTypeResolver,
    private val instantiator: RuntimeInstantiator,
    private val generatorRegistry: GeneratorRegistry,
) {
    fun link(unlinked: UnlinkedNode): ExecutableNode =
        when (unlinked) {
            is UnlinkedCycleNode -> linkCycleNode(unlinked)
            is UnlinkedAtomicNode -> linkAtomicNode(unlinked)
            is UnlinkedCompositeNode -> linkCompositeNode(unlinked)
            is UnlinkedCollectionNode -> linkCollectionNode(unlinked)
            is UnlinkedMapNode -> linkMapNode(unlinked)
        }

    private fun linkCycleNode(unlinked: UnlinkedCycleNode): ExecutableNode {
        val descriptor = typeResolver.resolve(unlinked.type)

        val declAttributes = LinkedHashMap<String, Attribute>()
        descriptor.annotations.forEach {
            val attr = toAttribute(it, AttributeOrigin.TYPE_DECLARATION)
            putLastWins(declAttributes, attr.name, attr)
        }

        val fullAttributes = LinkedHashMap<String, Attribute>(declAttributes)
        unlinked.attributes.forEach { (k, v) -> putLastWins(fullAttributes, k, v) }

        val handle = runtimeResolver.resolveHandle(unlinked.type)

        return ExecutableCycleNode(
            type = unlinked.type,
            attributes = fullAttributes,
            generator =
                CycleBreakingGenerator(
                    descriptor = descriptor,
                    attributes = fullAttributes,
                    diagnosticInfo = unlinked,
                    runtimeHandle = handle,
                    instantiator = instantiator,
                ),
            source = DecisionSource.Framework("Cycle Truncation (ADR-027)"),
        )
    }

    private fun linkAtomicNode(node: UnlinkedAtomicNode): ExecutableNode =
        ExecutableAtomicNode(
            type = node.type,
            attributes = node.attributes,
            generator = generatorRegistry.findGenerator(node.type, node.attributes),
            source = DecisionSource.User.DEFAULT, // [Fix] Use instance, not Companion
        )

    private fun linkCompositeNode(node: UnlinkedCompositeNode): ExecutableNode {
        val linkedFields = node.fields.mapValues { (_, child) -> link(child) }
        val generator = generatorRegistry.findCompositeGenerator(node.type, linkedFields)

        return ExecutableCompositeNode(
            type = node.type,
            attributes = node.attributes,
            fields = linkedFields,
            generator = generator,
            source = DecisionSource.Strategy.DEFAULT,
        )
    }

    private fun linkCollectionNode(node: UnlinkedCollectionNode): ExecutableNode {
        val linkedElement = link(node.elementNode)
        val generator = generatorRegistry.findCollectionGenerator(node.type, linkedElement, node.isFixedSize)

        return ExecutableCollectionNode(
            type = node.type,
            attributes = node.attributes,
            elementNode = linkedElement,
            isFixedSize = node.isFixedSize,
            generator = generator,
            source = DecisionSource.Strategy.DEFAULT,
        )
    }

    private fun linkMapNode(node: UnlinkedMapNode): ExecutableNode {
        val k = link(node.keyNode)
        val v = link(node.valueNode)
        val gen = generatorRegistry.findMapGenerator(node.type, k, v)

        return ExecutableMapNode(
            type = node.type,
            attributes = node.attributes,
            keyNode = k,
            valueNode = v,
            generator = gen,
            source = DecisionSource.Strategy.DEFAULT,
        )
    }

    private fun <K, V> putLastWins(
        map: LinkedHashMap<K, V>,
        key: K,
        value: V,
    ) {
        if (map.containsKey(key)) map.remove(key)
        map[key] = value
    }

    private fun toAttribute(
        desc: AnnotationDescriptor,
        origin: AttributeOrigin,
    ): Attribute.AnnotationAttribute = Attribute.AnnotationAttribute(desc.qualifiedName, origin, desc.values)
}

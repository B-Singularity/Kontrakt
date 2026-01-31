package execution.domain.service.linker

import execution.domain.exception.LinkageException
import execution.domain.vo.context.linker.LinkerContext
import execution.domain.vo.plan.*

/**
 * Converts the structural plan into a deterministic execution plan.
 * Handles collection expansion, map expansion, and interface polymorphism.
 */
class ExpansionLinker(
    private val registry: GeneratorRegistry
) {

    fun link(
        unlinked: UnlinkedNode,
        context: LinkerContext,
        path: String = "$"
    ): ExecutableNode {
        try {
            // 1. Check User Override
            val overrideGenerator = context.getOverride(path)
            if (overrideGenerator != null) {
                // When overridden, we treat it as an Atomic leaf node (structure collapsed)
                return ExecutableAtomicNode(
                    type = unlinked.type,
                    attributes = unlinked.attributes,
                    generator = overrideGenerator,
                    source = DecisionSource.User("Explicit Override at $path")
                )
            }

            // 2. Select Default Generator
            val (generator, source) = registry.select(unlinked)

            return when (unlinked) {
                is UnlinkedAtomicNode -> {
                    ExecutableAtomicNode(unlinked.type, unlinked.attributes, generator, source)
                }

                is UnlinkedCompositeNode -> {
                    val linkedFields = unlinked.fields.mapValues { (name, childNode) ->
                        link(childNode, context, "$path.$name")
                    }
                    ExecutableCompositeNode(unlinked.type, unlinked.attributes, linkedFields, generator, source)
                }

                is UnlinkedCollectionNode -> {
                    // [Policy] Determine size using the context
                    val size =
                        context.generateStructuralSize(0, 10) // Should use constraints from attributes if available

                    val children = (0 until size).map { index ->
                        link(unlinked.elementNode, context, "$path[$index]")
                    }

                    ExecutableCollectionNode(
                        type = unlinked.type,
                        attributes = unlinked.attributes,
                        children = children,
                        isFixedSize = unlinked.isFixedSize,
                        generator = generator,
                        source = source
                    )
                }

                is UnlinkedMapNode -> {
                    // [Map Expansion] Similar to Collection, we determine number of entries
                    val size = context.generateStructuralSize(0, 10)

                    val entries = (0 until size).map { index ->
                        // Link Key and Value separately
                        // Path notation: map[0].key, map[0].value
                        val keyNode = link(unlinked.keyNode, context, "$path[$index].key")
                        val valueNode = link(unlinked.valueNode, context, "$path[$index].value")
                        keyNode to valueNode
                    }

                    ExecutableMapNode(
                        type = unlinked.type,
                        attributes = unlinked.attributes,
                        entries = entries,
                        generator = generator,
                        source = source
                    )
                }

                is UnlinkedInterfaceNode -> {
                    // [ADR-025] Resolve Polymorphism
                    val resolution = registry.resolveImplementation(unlinked)

                    // [PHASE 1] Atomic Implementation
                    val implementationNode = ExecutableAtomicNode(
                        type = resolution.concreteType,
                        attributes = emptySet(),
                        generator = resolution.generator,
                        source = resolution.source
                    )

                    ExecutableInterfaceNode(
                        type = unlinked.type,
                        attributes = unlinked.attributes,
                        concreteType = resolution.concreteType,
                        implementationNode = implementationNode,
                        source = resolution.source
                    )
                }

                is UnlinkedReferenceNode -> {
                    ExecutableReferenceNode(unlinked.type, unlinked.attributes, generator, source)
                }
            }
        } catch (e: Exception) {
            if (e is execution.domain.exception.ExecutionException) throw e
            throw LinkageException(path, e.message ?: "Unknown linkage error", e)
        }
    }
}
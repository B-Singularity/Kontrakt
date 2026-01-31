package execution.domain.service.vm

import execution.domain.exception.ExecutionException
import execution.domain.exception.VMExecutionException
import execution.domain.strategy.generation.Generator
import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.plan.*

/**
 * [The Executor]
 * A dumb execution engine that traverses the deterministic [ExecutableNode] tree.
 * It strictly follows the plan and delegates value creation to [Generator].
 */
class VirtualMachine {

    fun execute(plan: ExecutableNode, context: GenerationContext): Any? {
        return try {
            traverse(plan, context)
        } catch (e: Exception) {
            if (e is ExecutionException) throw e
            throw VMExecutionException(plan.type, "Runtime execution failed", e)
        }
    }

    private fun traverse(node: ExecutableNode, context: GenerationContext): Any? {
        return when (node) {
            is ExecutableAtomicNode -> {
                node.generator.generate(context)
            }

            is ExecutableCompositeNode -> {
                // 1. Generate all fields (Recursive Step)
                val fieldValues = node.fields.mapValues { (_, childNode) ->
                    traverse(childNode, context)
                }

                // 2. Assemble Object
                // Generator is cast to CompositeGenerator to accept field values
                val generator = node.generator
                if (generator is CompositeGenerator) {
                    generator.generateWithFields(context, fieldValues)
                } else {
                    // Fallback: If generator doesn't support explicit field injection,
                    // it implies it might be a custom generator ignoring the plan's fields.
                    // However, in strict mode, this should be an error or handled gracefully.
                    generator.generate(context)
                }
            }

            is ExecutableCollectionNode -> {
                // 1. Execute all expanded children
                val elements = node.children.map { child ->
                    traverse(child, context)
                }

                if (node.isFixedSize) {
                    // [Array Handling]
                    // Arrays are not MutableCollections. We cannot use 'add'.
                    // Strategy: The Generator knows the array type (e.g., IntArray, String[]).
                    // We can pass the elements to a specialized ArrayGenerator.
                    val generator = node.generator
                    if (generator is ArrayGenerator) {
                        return generator.generateArray(context, elements)
                    } else {
                        throw VMExecutionException(
                            node.type,
                            "Generator for FixedSize collection must implement ArrayGenerator"
                        )
                    }
                } else {
                    // [Collection Handling]
                    // 2. Create the empty container (List, Set)
                    val container = node.generator.generate(context)

                    // 3. Populate container
                    if (container is MutableCollection<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (container as MutableCollection<Any?>).addAll(elements)
                    } else {
                        throw VMExecutionException(
                            node.type,
                            "Generated container is not a MutableCollection: ${container?.javaClass?.name}"
                        )
                    }
                    return container
                }
            }

            is ExecutableMapNode -> {
                // 1. Create the empty map
                val map = node.generator.generate(context)

                if (map is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val mutableMap = map as MutableMap<Any?, Any?>

                    // 2. Execute Entries and Put
                    node.entries.forEach { (keyNode, valueNode) ->
                        val key = traverse(keyNode, context)
                        val value = traverse(valueNode, context)
                        // Note: Key collision strategy is implicit (last one wins)
                        mutableMap[key] = value
                    }
                } else {
                    throw VMExecutionException(node.type, "Generated map is not a MutableMap")
                }
                return map
            }

            is ExecutableInterfaceNode -> {
                traverse(node.implementationNode, context)
            }

            is ExecutableReferenceNode -> {
                node.generator.generate(context)
            }
        }
    }
}

/**
 * Helper interface for Composite Generators (Objects).
 */
interface CompositeGenerator : Generator<Any> {
    fun generateWithFields(context: GenerationContext, fields: Map<String, Any?>): Any
}

/**
 * Helper interface for Array Generators.
 * Arrays require special handling because they are not 'MutableCollection'.
 */
interface ArrayGenerator : Generator<Any> {
    fun generateArray(context: GenerationContext, elements: List<Any?>): Any
}
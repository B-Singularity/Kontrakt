package realization.graph.linking

import ir.Attribute
import realization.execution.generation.Generator
import realization.execution.plan.ExecutableNode
import stage.canonicalization.material.TypeReference

/**
 * [Domain Service] Registry for looking up generators.
 */
interface GeneratorRegistry {
    fun findGenerator(
        type: TypeReference,
        attributes: Map<String, Attribute>,
    ): Generator<Any?>

    fun findCompositeGenerator(
        type: TypeReference,
        fields: Map<String, ExecutableNode>,
    ): Generator<Any?>

    fun findCollectionGenerator(
        type: TypeReference,
        elementNode: ExecutableNode,
        isFixedSize: Boolean,
    ): Generator<Any?>

    fun findMapGenerator(
        type: TypeReference,
        keyNode: ExecutableNode,
        valueNode: ExecutableNode,
    ): Generator<Any?>
}

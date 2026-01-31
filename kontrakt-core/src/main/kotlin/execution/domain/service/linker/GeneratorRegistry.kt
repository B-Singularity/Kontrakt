package execution.domain.service.linker

import execution.domain.exception.GeneratorNotFoundException
import execution.domain.strategy.generation.Generator
import execution.domain.vo.plan.*
import metamodel.domain.vo.TypeReference

/**
 * Determines the generation strategy based on node types and attributes.
 * Acts as a pure "Discriminator" using Chain of Responsibility.
 */
class GeneratorRegistry(
    private val strategies: List<GeneratorSelectionStrategy>,
    private val fallbackStrategy: GeneratorSelectionStrategy,
    private val interfaceStrategy: InterfaceResolutionStrategy
) {

    fun select(node: UnlinkedNode): Pair<Generator<*>, DecisionSource> {
        for (strategy in strategies) {
            val result = strategy.decide(node)
            if (result is SelectionResult.Selected) {
                return result.generator to result.source
            }
        }

        val fallback = fallbackStrategy.decide(node) as? SelectionResult.Selected
            ?: throw GeneratorNotFoundException(node.type, node.attributes)

        return fallback.generator to fallback.source
    }

    fun resolveImplementation(node: UnlinkedInterfaceNode): ResolutionResult {
        return interfaceStrategy.resolve(node)
    }
}

interface GeneratorSelectionStrategy {
    fun decide(node: UnlinkedNode): SelectionResult
}

sealed interface SelectionResult {
    data class Selected(val generator: Generator<*>, val source: DecisionSource) : SelectionResult
    object Pass : SelectionResult
}

interface InterfaceResolutionStrategy {
    fun resolve(node: UnlinkedInterfaceNode): ResolutionResult
}

data class ResolutionResult(
    val concreteType: TypeReference,
    val generator: Generator<*>,
    val source: DecisionSource
)
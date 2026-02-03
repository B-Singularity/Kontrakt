package execution.domain.service.linker

import execution.domain.exception.GeneratorNotFoundException
import execution.domain.strategy.generation.Generator
import execution.domain.vo.plan.DecisionSource
import execution.domain.vo.plan.UnlinkedInterfaceNode
import execution.domain.vo.plan.UnlinkedNode
import metamodel.domain.vo.TypeReference

/**
 * [Generator Discriminator]
 * Determines the specific generation strategy (Worker) based on the structural plan node.
 * Acts as a pure "Decision Maker" using the Chain of Responsibility pattern.
 *
 * ### Architectural Constraints (Strict Boundaries)
 * 1. **No Recursion:** The Registry MUST NOT handle object graph recursion. It selects a strategy for a *single node* only.
 * 2. **No ExecutableNode Creation:** The Registry outputs a `Generator` (Tool) and `DecisionSource` (Reason). It NEVER creates `ExecutableNode` (Instruction).
 * 3. **No Sealed Hierarchy Inspection:** The Registry sees the `UnlinkedNode` as is. It DOES NOT traverse `sealed subclasses`. That is the Linker's responsibility.
 */
class GeneratorRegistry(
    private val strategies: List<GeneratorSelectionStrategy>,
    private val fallbackStrategy: GeneratorSelectionStrategy,
    private val interfaceStrategy: InterfaceResolutionStrategy
) {

    /**
     * Selects the best generator for the given node.
     *
     * @return [GeneratorDecision] containing the selected strategy and its source.
     * @throws GeneratorNotFoundException if no strategy (including fallback) accepts the node.
     */
    fun select(node: UnlinkedNode): GeneratorDecision {
        // 1. Chain of Responsibility: Try specific strategies
        for (strategy in strategies) {
            val result = strategy.decide(node)
            if (result is SelectionResult.Selected) {
                return GeneratorDecision(result.generator, result.source)
            }
        }

        // 2. Fallback: The "Last Resort"
        val fallback = fallbackStrategy.decide(node) as? SelectionResult.Selected
            ?: throw GeneratorNotFoundException(node.type, node.attributes)

        return GeneratorDecision(fallback.generator, fallback.source)
    }

    /**
     * Resolves a single concrete implementation for an interface.
     */
    fun resolveImplementation(node: UnlinkedInterfaceNode): ResolutionResult {
        return interfaceStrategy.resolve(node)
    }
}

// =========================================================================================
//  Service Contracts (DTOs & Strategy Interfaces)
//  These represent the "Port" definition of the GeneratorRegistry service.
// =========================================================================================

/**
 * [Response DTO]
 * Represents the final decision made by the registry.
 * Bundled here because it is tightly coupled to the Registry's return contract.
 */
data class GeneratorDecision(
    val generator: Generator<*>,
    val source: DecisionSource
)

/**
 * Strategy interface for selecting a generator.
 */
interface GeneratorSelectionStrategy {
    fun decide(node: UnlinkedNode): SelectionResult
}

/**
 * [Internal State DTO]
 * Explicitly defines the outcome of a strategy execution.
 */
sealed interface SelectionResult {
    data class Selected(val generator: Generator<*>, val source: DecisionSource) : SelectionResult
    object Pass : SelectionResult
}

/**
 * Strategy for finding a concrete implementation for an interface.
 */
interface InterfaceResolutionStrategy {
    fun resolve(node: UnlinkedInterfaceNode): ResolutionResult
}

/**
 * [Response DTO]
 * Result of an interface resolution attempt.
 */
data class ResolutionResult(
    val concreteType: TypeReference,
    val generator: Generator<*>,
    val source: DecisionSource
)
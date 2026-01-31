package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext

// --- Shell Creators (Empty Containers) ---
// Architectural Role: Creates empty shells. Content population is delegated to the VM/Planner.

class ListGenerator : Generator<MutableList<Any?>> {
    override fun generate(context: GenerationContext) = ArrayList<Any?>()
}

class SetGenerator : Generator<MutableSet<Any?>> {
    override fun generate(context: GenerationContext) = HashSet<Any?>()
}

class MapGenerator : Generator<MutableMap<Any?, Any?>> {
    override fun generate(context: GenerationContext) = HashMap<Any?, Any?>()
}

// --- Array Implementation ---

/**
 * [Generic Array Generator]
 * Implements [ArrayProducer].
 *
 * ### Architectural Role: Pure Executor
 * - Decoupled from `java.lang.reflect`.
 * - Relies on functional strategies for platform-specific operations.
 *
 * @param instantiator Function to create an empty array of size N. `(Size) -> Array`
 * @param setter Function to set a value at index I. `(Array, Index, Value) -> Unit`
 * **Contract Note:** The [setter] implementation is responsible for enforcing nullability constraints
 * and handling type conversions (e.g. primitive unboxing).
 */
class ArrayGenerator(
    private val instantiator: (Int) -> Any,
    private val setter: (Any, Int, Any?) -> Unit
) : ArrayProducer {

    override fun generateArray(context: GenerationContext, elements: List<Any?>): Any {
        // 1. Instantiate (Delegated)
        val array = instantiator(elements.size)

        // 2. Populate
        elements.forEachIndexed { i, v ->
            try {
                // 3. Set Value (Delegated)
                setter(array, i, v)
            } catch (e: Exception) {
                // Contextualize error
                throw RuntimeException(
                    "Array element assignment failed at index $i. Value: '$v' (${v?.javaClass?.simpleName})",
                    e
                )
            }
        }
        return array
    }
}

// --- Object Assembler ---

/**
 * [Object Assembler]
 * A pure generator that creates objects using a generic factory function.
 *
 * @param factory A function that takes a Map of field values and produces the Object.
 * `(Fields) -> Object`. The factory encapsulates validation, reflection, and accessibility logic.
 */
class ObjectAssembler(
    private val factory: (Map<String, Any?>) -> Any
) : CompositeGenerator {

    override fun generateWithFields(context: GenerationContext, fields: Map<String, Any?>): Any {
        try {
            // Delegate complexity to the factory lambda
            return factory(fields)
        } catch (e: Exception) {
            val cause = e.cause ?: e
            throw RuntimeException(
                "Object assembly failed. Provided Fields: ${fields.keys}. Error: ${cause.message}",
                cause
            )
        }
    }
}

/**
 * [Polymorphic Strategy]
 * Responsible for selecting one implementation from a set of candidates.
 * used for Sealed Classes, Interfaces, or Abstract Classes.
 *
 * ### Architectural Role: Pure Executor
 * - **DOES NOT** perform reflection to find subclasses (Delegated to Linker).
 * - **DOES NOT** handle recursion (Delegated to Linker).
 * - **Merely delegates** execution to one of the injected [candidates].
 *
 * @param candidates List of concrete generators for each subtype.
 * Must be pre-validated by the Linker to ensure exhaustive coverage.
 */
class SealedClassGenerator(
    private val candidates: List<Generator<Any>>
) : Generator<Any> {

    init {
        // "Linker Contract" Defense: Empty candidates imply a broken schema graph.
        require(candidates.isNotEmpty()) {
            "SealedClassGenerator must have at least one candidate. Check Linker logic."
        }
    }

    override fun generate(context: GenerationContext): Any {
        // Smart Fuzzing: Randomly select one concrete implementation strategy.
        // This simulates polymorphic behavior at runtime.
        val selectedGenerator = candidates.random(context.seededRandom)
        return selectedGenerator.generate(context)
    }

    /**
     * ### Philosophy: Breadth over Depth
     * Instead of drilling down into edge cases of *each* candidate (which causes combinatorial explosion),
     * this strategy ensures **Type Coverage**.
     *
     * - Returns one valid instance from **EVERY** candidate.
     * - Guarantees that the consumer handles every concrete subtype at least once.
     */
    override fun generateEdgeCases(context: GenerationContext): List<Any> {
        return candidates.map { it.generate(context) }.distinct()
    }

    /**
     * Aggregates invalid cases from all candidates.
     * If a value is invalid for *any* subtype, it is likely invalid for the polymorphic type
     * (depending on the constraint), but strictly speaking, we want to test
     * if the system handles garbage input for any possible implementation.
     */
    override fun generateInvalid(context: GenerationContext): List<Any?> {
        return candidates.flatMap { it.generateInvalid(context) }.distinct()
    }

    override fun toString(): String = "SealedClassGenerator(candidates=${candidates.size})"
}
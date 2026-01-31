package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext

/**
 * [Strategy Interface]
 * The executable unit that the Virtual Machine invokes to produce a value.
 *
 * This interface encapsulates the "Execution" phase of the test data generation.
 * Unlike the legacy architecture, these generators are NOT responsible for parsing annotations
 * or handling recursion. They are simple workers that produce values based on
 * configuration injected via their constructors.
 *
 * It supports three modes of generation to maintain the "Smart Fuzzing" capabilities:
 * 1. [generate]: The happy path (Smart Random with bias towards edges).
 * 2. [generateEdgeCases]: Boundary values (Min, Max, Empty, etc.).
 * 3. [generateInvalid]: Values guaranteed to violate constraints (Negative Testing).
 *
 * @param T The type of value produced by this generator.
 */
interface Generator<out T> {
    /**
     * Generates a single valid value using the provided runtime context.
     * Implementations should prefer "Smart Random" (bias towards edges) over pure random
     * to increase the likelihood of finding bugs.
     *
     * @param context Provides access to the source of entropy (Random), clock, etc.
     * @return The generated value.
     */
    fun generate(context: GenerationContext): T

    /**
     * Returns a list of "Edge Cases" or "Boundary Values" for exhaustive testing.
     * Examples: Min/Max values for numbers, Empty strings, Nulls (if nullable).
     */
    fun generateEdgeCases(context: GenerationContext): List<T> = emptyList()

    /**
     * Returns a list of "Invalid Values" that violate the constraints.
     * Used for verifying defensive programming logic (Negative Testing).
     * Examples: Out of range numbers, Invalid formats.
     */
    fun generateInvalid(context: GenerationContext): List<Any?> = emptyList()
}

/**
 * [Extension] For Generators that need to assemble objects from pre-calculated field values.
 * Used by [execution.domain.vo.plan.ExecutableCompositeNode].
 *
 * The VM drives the recursion and passes the generated field values to this generator.
 */
interface CompositeGenerator : Generator<Any> {
    /**
     * Assembles the object using the provided field values.
     *
     * @param context The generation context.
     * @param fields A map of field names to their generated values.
     * @return The assembled object instance.
     */
    fun generateWithFields(context: GenerationContext, fields: Map<String, Any?>): Any

    // Composite generators are driven by the VM's structure traversal,
    // so a standalone generate() call without fields is not supported.
    override fun generate(context: GenerationContext): Any =
        throw UnsupportedOperationException("CompositeGenerator requires fields. Use generateWithFields().")
}

/**
 * [Extension] For Generators that create fixed-size Arrays.
 * Used by [execution.domain.vo.plan.ExecutableCollectionNode] when isFixedSize=true.
 *
 * Arrays require special handling because they are not 'MutableCollection' and
 * need component type information for creation.
 */
interface ArrayProducer : Generator<Any> {
    /**
     * Creates an array populated with the given elements.
     *
     * @param context The generation context.
     * @param elements The elements to populate the array with.
     * @return The created array instance.
     */
    fun generateArray(context: GenerationContext, elements: List<Any?>): Any

    override fun generate(context: GenerationContext): Any =
        throw UnsupportedOperationException("ArrayGenerator requires elements. Use generateArray().")
}
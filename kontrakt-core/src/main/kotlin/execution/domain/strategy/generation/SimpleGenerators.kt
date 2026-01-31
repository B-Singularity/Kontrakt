package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext

/**
 * Generator for Boolean values.
 *
 * @param fixedValue If set, the generator acts as a constant provider.
 */
class BooleanGenerator(private val fixedValue: Boolean? = null) : Generator<Boolean> {

    override fun generate(context: GenerationContext): Boolean {
        return fixedValue ?: context.seededRandom.nextBoolean()
    }

    override fun generateEdgeCases(context: GenerationContext): List<Boolean> {
        return if (fixedValue != null) listOf(fixedValue) else listOf(true, false)
    }

    /**
     * @return Logical Invalid values.
     * Note: Returns the negation of the fixed value if one exists.
     * If no fixed value is set, there is no concept of "contract invalid" for a pure boolean.
     */
    override fun generateInvalid(context: GenerationContext): List<Any?> {
        return if (fixedValue != null) listOf(!fixedValue) else emptyList()
    }
}

/**
 * Generator for Enum values.
 *
 * ### Improvements
 * 1. **Strict Typing**: Enforces `<T : Enum<T>>` to ensure compile-time safety.
 * 2. **Closed Set**: Correctly treats Enums as closed sets with no "logical invalid" values outside the set.
 */
class EnumGenerator<T : Enum<T>>(
    private val constants: Array<T>
) : Generator<T> {

    init {
        require(constants.isNotEmpty()) { "Enum constants cannot be empty" }
    }

    override fun generate(context: GenerationContext): T {
        return constants.random(context.seededRandom)
    }

    override fun generateEdgeCases(context: GenerationContext): List<T> {
        // Exhaustive testing is the best strategy for finite Enums.
        return constants.toList()
    }

    override fun generateInvalid(context: GenerationContext): List<Any?> {
        // Enums are closed sets. No logical invalid values exist outside the type definition.
        return emptyList()
    }
}
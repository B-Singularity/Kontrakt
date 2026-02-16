package linking.domain.vo

import linking.domain.exception.LinkingInputException

/**
 * [Value Object] Represents the unique identifier of a test scenario.
 * Encapsulates validation logic to ensure no invalid IDs exist in the domain.
 */
@JvmInline
value class ScenarioId(val value: String) {
    init {
        if (value.isBlank()) {
            // Using a temporary map for the exception to avoid circular dependency issues
            throw LinkingInputException(
                "ScenarioId cannot be blank.",
                mapOf("invalid_value" to value)
            )
        }
    }

    override fun toString(): String = value
}
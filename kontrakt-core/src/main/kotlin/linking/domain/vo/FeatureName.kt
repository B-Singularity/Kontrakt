package linking.domain.vo

import linking.domain.exception.LinkingInputException

/**
 * [Value Object] Represents a distinct feature name required by a scenario.
 * Implements [Comparable] to support deterministic sorting naturally.
 */
@JvmInline
value class FeatureName(
    val value: String,
) : Comparable<FeatureName> {
    init {
        if (value.isBlank()) {
            throw LinkingInputException(
                "FeatureName cannot be blank.",
                mapOf("invalid_value" to value),
            )
        }
    }

    override fun compareTo(other: FeatureName): Int = this.value.compareTo(other.value)

    override fun toString(): String = value
}

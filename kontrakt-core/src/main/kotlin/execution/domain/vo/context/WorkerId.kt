package execution.domain.vo.context

/**
 * [Domain Value Object] Strongly typed identifier for a Worker Thread.
 *
 * Implements the "Private Constructor + Factory" pattern to balance
 * Domain Invariants (Validation) and Testability (MockK Compatibility).
 */
@JvmInline
value class WorkerId private constructor(
    val value: Int,
) {
    companion object {
        /**
         * [Factory] Standard creation method.
         * Enforces domain invariants. Use this in application code.
         *
         * @throws IllegalArgumentException if the value is negative.
         */
        fun of(value: Int): WorkerId {
            require(value >= 0) { "WorkerId must be non-negative" }
            return WorkerId(value)
        }

        /**
         * [Factory] Safe generation from thread identity.
         * Guaranteed to be non-negative via bitwise operation.
         */
        fun fromCurrentThread(): WorkerId {
            // [Safety] Strip the sign bit to ensure strictly non-negative ID
            return WorkerId(Thread.currentThread().name.hashCode() and Int.MAX_VALUE)
        }
    }
}
package execution.domain.vo

/**
 * [Domain Value Object] Strongly typed identifier for a Worker Thread.
 *
 * Wraps a primitive Int to prevent accidental misuse (e.g., passing a generic counter or index).
 * Uses @JvmInline to ensure zero runtime overhead (compiles down to a primitive int).
 */

@JvmInline
value class WorkerId(val value: Int) {
    init {
        require(value >= 0) { "WorkerId must be non-negative" }
    }

    companion object {
        /**
         * Generates a WorkerId from the current thread.
         * Uses hashcode as a stable identifier.
         */
        fun fromCurrentThread(): WorkerId = WorkerId(Thread.currentThread().name.hashCode())
    }
}
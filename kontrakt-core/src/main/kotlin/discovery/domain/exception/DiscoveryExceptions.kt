package discovery.domain.exception

import exception.KontraktException

/**
 * [Integrity Violation]
 * Thrown when the runtime environment is corrupted (e.g., LinkageError)
 * or when an internal invariant is broken.
 * Action: Ops/DevOps intervention required.
 */
class RuntimeIntegrityException(
    message: String,
    cause: Throwable? = null
) : KontraktException(message, cause) {
    override val domain: String = "RUNTIME_INTEGRITY"
}

/**
 * [Structured Violation Data]
 * Represents a user-fixable violation found in the codebase.
 *
 * ## Determinism Contract
 * - **message**: MUST be static text. Dynamic values should be limited to stable identifiers (e.g., Class Name).
 * - **Prohibited**: Absolute paths, memory addresses, timestamps, or stack traces in the message.
 * - **Purpose**: Ensures byte-for-byte reproducible reports across different environments.
 */
data class DiscoveryViolation(
    val className: String,
    val sourceLocation: String?,
    val context: String,
    val kind: ViolationKind,
    val message: String
) : Comparable<DiscoveryViolation> {

    enum class ViolationKind {
        /** User error: Naming convention, Annotation misuse, Logical conflict. */
        PROTOCOL_VIOLATION
    }

    override fun toString(): String {
        val loc = sourceLocation?.let { "($it)" } ?: ""
        return "[$kind] $className$loc: $message [$context]"
    }

    /**
     * [Determinism] Total Order based on structural fields.
     */
    override fun compareTo(other: DiscoveryViolation): Int {
        return compareValuesBy(
            this, other,
            { kindRank(it.kind) },
            { it.className },
            { it.context },
            { it.sourceLocation ?: "" },
            { it.message } // Safe as per Determinism Contract
        )
    }

    private fun kindRank(k: ViolationKind): Int = when (k) {
        ViolationKind.PROTOCOL_VIOLATION -> 0
    }
}

/**
 * [Discovery Failure]
 * Aggregates user protocol violations.
 */
class DiscoveryFailedException(
    val violations: List<DiscoveryViolation>
) : KontraktException(buildSummary(violations)) {

    override val domain: String = "DISCOVERY"

    companion object {
        private fun buildSummary(violations: List<DiscoveryViolation>): String {
            val count = violations.size
            val preview = violations.take(3).joinToString(", ") { "${it.className}(${it.kind})" }
            val ellipsis = if (count > 3) ", and ${count - 3} more" else ""
            return "Discovery failed with $count violations: [$preview$ellipsis]"
        }
    }
}
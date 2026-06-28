package stage.diagnostic.material

import exception.safety.PayloadSanitizer

/**
 * [Base Exception] Root of the Kontrakt framework exception hierarchy.
 */
open class KontraktException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    open val domain: String = "SHARED"

    /**
     * [Fail-Safe Context]
     * Guaranteed to be non-null, immutable, and JSON-safe.
     */
    val details: Map<String, Any?> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        try {
            // Safe, typed sanitization
            PayloadSanitizer.sanitizeMap(payload)
        } catch (e: Throwable) {
            mapOf("error" to "Critical Failure in Exception Context", "cause" to e.message)
        }
    }

    /**
     * [Extension Point]
     * Subclasses override this to provide raw context.
     */
    protected open val payload: Map<String, Any?> = emptyMap()
}

class KontraktConfigurationException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain = "CONFIGURATION"
}

class ContractViolationException(
    message: String,
    val violations: List<String> = emptyList(),
) : KontraktException(message, null) {
    override val domain = "CONTRACT"
    override val payload = mapOf("violations" to violations)
}

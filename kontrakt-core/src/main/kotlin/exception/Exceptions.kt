package exception

import infrastructure.json.JsonSafeValidator

open class KontraktException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    open val domain: String = "SHARED"
    open val details: Map<String, Any?> = emptyMap()

    /**
     * [Safety Guard]
     * Validates payload. Passing payload explicitly to avoid property recursion issues.
     */
    protected fun validateJsonSafety(payload: Any?) {
        JsonSafeValidator.validate(payload)
    }
}
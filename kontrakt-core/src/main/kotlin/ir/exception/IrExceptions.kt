package kontrakt.ir.exception

import exception.KontraktException

/**
 * [Protocol Violation]
 * Thrown ONLY when input data violates the IR Constitution (e.g., Invalid TypeId format).
 * This exception is atomic and does not know about the Discovery process.
 *
 * It extends [KontraktException] to ensure consistent error handling across the framework.
 */
class IrProtocolViolationException(
    message: String,
    cause: Throwable? = null
) : KontraktException(message, cause) {
    override val domain: String = "IR_PROTOCOL"
}
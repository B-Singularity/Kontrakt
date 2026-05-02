package ir.exception

import exception.KontraktException

/**
 * Machine-readable fault taxonomy for routing and observability.
 *
 * This separation is critical for:
 * - Differentiating user model issues vs. framework contract breaches
 * - Stable error classification across adapters and execution layers
 */
enum class FaultKind {
    /** User input violated limits, syntax, or constraints. */
    USER_MODEL_INVALID,

    /** Internal adapters failed to uphold core contracts (e.g., Normalization). */
    FRAMEWORK_INVARIANT_BROKEN,
}

/**
 * Root exception for the IR protocol module.
 *
 * Guarantees:
 * - No dependency on outer modules (except the shared base exception type)
 * - Stable `domain` for log routing
 * - Mandatory `faultKind` for machine-driven handling
 */
abstract class IrException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "IR_PROTOCOL"
    abstract val faultKind: FaultKind
}

class IrProtocolViolationException(
    message: String,
    cause: Throwable? = null,
) : IrException(message, cause) {
    override val faultKind: FaultKind = FaultKind.USER_MODEL_INVALID
}

class IrInvariantBrokenException(
    message: String,
    cause: Throwable? = null,
) : IrException(message, cause) {
    override val faultKind: FaultKind = FaultKind.FRAMEWORK_INVARIANT_BROKEN
}

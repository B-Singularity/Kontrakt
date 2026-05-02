package ir

/**
 * Global hard limits for the IR protocol.
 *
 * Design intent:
 * - Single Source of Truth (SSOT) for all inbound validation and DoS defense.
 * - Used by IR value objects and deterministic containers.
 *
 * ARCHUNIT SEAL:
 * - This object MUST contain ONLY `const val` declarations.
 */
object IrLimits {
    const val MAX_IDENTIFIER_LENGTH = 255
    const val MAX_TYPE_ID_LENGTH = 255
    const val MAX_METADATA_VALUE_LENGTH = 4096

    const val MAX_SIGNATURE_BYTES = 8192

    const val MAX_CONTRACT_TYPES = 1024
    const val MAX_MODES = 128
    const val MAX_METADATA_ENTRIES = 1024
    const val MAX_ATTRIBUTE_VALUES = 1024

    const val MAX_NODE_ATTRIBUTES = 1024
    const val MAX_NODE_FIELDS = 1024

    /** Upper bound for raw payload size, measured in BYTES (UTF-8). */
    const val MAX_PAYLOAD_BYTES = 5 * 1024 * 1024 // 5MB

    /**
     * DoS defense against huge ingestion collections that dedupe down.
     * For Deterministic* factories: input.size MUST NOT exceed (limit * MULTIPLIER).
     */
    const val DETERMINISTIC_INPUT_MULTIPLIER = 4
}

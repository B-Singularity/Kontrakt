package planning.infrastructure.runtime.policy.internal

/**
 * Internal sizing calibration used only by runtime-side policy/capacity resolution.
 *
 * This is NOT part of the public / cross-boundary contract.
 * These values are implementation-policy defaults, not protocol SSOT constants.
 */
internal data class ResolvedSizingCalibration(
    val signatureReserveRatio: Double = 0.20,
    val preferredDepthDivisor: Int = 8,
    val undoRecordsPerDepth: Int = 4,
    val secureWipeOnReset: Boolean = false,
)

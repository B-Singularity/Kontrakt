package planning.domain.expansion.polymorphic

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Ratification fingerprint for runtime binding scope.
 *
 * Current canonical representation is BLAKE3-256 lowercase hex produced by the
 * ratification boundary. This class validates shape only; it does not compute
 * the digest.
 */
class RuntimeBindingRatificationFingerprint private constructor(
    val lowercaseHex256: String,
) {
    override fun equals(other: Any?): Boolean {
        return other is RuntimeBindingRatificationFingerprint &&
                lowercaseHex256 == other.lowercaseHex256
    }

    override fun hashCode(): Int {
        return lowercaseHex256.hashCode()
    }

    override fun toString(): String {
        return lowercaseHex256
    }

    companion object {
        private const val HEX_256_LENGTH: Int = 64

        @JvmStatic
        fun issue(lowercaseHex256: String): RuntimeBindingRatificationFingerprint {
            if (lowercaseHex256.length != HEX_256_LENGTH) {
                throw TypeExpansionContractViolationException(
                    reason = "RuntimeBindingRatificationFingerprint must be BLAKE3-256 lowercase hex: actualLength=${lowercaseHex256.length}",
                )
            }

            if (!lowercaseHex256.all { it in '0'..'9' || it in 'a'..'f' }) {
                throw TypeExpansionContractViolationException(
                    reason = "RuntimeBindingRatificationFingerprint must be lowercase hex.",
                )
            }

            return RuntimeBindingRatificationFingerprint(lowercaseHex256)
        }
    }
}
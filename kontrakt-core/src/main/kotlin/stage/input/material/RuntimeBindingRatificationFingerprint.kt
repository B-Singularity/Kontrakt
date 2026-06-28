package metamodel.domain.vo

import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Ratified fingerprint for a runtime binding decision.
 *
 * This is not:
 *
 * - a cache key;
 * - a route key;
 * - a persisted identity;
 * - a security bearer token;
 * - a random nonce;
 * - or a canonical byte encoder.
 *
 * The input boundary accepts a 256-bit lowercase hexadecimal string.
 * The internal representation is 32 raw bytes to avoid retaining a 64-character
 * hex String for every runtime binding proof.
 *
 * Security law:
 *
 * - toString() never exposes fingerprint bytes;
 * - renderSummary() never exposes fingerprint bytes;
 * - equals(...) uses fixed-length constant-time byte comparison;
 * - invalid hex diagnostics do not echo the offending character;
 * - callers receive defensive byte copies only.
 *
 * Memory law:
 *
 * Storing 32 bytes is materially cheaper than retaining a 64-character String
 * across large binding graphs.
 *
 * Hash law:
 *
 * hashCode() is for in-memory equality collections only.
 *
 * Do not use hashCode() as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime order hash;
 * - serialized order digest.
 */
class RuntimeBindingRatificationFingerprint private constructor(
    private val bytes: ByteArray,
) {
    fun copyBytesForProtocolDerivation(): ByteArray = bytes.copyOf()

    /**
     * Diagnostic-safe summary.
     *
     * This intentionally exposes only the bit width, not the fingerprint value.
     */
    fun renderSummary(): String = "RuntimeBindingRatificationFingerprint(<redacted>, bits=${bytes.size * 8})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingRatificationFingerprint) return false

        return constantTimeEquals(
            left = bytes,
            right = other.bytes,
        )
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = renderSummary()

    companion object {
        private const val HEX_256_LENGTH: Int = 64
        private const val BYTE_256_LENGTH: Int = 32

        @JvmStatic
        fun issueLowercaseHex256(lowercaseHex256: String): RuntimeBindingRatificationFingerprint {
            if (lowercaseHex256.length != HEX_256_LENGTH) {
                throw MetamodelFactContractViolationException(
                    "RuntimeBindingRatificationFingerprint must be 256-bit lowercase hex.",
                )
            }

            val bytes = ByteArray(BYTE_256_LENGTH)

            var index = 0
            while (index < BYTE_256_LENGTH) {
                val high =
                    decodeLowercaseHexNibble(
                        ch = lowercaseHex256[index * 2],
                        index = index * 2,
                    )
                val low =
                    decodeLowercaseHexNibble(
                        ch = lowercaseHex256[index * 2 + 1],
                        index = index * 2 + 1,
                    )

                bytes[index] = ((high shl 4) or low).toByte()
                index += 1
            }

            return RuntimeBindingRatificationFingerprint(bytes)
        }

        private fun decodeLowercaseHexNibble(
            ch: Char,
            index: Int,
        ): Int =
            when (ch) {
                in '0'..'9' -> ch.code - '0'.code
                in 'a'..'f' -> ch.code - 'a'.code + 10
                else -> throw MetamodelFactContractViolationException(
                    "RuntimeBindingRatificationFingerprint contains invalid lowercase hex at index=$index.",
                )
            }

        /**
         * Constant-time comparison for fixed-size fingerprint bytes.
         *
         * All valid instances are 32 bytes. The length fold keeps the method
         * defensive if construction rules are changed later.
         */
        private fun constantTimeEquals(
            left: ByteArray,
            right: ByteArray,
        ): Boolean {
            var diff = left.size xor right.size

            val minLength =
                if (left.size < right.size) {
                    left.size
                } else {
                    right.size
                }

            var index = 0
            while (index < minLength) {
                diff = diff or (left[index].toInt() xor right[index].toInt())
                index += 1
            }

            return diff == 0
        }
    }
}

package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Ratified deterministic seed material.
 *
 * The seed is represented as 256-bit lowercase hex at the input boundary and is
 * stored internally as 32 raw bytes.
 *
 * This object does not expose the seed as String.
 * Entropy derivation layers may request a defensive byte copy.
 *
 * Security law:
 *
 * - toString() never exposes seed bytes;
 * - equals(...) uses fixed-length constant-time byte comparison;
 * - invalid hex diagnostics do not echo the offending character;
 * - hashCode() is for in-memory equality collections only.
 *
 * Do not use hashCode() as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime protocol hash;
 * - seed digest.
 *
 * If a stable seed fingerprint is needed, add a dedicated seed-fingerprint
 * derivation boundary with explicit algorithm/version metadata.
 */
class SeedMaterial private constructor(
    private val bytes: ByteArray,
) {
    fun copyBytesForDerivation(): ByteArray {
        return bytes.copyOf()
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is SeedMaterial) return false

        return constantTimeEquals(
            left = bytes,
            right = other.bytes,
        )
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "SeedMaterial(<redacted>, bits=${bytes.size * 8})"
    }

    companion object {
        private const val HEX_256_LENGTH: Int = 64
        private const val BYTE_256_LENGTH: Int = 32

        @JvmStatic
        fun issueLowercaseHex256(
            hex: String,
        ): SeedMaterial {
            if (hex.length != HEX_256_LENGTH) {
                throw TypeExpansionContractViolationException(
                    reason = "SeedMaterial must be 256-bit lowercase hex: " +
                            "expectedLength=$HEX_256_LENGTH, actualLength=${hex.length}",
                )
            }

            val bytes = ByteArray(BYTE_256_LENGTH)

            var index = 0
            while (index < bytes.size) {
                val high = decodeLowercaseHexNibble(
                    ch = hex[index * 2],
                    index = index * 2,
                )
                val low = decodeLowercaseHexNibble(
                    ch = hex[index * 2 + 1],
                    index = index * 2 + 1,
                )

                bytes[index] = ((high shl 4) or low).toByte()
                index += 1
            }

            return SeedMaterial(bytes)
        }

        private fun decodeLowercaseHexNibble(
            ch: Char,
            index: Int,
        ): Int {
            return when (ch) {
                in '0'..'9' -> ch.code - '0'.code
                in 'a'..'f' -> ch.code - 'a'.code + 10
                else -> throw TypeExpansionContractViolationException(
                    reason = "SeedMaterial contains invalid lowercase hex at index=$index.",
                )
            }
        }

        /**
         * Constant-time comparison for fixed-size seed bytes.
         *
         * All valid SeedMaterial instances are 32 bytes, but the length fold keeps
         * the method defensive if construction rules are changed later.
         */
        private fun constantTimeEquals(
            left: ByteArray,
            right: ByteArray,
        ): Boolean {
            var diff = left.size xor right.size

            val minLength = if (left.size < right.size) {
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
package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Ratified deterministic seed material.
 *
 * The seed is represented as 256-bit lowercase hex.
 *
 * This object does not expose the seed as String.
 * Entropy derivation layers may request a defensive byte copy.
 */
class SeedMaterial private constructor(
    private val bytes: ByteArray,
) {
    fun copyBytesForDerivation(): ByteArray {
        return bytes.copyOf()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SeedMaterial) return false

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString(): String {
        return "SeedMaterial(<redacted>, bits=${bytes.size * 8})"
    }

    companion object {
        private const val HEX_256_LENGTH: Int = 64

        @JvmStatic
        fun issueLowercaseHex256(hex: String): SeedMaterial {
            if (hex.length != HEX_256_LENGTH) {
                throw TypeExpansionContractViolationException(
                    reason = "SeedMaterial must be 256-bit lowercase hex: expectedLength=$HEX_256_LENGTH, actualLength=${hex.length}",
                )
            }

            if (!hex.all { it in '0'..'9' || it in 'a'..'f' }) {
                throw TypeExpansionContractViolationException(
                    reason = "SeedMaterial must be lowercase hexadecimal.",
                )
            }

            val bytes = ByteArray(32)
            var i = 0
            while (i < bytes.size) {
                val high = decodeHex(hex[i * 2])
                val low = decodeHex(hex[i * 2 + 1])
                bytes[i] = ((high shl 4) or low).toByte()
                i++
            }

            return SeedMaterial(bytes)
        }

        private fun decodeHex(ch: Char): Int {
            return when (ch) {
                in '0'..'9' -> ch.code - '0'.code
                in 'a'..'f' -> ch.code - 'a'.code + 10
                else -> throw TypeExpansionContractViolationException(
                    reason = "Invalid lowercase hex char: $ch",
                )
            }
        }
    }
}
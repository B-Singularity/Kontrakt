package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Local ordinal after canonical ordering.
 *
 * Not a value class.
 *
 * Canonical encoding law:
 * - fixed-width signed 32-bit integer;
 * - big-endian;
 * - decimal String rendering is diagnostics only.
 */
class CanonicalLocalOrdinal private constructor(
    val value: Int,
) {
    fun plus(increment: Int): CanonicalLocalOrdinal {
        if (increment < 0) {
            throw TypeExpansionContractViolationException(
                reason = "CanonicalLocalOrdinal increment must be >= 0: $increment",
            )
        }

        val result = value.toLong() + increment.toLong()
        if (result > MAX_LOCAL_ORDINAL) {
            throw TypeExpansionContractViolationException(
                reason = "CanonicalLocalOrdinal overflow: current=$value, increment=$increment, max=$MAX_LOCAL_ORDINAL",
            )
        }

        return CanonicalLocalOrdinal(result.toInt())
    }

    fun writeFixedInt32BigEndian(
        destination: ByteArray,
        offset: Int,
    ) {
        if (offset < 0 || offset + 4 > destination.size) {
            throw TypeExpansionContractViolationException(
                reason = "Cannot write CanonicalLocalOrdinal at offset=$offset into destination size=${destination.size}",
            )
        }

        destination[offset] = ((value ushr 24) and 0xff).toByte()
        destination[offset + 1] = ((value ushr 16) and 0xff).toByte()
        destination[offset + 2] = ((value ushr 8) and 0xff).toByte()
        destination[offset + 3] = (value and 0xff).toByte()
    }

    override fun equals(other: Any?): Boolean {
        return other is CanonicalLocalOrdinal && value == other.value
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "ord($value)"
    }

    companion object {
        private const val MAX_LOCAL_ORDINAL: Int = 1_000_000

        @JvmStatic
        fun of(value: Int): CanonicalLocalOrdinal {
            if (value < 0 || value > MAX_LOCAL_ORDINAL) {
                throw TypeExpansionContractViolationException(
                    reason = "CanonicalLocalOrdinal out of range: value=$value, max=$MAX_LOCAL_ORDINAL",
                )
            }

            return CanonicalLocalOrdinal(value)
        }

        @JvmStatic
        fun zero(): CanonicalLocalOrdinal {
            return CanonicalLocalOrdinal(0)
        }
    }
}
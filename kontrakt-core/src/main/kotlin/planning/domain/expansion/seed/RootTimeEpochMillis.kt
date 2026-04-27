package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Run-ratified root time.
 *
 * Not a value class.
 *
 * V2 fixes seed root time precision to milliseconds.
 */
class RootTimeEpochMillis private constructor(
    val value: Long,
) {
    fun writeFixedInt64BigEndian(
        destination: ByteArray,
        offset: Int,
    ) {
        if (offset < 0 || offset + 8 > destination.size) {
            throw TypeExpansionContractViolationException(
                reason = "Cannot write RootTimeEpochMillis at offset=$offset into destination size=${destination.size}",
            )
        }

        destination[offset] = ((value ushr 56) and 0xff).toByte()
        destination[offset + 1] = ((value ushr 48) and 0xff).toByte()
        destination[offset + 2] = ((value ushr 40) and 0xff).toByte()
        destination[offset + 3] = ((value ushr 32) and 0xff).toByte()
        destination[offset + 4] = ((value ushr 24) and 0xff).toByte()
        destination[offset + 5] = ((value ushr 16) and 0xff).toByte()
        destination[offset + 6] = ((value ushr 8) and 0xff).toByte()
        destination[offset + 7] = (value and 0xff).toByte()
    }

    override fun equals(other: Any?): Boolean {
        return other is RootTimeEpochMillis && value == other.value
    }

    override fun hashCode(): Int {
        return (value xor (value ushr 32)).toInt()
    }

    override fun toString(): String {
        return "epochMillis($value)"
    }

    companion object {
        @JvmStatic
        fun of(value: Long): RootTimeEpochMillis {
            if (value < 0L) {
                throw TypeExpansionContractViolationException(
                    reason = "RootTimeEpochMillis must be >= 0: $value",
                )
            }

            return RootTimeEpochMillis(value)
        }
    }
}
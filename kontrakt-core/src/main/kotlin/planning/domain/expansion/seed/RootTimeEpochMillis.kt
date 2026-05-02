package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Root epoch-millis value used as deterministic seed material.
 *
 * This is not:
 *
 * - a wall-clock reader;
 * - a system time provider;
 * - a mutable clock;
 * - a scheduling primitive;
 * - or a policy resolver.
 *
 * The value must be captured at a stable boundary before deterministic planning
 * begins. Once captured, it is just seed material.
 *
 * Range law:
 *
 * - negative epoch millis are rejected;
 * - no arbitrary future upper bound is hardcoded here.
 *
 * If a deployment wants to reject values beyond a specific future date, that
 * belongs to a policy boundary such as PlannerSessionConfig / SeedPolicy, not to
 * this low-level value object.
 *
 * Encoding law:
 *
 * - fixed-width signed 64-bit integer;
 * - big-endian;
 * - decimal String rendering is diagnostic only.
 *
 * Hash law:
 *
 * hashCode() folds the 64-bit value into 32 bits using the standard xor-shift
 * pattern. This is for in-memory equality collections only.
 *
 * Do not use hashCode() as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime protocol hash;
 * - serialized seed digest.
 */
class RootTimeEpochMillis private constructor(
    val value: Long,
) {
    fun writeFixedInt64BigEndian(
        destination: ByteArray,
        offset: Int,
    ) {
        /*
         * Overflow-safe bounds check.
         *
         * Avoid:
         *
         *     offset + 8 > destination.size
         *
         * because offset + 8 can overflow when offset is close to Int.MAX_VALUE.
         */
        if (offset < 0 || offset > destination.size - FIXED_INT64_BYTES) {
            throw TypeExpansionContractViolationException(
                reason = "Cannot write RootTimeEpochMillis at offset=$offset " +
                        "into destination size=${destination.size}",
            )
        }

        destination[offset] = ((value ushr 56) and 0xffL).toByte()
        destination[offset + 1] = ((value ushr 48) and 0xffL).toByte()
        destination[offset + 2] = ((value ushr 40) and 0xffL).toByte()
        destination[offset + 3] = ((value ushr 32) and 0xffL).toByte()
        destination[offset + 4] = ((value ushr 24) and 0xffL).toByte()
        destination[offset + 5] = ((value ushr 16) and 0xffL).toByte()
        destination[offset + 6] = ((value ushr 8) and 0xffL).toByte()
        destination[offset + 7] = (value and 0xffL).toByte()
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        return other is RootTimeEpochMillis &&
                value == other.value
    }

    override fun hashCode(): Int {
        return (value xor (value ushr 32)).toInt()
    }

    override fun toString(): String {
        return "epochMillis($value)"
    }

    companion object {
        private const val FIXED_INT64_BYTES: Int = 8

        @JvmStatic
        fun of(
            value: Long,
        ): RootTimeEpochMillis {
            if (value < 0L) {
                throw TypeExpansionContractViolationException(
                    reason = "RootTimeEpochMillis must be >= 0: $value",
                )
            }

            return RootTimeEpochMillis(value)
        }

        @JvmStatic
        fun fixedWidthBytes(): Int {
            return FIXED_INT64_BYTES
        }
    }
}
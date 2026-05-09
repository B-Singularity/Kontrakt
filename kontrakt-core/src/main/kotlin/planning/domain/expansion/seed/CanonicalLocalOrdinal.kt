package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Local ordinal after canonical ordering.
 *
 * This is not a value class.
 *
 * Rationale:
 *
 * - This object is a boundary value object, not the primitive counter used inside
 *   tight loops.
 * - Hot loops should carry Int locally and publish CanonicalLocalOrdinal only at
 *   the domain boundary.
 * - This preserves explicit domain type safety without forcing heap allocation
 *   into every primitive counting step.
 *
 * Allocation law:
 *
 * - zero() returns a singleton.
 * - of(0) returns the same singleton.
 * - plus(0) returns this.
 * - plus(n > 0) returns a new immutable value object.
 *
 * Do not introduce a general small-value cache here yet. That belongs to the
 * later flyweight / interning / allocation-policy phase.
 *
 * Canonical encoding law:
 *
 * - fixed-width signed 32-bit integer;
 * - big-endian;
 * - decimal String rendering is diagnostics only.
 *
 * Hash law:
 *
 * hashCode() returns the integer value for in-memory equality collections only.
 * It must not be used as a persisted fingerprint, route key, canonical digest,
 * or cross-runtime order hash.
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

        if (increment == 0) {
            return this
        }

        val result = value.toLong() + increment.toLong()

        if (result > MAX_LOCAL_ORDINAL) {
            throw TypeExpansionContractViolationException(
                reason =
                    "CanonicalLocalOrdinal overflow: " +
                            "current=$value, increment=$increment, max=$MAX_LOCAL_ORDINAL",
            )
        }

        return CanonicalLocalOrdinal(result.toInt())
    }

    /**
     * Returns primitive Int after checked addition.
     *
     * Use this in inner loops when the caller wants to avoid allocating a new
     * CanonicalLocalOrdinal for every increment step.
     *
     * The caller should wrap the final result with CanonicalLocalOrdinal.of(...)
     * at the domain boundary.
     */
    fun plusToInt(increment: Int): Int {
        if (increment < 0) {
            throw TypeExpansionContractViolationException(
                reason = "CanonicalLocalOrdinal increment must be >= 0: $increment",
            )
        }

        if (increment == 0) {
            return value
        }

        val result = value.toLong() + increment.toLong()

        if (result > MAX_LOCAL_ORDINAL) {
            throw TypeExpansionContractViolationException(
                reason =
                    "CanonicalLocalOrdinal overflow: " +
                            "current=$value, increment=$increment, max=$MAX_LOCAL_ORDINAL",
            )
        }

        return result.toInt()
    }

    fun writeFixedInt32BigEndian(
        destination: ByteArray,
        offset: Int,
    ) {
        /*
         * Overflow-safe bounds check.
         *
         * Avoid `offset + 4 > destination.size` because offset + 4 can overflow
         * for very large positive offsets.
         */
        if (offset < 0 || offset > destination.size - FIXED_INT32_BYTES) {
            throw TypeExpansionContractViolationException(
                reason =
                    "Cannot write CanonicalLocalOrdinal at offset=$offset " +
                            "into destination size=${destination.size}",
            )
        }

        destination[offset] = ((value ushr 24) and 0xff).toByte()
        destination[offset + 1] = ((value ushr 16) and 0xff).toByte()
        destination[offset + 2] = ((value ushr 8) and 0xff).toByte()
        destination[offset + 3] = (value and 0xff).toByte()
    }

    override fun equals(other: Any?): Boolean =
        other is CanonicalLocalOrdinal &&
                value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = "ord($value)"

    companion object {
        private const val MAX_LOCAL_ORDINAL: Int = 1_000_000
        private const val FIXED_INT32_BYTES: Int = 4

        private val ZERO = CanonicalLocalOrdinal(0)

        @JvmStatic
        fun of(value: Int): CanonicalLocalOrdinal {
            if (value < 0 || value > MAX_LOCAL_ORDINAL) {
                throw TypeExpansionContractViolationException(
                    reason =
                        "CanonicalLocalOrdinal out of range: " +
                                "value=$value, max=$MAX_LOCAL_ORDINAL",
                )
            }

            if (value == 0) {
                return ZERO
            }

            return CanonicalLocalOrdinal(value)
        }

        @JvmStatic
        fun zero(): CanonicalLocalOrdinal = ZERO

        @JvmStatic
        fun maxValue(): Int = MAX_LOCAL_ORDINAL
    }
}

package stage.normalization.contract

/**
 * Shared deterministic ordering primitives for metamodel order values.
 *
 * This is not:
 *
 * - locale collation;
 * - Unicode collation;
 * - canonical byte encoding;
 * - a persisted fingerprint;
 * - or a planning-domain text law.
 *
 * These functions exist only to prevent each VO from re-implementing the same
 * low-level comparison loops.
 *
 * Caller responsibility:
 *
 * Each caller must document whether its input is:
 *
 * - ASCII-only order material;
 * - NFC-ratified Unicode material;
 * - or already-canonical metamodel text.
 *
 * This object does not validate inputs. It only compares them.
 */
object MetamodelProtocolOrdering {
    /**
     * Compares two strings by Kotlin/JVM UTF-16 code units.
     *
     * This is deterministic and locale-independent.
     *
     * It deliberately does not call String.compareTo(...) so the order law is
     * explicit at the metamodel layer.
     */
    fun compareUtf16CodeUnits(
        left: String,
        right: String,
    ): Int {
        val n1 = left.length
        val n2 = right.length
        val minLength = if (n1 < n2) n1 else n2

        var index = 0
        while (index < minLength) {
            val c1 = left[index]
            val c2 = right[index]

            if (c1 != c2) {
                return c1.code - c2.code
            }

            index += 1
        }

        return n1 - n2
    }

    fun compareBoolean(
        left: Boolean,
        right: Boolean,
    ): Int {
        if (left == right) return 0
        return if (!left && right) -1 else 1
    }

    fun compareInt(
        left: Int,
        right: Int,
    ): Int =
        when {
            left < right -> -1
            left > right -> 1
            else -> 0
        }

    fun compareLong(
        left: Long,
        right: Long,
    ): Int =
        when {
            left < right -> -1
            left > right -> 1
            else -> 0
        }

    fun compareNullableInt(
        left: Int?,
        right: Int?,
    ): Int {
        if (left == null && right == null) return 0
        if (left == null) return -1
        if (right == null) return 1
        return compareInt(left, right)
    }

    fun compareNullableProtocolOrder(
        left: Int?,
        right: Int?,
    ): Int = compareNullableInt(left, right)
}

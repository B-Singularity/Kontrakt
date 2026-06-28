package stage.lowering.material.projection

import stage.lowering.diagnostics.ActiveMemberProjectionException

/**
 * Small deterministic ordering/rendering primitives used by projection-domain keys.
 *
 * This object keeps generic helper names out of the package-level namespace.
 *
 * These helpers are mechanical primitives only.
 * Semantic authority remains in explicit key tuples, duplicate-key rules, and
 * fail-fast ambiguity checks.
 */
internal object ProjectionOrderingPrimitives {
    fun renderField(
        value: String,
    ): String {
        return value.length.toString() + ":" + value
    }

    fun compareStrings(
        left: String,
        right: String,
    ): Int = left.compareTo(right)

    fun compareLongs(
        left: Long,
        right: Long,
    ): Int = java.lang.Long.compare(left, right)

    fun compareInts(
        left: Int,
        right: Int,
    ): Int = Integer.compare(left, right)

    fun memberKindRank(value: MemberKind): Int =
        when (value) {
            MemberKind.CTOR_PARAM -> 0
            MemberKind.PROPERTY -> 1
        }

    fun takeIfNonZero(value: Int): Int? = if (value != 0) value else null

    fun requireCanonicalComponentShape(
        field: String,
        value: String,
    ) {
        if (value.isBlank()) {
            throw ActiveMemberProjectionException("$field must not be blank.")
        }

        if (value.contains('|')) {
            throw ActiveMemberProjectionException(
                "$field must not contain reserved delimiter '|': $value",
            )
        }

        var i = 0
        while (i < value.length) {
            val ch = value[i]
            if (ch.isISOControl()) {
                throw ActiveMemberProjectionException(
                    "$field must not contain ISO control characters.",
                )
            }
            i++
        }
    }
}

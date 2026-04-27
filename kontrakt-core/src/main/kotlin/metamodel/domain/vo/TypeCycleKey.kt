package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Cycle-detection structural key.
 *
 * Cycle identity law:
 * - all nullability markers are stripped before this value is issued;
 * - reified generic structure remains;
 * - backend/binary names are forbidden;
 * - source variance syntax must already be lowered.
 *
 * This class does not parse raw type syntax.
 * It accepts only already-lowered structural cycle material.
 */
class TypeCycleKey private constructor(
    val value: String,
    val shapeSummary: TypeShapeSummary,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeCycleKey) return false

        return value == other.value &&
                shapeSummary == other.shapeSummary
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + shapeSummary.hashCode()
        return result
    }

    override fun toString(): String {
        return value
    }

    companion object {
        @JvmStatic
        fun issue(
            value: String,
            shapeSummary: TypeShapeSummary,
        ): TypeCycleKey {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "TypeCycleKey.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectJvmBinaryDescriptor(
                field = "TypeCycleKey.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectWhitespace(
                field = "TypeCycleKey.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectNullableMarker(
                field = "TypeCycleKey.value",
                value = value,
            )
            CanonicalTypeTextGuards.requireNoRawVarianceMarker(
                field = "TypeCycleKey.value",
                value = value,
            )

            return TypeCycleKey(
                value = value,
                shapeSummary = shapeSummary,
            )
        }
    }
}
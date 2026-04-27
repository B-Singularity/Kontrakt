package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Canonical type-reference signature.
 *
 * This is not CanonicalPlanNode signature.
 * It is canonical type-reference signature material.
 *
 * The format must already be lowered by TypeReferenceFactory / adapter bridge.
 * This VO guards the lowered surface and carries a shape summary to avoid
 * reparsing signature text in planning hot paths.
 */
class CanonicalTypeSignature private constructor(
    val value: String,
    val shapeSummary: TypeShapeSummary,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanonicalTypeSignature) return false

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
        ): CanonicalTypeSignature {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "CanonicalTypeSignature.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectJvmBinaryDescriptor(
                field = "CanonicalTypeSignature.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectWhitespace(
                field = "CanonicalTypeSignature.value",
                value = value,
            )
            CanonicalTypeTextGuards.requireNoRawVarianceMarker(
                field = "CanonicalTypeSignature.value",
                value = value,
            )

            return CanonicalTypeSignature(
                value = value,
                shapeSummary = shapeSummary,
            )
        }
    }
}
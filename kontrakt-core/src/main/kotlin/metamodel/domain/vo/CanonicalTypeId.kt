package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Canonical type identity.
 *
 * This is not a runtime Class name, not a JVM descriptor, and not an adapter
 * binary name. It is domain-issued identity material.
 *
 * Interning is intentionally not performed inside this VO. Session/global
 * interning belongs to TypeReferenceFactory / metamodel interner so that object
 * lifetime and memory ownership remain explicit.
 */
class CanonicalTypeId private constructor(
    val value: String,
    val shapeSummary: TypeShapeSummary,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanonicalTypeId) return false

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
        ): CanonicalTypeId {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "CanonicalTypeId.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectJvmBinaryDescriptor(
                field = "CanonicalTypeId.value",
                value = value,
            )
            CanonicalTypeTextGuards.rejectWhitespace(
                field = "CanonicalTypeId.value",
                value = value,
            )

            return CanonicalTypeId(
                value = value,
                shapeSummary = shapeSummary,
            )
        }
    }
}
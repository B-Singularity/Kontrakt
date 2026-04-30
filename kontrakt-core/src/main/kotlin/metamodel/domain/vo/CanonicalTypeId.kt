package metamodel.domain.vo

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Canonical type identity.
 *
 * This is not a runtime Class name, not a JVM descriptor, and not an adapter
 * binary name. It is domain-issued identity material.
 *
 * Equality is value-primary.
 *
 * shapeSummary is coherence metadata, not an additional equality axis.
 * Same text with different shape is forbidden by TypeShapeRatification.
 *
 * This VO deliberately does not cache hashCode and does not implement interning.
 * Type identity interning and eviction policy belong to the later planning
 * cache/memory-governance stage.
 */
class CanonicalTypeId private constructor(
    val text: CanonicalTypeText,
    val shapeSummary: TypeShapeSummary,
    val shapeRatification: TypeShapeRatification,
) {
    val value: String
        get() = text.value

    override fun equals(other: Any?): Boolean {
        return other is CanonicalTypeId && text.value == other.text.value
    }

    override fun hashCode(): Int {
        return text.value.hashCode()
    }

    override fun toString(): String {
        return text.value
    }

    companion object {
        @JvmStatic
        fun issue(
            text: CanonicalTypeText,
            shapeSummary: TypeShapeSummary,
            shapeRatification: TypeShapeRatification,
        ): CanonicalTypeId {
            shapeRatification.requireMatches(
                text = text,
                shapeSummary = shapeSummary,
                reason = "CanonicalTypeId requires shape-ratified text.",
            )

            return CanonicalTypeId(
                text = text,
                shapeSummary = shapeSummary,
                shapeRatification = shapeRatification,
            )
        }
    }
}

/**
 * Factory-issued proof that one canonical type text is bound to one shape
 * summary under a pinned classifier.
 *
 * This is the defense against classification drift:
 *
 * - same text, different shape;
 * - same text, different classifier version;
 * - adapter A says COLLECTION while adapter B says ATOMIC.
 *
 * This object does not compute the classification. TypeReferenceFactory or an
 * equivalent metamodel ratifier issues it after consulting normalized metadata.
 */
class TypeShapeRatification private constructor(
    val text: CanonicalTypeText,
    val shapeSummary: TypeShapeSummary,
    val classifierId: String,
    val classifierVersion: String,
    val ratificationToken: String,
) {
    fun requireMatches(
        text: CanonicalTypeText,
        shapeSummary: TypeShapeSummary,
        reason: String,
    ) {
        if (this.text != text) {
            throw TypeExpansionContractViolationException(
                reason = "$reason Text mismatch: expected=${this.text.value}, actual=${text.value}",
            )
        }

        if (this.shapeSummary != shapeSummary) {
            throw TypeExpansionContractViolationException(
                reason = "$reason Shape mismatch: expected=${this.shapeSummary}, actual=$shapeSummary",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeRatification) return false

        return text == other.text &&
                shapeSummary == other.shapeSummary &&
                classifierId == other.classifierId &&
                classifierVersion == other.classifierVersion &&
                ratificationToken == other.ratificationToken
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + shapeSummary.hashCode()
        result = 31 * result + classifierId.hashCode()
        result = 31 * result + classifierVersion.hashCode()
        result = 31 * result + ratificationToken.hashCode()
        return result
    }

    override fun toString(): String {
        return buildString {
            append("TypeShapeRatification(")
            append("text=")
            append(text.value)
            append(", shapeSummary=")
            append(shapeSummary)
            append(", classifierId=")
            append(classifierId)
            append(", classifierVersion=")
            append(classifierVersion)
            append(", token=<redacted>")
            append(')')
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            text: CanonicalTypeText,
            shapeSummary: TypeShapeSummary,
            classifierId: String,
            classifierVersion: String,
            ratificationToken: String,
        ): TypeShapeRatification {
            requireComponent("classifierId", classifierId)
            requireComponent("classifierVersion", classifierVersion)
            requireComponent("ratificationToken", ratificationToken)

            return TypeShapeRatification(
                text = text,
                shapeSummary = shapeSummary,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
                ratificationToken = ratificationToken,
            )
        }

        private fun requireComponent(
            field: String,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not be empty.",
                )
            }

            if (value.contains('|')) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not contain reserved delimiter '|': $value",
                )
            }
        }
    }
}
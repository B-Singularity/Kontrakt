package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

class AnnotationQualifiedName private constructor(
    val value: String,
) {
    override fun equals(other: Any?): Boolean {
        return other is AnnotationQualifiedName && value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

    companion object {
        @JvmStatic
        fun issue(value: String): AnnotationQualifiedName {
            CanonicalTextLaw.validateCanonicalQualifiedIdentifier(
                field = "AnnotationQualifiedName.value",
                value = value,
            )

            return AnnotationQualifiedName(value)
        }
    }
}
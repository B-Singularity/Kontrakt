package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

class AnnotationArgumentName private constructor(
    val value: String,
) {
    override fun equals(other: Any?): Boolean {
        return other is AnnotationArgumentName && value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

    companion object {
        @JvmStatic
        fun issue(value: String): AnnotationArgumentName {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "AnnotationArgumentName.value",
                value = value,
            )

            return AnnotationArgumentName(value)
        }
    }
}
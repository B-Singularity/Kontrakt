package metamodel.domain.vo

class AnnotationValueEntry private constructor(
    val name: AnnotationArgumentName,
    val value: AnnotationValue,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationValueEntry) return false

        return name == other.name && value == other.value
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "$name=$value"
    }

    companion object {
        @JvmStatic
        fun issue(
            name: AnnotationArgumentName,
            value: AnnotationValue,
        ): AnnotationValueEntry {
            return AnnotationValueEntry(
                name = name,
                value = value,
            )
        }
    }
}
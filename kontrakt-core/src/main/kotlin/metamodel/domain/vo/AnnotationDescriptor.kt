package metamodel.domain.vo

/**
 * Immutable canonical annotation descriptor.
 *
 * Repeatable annotations are distinguished by both qualified name and values.
 */
class AnnotationDescriptor private constructor(
    val qualifiedName: AnnotationQualifiedName,
    val values: AnnotationValueMap,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationDescriptor) return false

        return qualifiedName == other.qualifiedName &&
                values == other.values
    }

    override fun hashCode(): Int {
        var result = qualifiedName.hashCode()
        result = 31 * result + values.hashCode()
        return result
    }

    override fun toString(): String {
        return "AnnotationDescriptor(qualifiedName=$qualifiedName, values=$values)"
    }

    companion object {
        @JvmStatic
        fun issue(
            qualifiedName: AnnotationQualifiedName,
            values: AnnotationValueMap,
        ): AnnotationDescriptor {
            return AnnotationDescriptor(
                qualifiedName = qualifiedName,
                values = values,
            )
        }
    }
}
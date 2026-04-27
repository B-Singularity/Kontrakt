package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw
import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Deterministically ordered use-site annotations.
 *
 * Repeatable annotations are allowed when their values differ.
 * Exact duplicate annotation descriptors are rejected.
 */
class OrderedUseSiteAnnotations private constructor(
    private val elements: Array<AnnotationDescriptor>,
) : AbstractList<AnnotationDescriptor>() {

    override val size: Int
        get() = elements.size

    override fun get(index: Int): AnnotationDescriptor {
        return elements[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderedUseSiteAnnotations) return false

        return elements.contentEquals(other.elements)
    }

    override fun hashCode(): Int {
        return elements.contentHashCode()
    }

    override fun toString(): String {
        return "OrderedUseSiteAnnotations(size=${elements.size})"
    }

    companion object {
        private val EMPTY = OrderedUseSiteAnnotations(emptyArray())

        private val ORDER: Comparator<AnnotationDescriptor> =
            Comparator { left, right ->
                val name = CanonicalTextLaw.compareCanonicalIdentifiers(
                    left.qualifiedName.value,
                    right.qualifiedName.value,
                )

                if (name != 0) {
                    return@Comparator name
                }

                compareAnnotationValues(left.values, right.values)
            }

        @JvmStatic
        fun empty(): OrderedUseSiteAnnotations {
            return EMPTY
        }

        @JvmStatic
        fun issue(annotations: Collection<AnnotationDescriptor>): OrderedUseSiteAnnotations {
            if (annotations.isEmpty()) {
                return EMPTY
            }

            val buffer = annotations.toTypedArray()
            buffer.sortWith(ORDER)

            var i = 1
            while (i < buffer.size) {
                val previous = buffer[i - 1]
                val current = buffer[i]

                if (previous == current) {
                    throw TypeExpansionContractViolationException(
                        reason = "Duplicate use-site annotation descriptor: ${current.qualifiedName.value}",
                    )
                }

                i++
            }

            return OrderedUseSiteAnnotations(buffer.copyOf())
        }

        private fun compareAnnotationValues(
            left: AnnotationValueMap,
            right: AnnotationValueMap,
        ): Int {
            val sizeCompare = left.size.compareTo(right.size)
            if (sizeCompare != 0) {
                return sizeCompare
            }

            var i = 0
            while (i < left.size) {
                val leftEntry = left[i]
                val rightEntry = right[i]

                val name = CanonicalTextLaw.compareCanonicalIdentifiers(
                    leftEntry.name.value,
                    rightEntry.name.value,
                )

                if (name != 0) {
                    return name
                }

                val value = AnnotationValue.compare(leftEntry.value, rightEntry.value)
                if (value != 0) {
                    return value
                }

                i++
            }

            return 0
        }
    }
}
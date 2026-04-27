package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw
import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Deterministic annotation argument map.
 *
 * This is not backed by HashMap.
 * Entries are ordered by argument name under canonical identifier order.
 */
class AnnotationValueMap private constructor(
    private val entries: Array<AnnotationValueEntry>,
) : AbstractList<AnnotationValueEntry>() {

    override val size: Int
        get() = entries.size

    override fun get(index: Int): AnnotationValueEntry {
        return entries[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationValueMap) return false

        return entries.contentEquals(other.entries)
    }

    override fun hashCode(): Int {
        return entries.contentHashCode()
    }

    override fun toString(): String {
        return entries.joinToString(prefix = "AnnotationValueMap(", postfix = ")")
    }

    companion object {
        private val ORDER: Comparator<AnnotationValueEntry> =
            Comparator { left, right ->
                CanonicalTextLaw.compareCanonicalIdentifiers(
                    left.name.value,
                    right.name.value,
                )
            }

        private val EMPTY = AnnotationValueMap(emptyArray())

        @JvmStatic
        fun empty(): AnnotationValueMap {
            return EMPTY
        }

        @JvmStatic
        fun issue(entries: Collection<AnnotationValueEntry>): AnnotationValueMap {
            if (entries.isEmpty()) {
                return EMPTY
            }

            val buffer = entries.toTypedArray()
            buffer.sortWith(ORDER)

            var i = 1
            while (i < buffer.size) {
                val previous = buffer[i - 1]
                val current = buffer[i]

                if (ORDER.compare(previous, current) == 0) {
                    throw TypeExpansionContractViolationException(
                        reason = "Duplicate annotation argument: ${current.name.value}",
                    )
                }

                i++
            }

            return AnnotationValueMap(buffer.copyOf())
        }
    }
}
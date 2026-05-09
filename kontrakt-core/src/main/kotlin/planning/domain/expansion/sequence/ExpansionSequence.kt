package planning.domain.expansion.sequence

import planning.domain.exception.TypeExpansionContractViolationException
import java.util.AbstractList

/**
 * Sorts by the supplied order comparator and rejects comparator ties.
 *
 * The comparator must be a Kontrakt-owned deterministic comparator.
 *
 * Forbidden comparator sources:
 * - locale collation;
 * - backend enumeration order;
 * - JVM identity hash;
 * - object address;
 * - mutable runtime state;
 * - platform default string ordering unless wrapped by a ratified Kontrakt law.
 *
 * Comparator tie means one of:
 * - duplicate semantic material;
 * - ambiguous canonical ordering;
 * - malformed caller-provided comparator law.
 */
class ExpansionSequence<T : Any> private constructor(
    private val elements: Array<Any?>,
) : AbstractList<T>() {
    override val size: Int
        get() = elements.size

    @Suppress("UNCHECKED_CAST")
    override fun get(index: Int): T {
        if (index < 0 || index >= elements.size) {
            throw TypeExpansionContractViolationException(
                reason = "ExpansionSequence index out of range: index=$index, size=${elements.size}",
            )
        }

        return elements[index] as T
    }

    fun copyTo(destination: MutableCollection<T>) {
        var i = 0
        while (i < elements.size) {
            @Suppress("UNCHECKED_CAST")
            destination.add(elements[i] as T)
            i++
        }
    }

    companion object {
        @JvmStatic
        fun <T : Any> empty(): ExpansionSequence<T> = ExpansionSequence(emptyArray())

        /**
         * Freezes caller-supplied already ordered elements.
         *
         * This method does not sort.
         *
         * Use this only after the caller has already applied the relevant
         * order ordering law.
         */
        @JvmStatic
        fun <T : Any> alreadyOrdered(elements: Collection<T>): ExpansionSequence<T> {
            val snapshot = arrayOfNulls<Any?>(elements.size)
            val iterator = elements.iterator()

            var index = 0
            while (iterator.hasNext()) {
                if (index >= snapshot.size) {
                    throw TypeExpansionContractViolationException(
                        reason = "Collection grew during ExpansionSequence capture.",
                    )
                }

                val element: Any? = iterator.next()
                if (element == null) {
                    throw TypeExpansionContractViolationException(
                        reason = "ExpansionSequence does not allow null elements.",
                    )
                }

                snapshot[index] = element
                index++
            }

            if (index != snapshot.size) {
                throw TypeExpansionContractViolationException(
                    reason = "Collection shrank during ExpansionSequence capture: expected=${snapshot.size}, actual=$index.",
                )
            }

            return ExpansionSequence(snapshot)
        }

        /**
         * Sorts by the supplied comparator and rejects comparator ties.
         *
         * Comparator tie means one of:
         * - duplicate semantic material;
         * - ambiguous canonical ordering;
         * - malformed caller-provided comparator law.
         */
        @JvmStatic
        fun <T : Any> orderedStrict(
            elements: Collection<T>,
            comparator: Comparator<in T>,
            duplicateMessage: (T, T) -> String,
        ): ExpansionSequence<T> {
            val buffer = ArrayList<T>(elements.size)
            val iterator = elements.iterator()

            while (iterator.hasNext()) {
                val element: Any? = iterator.next()
                if (element == null) {
                    throw TypeExpansionContractViolationException(
                        reason = "ExpansionSequence does not allow null elements.",
                    )
                }

                @Suppress("UNCHECKED_CAST")
                buffer.add(element as T)
            }

            buffer.sortWith(comparator)

            var i = 1
            while (i < buffer.size) {
                val previous = buffer[i - 1]
                val current = buffer[i]

                if (comparator.compare(previous, current) == 0) {
                    throw TypeExpansionContractViolationException(
                        reason = duplicateMessage(previous, current),
                    )
                }

                i++
            }

            return alreadyOrdered(buffer)
        }
    }
}

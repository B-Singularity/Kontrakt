package ir.structure

import ir.IrLimits
import ir.exception.IrProtocolViolationException
import java.util.Collections
import java.util.TreeSet

/**
 * Deterministic, deeply immutable list wrapper.
 *
 * Guarantees:
 * - Total order (sorted)
 * - Deduplication (set semantics at construction time)
 * - Unmodifiable backing storage
 *
 * Why:
 * - Contributors cannot "forget to sort", "forget to copy", or "leak mutability".
 * - Deterministic iteration is required for stable cache keys and canonicalization.
 *
 * @param T must be Comparable to define a stable global order.
 */
class DeterministicList<T : Comparable<T>> private constructor(
    private val delegate: List<T>
) : List<T> by delegate {

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()

    companion object {
        /**
         * Normalizing factory with streaming ingestion.
         *
         * Enforces:
         * - Ingestion DoS cap (input.size <= limit * multiplier)
         * - Distinct + Sorted
         * - Unique limit check
         * - Deep immutability via defensive copy + unmodifiable view
         */
        fun <T : Comparable<T>> of(
            elements: Collection<T>,
            limit: Int,
            inputMultiplier: Int = IrLimits.DETERMINISTIC_INPUT_MULTIPLIER
        ): DeterministicList<T> {

            val inputLimit = limit * inputMultiplier
            if (elements.size > inputLimit) {
                throw IrProtocolViolationException("DeterministicList ingestion limit exceeded ($inputLimit).")
            }

            val uniqueSorted = TreeSet<T>()
            for (element in elements) {
                uniqueSorted.add(element)
                if (uniqueSorted.size > limit) {
                    throw IrProtocolViolationException("DeterministicList unique limit exceeded ($limit).")
                }
            }

            val safeList = Collections.unmodifiableList(ArrayList(uniqueSorted))
            return DeterministicList(safeList)
        }
    }
}
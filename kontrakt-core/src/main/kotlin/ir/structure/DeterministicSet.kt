package ir.structure

import ir.IrLimits
import ir.exception.IrProtocolViolationException
import java.util.Collections
import java.util.TreeSet

/**
 * Deterministic, deeply immutable set wrapper.
 *
 * Guarantees:
 * - Total order (TreeSet)
 * - Deduplication (set semantics)
 * - Unmodifiable backing storage
 *
 * @param T must be Comparable for deterministic ordering.
 */
class DeterministicSet<T : Comparable<T>> private constructor(
    private val delegate: Set<T>
) : Set<T> by delegate {

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()

    companion object {
        fun <T : Comparable<T>> of(
            elements: Collection<T>,
            limit: Int,
            inputMultiplier: Int = IrLimits.DETERMINISTIC_INPUT_MULTIPLIER
        ): DeterministicSet<T> {

            val inputLimit = limit * inputMultiplier
            if (elements.size > inputLimit) {
                throw IrProtocolViolationException("DeterministicSet ingestion limit exceeded ($inputLimit).")
            }

            val uniqueSorted = TreeSet<T>()
            for (e in elements) {
                uniqueSorted.add(e)
                if (uniqueSorted.size > limit) {
                    throw IrProtocolViolationException("DeterministicSet unique limit exceeded ($limit).")
                }
            }

            val safeSet = Collections.unmodifiableSet(uniqueSorted)
            return DeterministicSet(safeSet)
        }
    }
}
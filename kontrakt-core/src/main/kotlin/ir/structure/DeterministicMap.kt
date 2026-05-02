package ir.structure

import ir.exception.IrProtocolViolationException
import java.util.Collections
import java.util.TreeMap

/**
 * Deterministic, deeply immutable map wrapper.
 *
 * Guarantees:
 * - Key-sorted iteration order (TreeMap normalization)
 * - Entry limit enforcement
 * - Optional per-value validation hook
 * - Unmodifiable backing storage
 *
 * Why:
 * - Prevents accidental HashMap/LinkedHashMap usage.
 * - Ensures stable iteration and canonical serialization order.
 *
 * @param K must be Comparable for stable ordering.
 */
class DeterministicMap<K : Comparable<K>, V> private constructor(
    private val delegate: Map<K, V>,
) : Map<K, V> by delegate {
    override fun equals(other: Any?): Boolean = delegate == other

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()

    companion object {
        /**
         * Streaming Normalization Factory.
         * Enforces:
         * - Entry limit check
         * - Value validation hook (e.g., string length check)
         * - TreeMap normalization (sorted keys)
         * - Unmodifiable
         */
        fun <K : Comparable<K>, V> of(
            input: Map<K, V>,
            limit: Int,
            valueValidator: (V) -> Unit = {},
        ): DeterministicMap<K, V> {
            if (input.size > limit) {
                throw IrProtocolViolationException("DeterministicMap entry limit exceeded ($limit).")
            }

            input.values.forEach(valueValidator)

            val normalized = TreeMap<K, V>()
            normalized.putAll(input)

            val safeMap = Collections.unmodifiableMap(normalized)
            return DeterministicMap(safeMap)
        }
    }
}

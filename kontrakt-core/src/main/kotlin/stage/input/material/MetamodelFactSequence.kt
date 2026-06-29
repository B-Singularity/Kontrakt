package stage.input.material

import stage.input.diagnostics.DuplicateMetamodelFactException
import stage.input.diagnostics.InvalidTypeFactShapeException
import java.util.AbstractList

/**
 * Immutable deterministic sequence for metamodel raw-fact boundaries.
 *
 * This is NOT a set.
 * This type never deduplicates input silently.
 *
 * Purpose:
 * - defensive copy
 * - deterministic ordering
 * - immutable indexed access
 * - explicit duplicate failure where the caller declares a uniqueness key
 *
 * Why not realization.graph.structure.DeterministicList?
 * - realization.graph.structure.DeterministicList has sorted-set semantics.
 * - It deduplicates through TreeSet.
 * - Raw metamodel facts must not hide adapter duplication by deduplication.
 *
 * DDD role:
 * - metamodel-domain collection value object
 *
 * Hexagonal role:
 * - protects the outbound raw-fact boundary from mutable or unstable adapter collections
 *
 * Compiler-style role:
 * - turns backend-discovered facts into a deterministic sequence before semantic projection
 */
class MetamodelFactSequence<T> private constructor(
    private val snapshot: Array<Any?>,
) : AbstractList<T>(),
    RandomAccess {
    override val size: Int
        get() = snapshot.size

    @Suppress("UNCHECKED_CAST")
    override fun get(index: Int): T = snapshot[index] as T

    companion object {
        /**
         * Builds a deterministic sequence by sorting with a strict total comparator.
         *
         * This factory does not deduplicate.
         *
         * If the comparator reports equality for two adjacent elements after sorting,
         * the comparator is not strict enough for deterministic fact sequencing and
         * the factory fails closed.
         */
        @JvmStatic
        fun <T> orderedByStrict(
            owner: String,
            factKind: String,
            elements: Collection<T>,
            comparator: Comparator<in T>,
        ): MetamodelFactSequence<T> {
            val buffer = copyElements(elements)

            buffer.sortWith(comparator)

            requireNoComparatorTies(
                owner = owner,
                factKind = factKind,
                buffer = buffer,
                comparator = comparator,
            )

            return fromOrderedBuffer(buffer)
        }

        /**
         * Builds a deterministic sequence by:
         *
         * 1. copying input,
         * 2. sorting the same buffer by the declared duplicate key,
         * 3. checking adjacent duplicate keys in O(N log N),
         * 4. sorting the same buffer again by the final deterministic ordering comparator,
         * 5. verifying the final comparator has no ties.
         *
         * This factory never removes duplicates.
         *
         * Duplicate-key validation intentionally happens before final comparator-tie
         * validation so true raw fact duplication is reported as
         * DuplicateMetamodelFactException, not as a misleading comparator-shape error.
         *
         * K is constrained to Any so null keys cannot silently enter deterministic
         * key comparison. Unknown/unavailable facts must be represented explicitly
         * in the key value, not by null.
         */
        @JvmStatic
        fun <T, K : Any> orderedUniqueBy(
            owner: String,
            factKind: String,
            duplicateKeyName: String,
            elements: Collection<T>,
            orderingComparator: Comparator<in T>,
            keyOf: (T) -> K,
            keyComparator: Comparator<in K>,
            keyToString: (K) -> String,
        ): MetamodelFactSequence<T> {
            val buffer = copyElements(elements)

            sortByKeyAndRequireUniqueAdjacentKeys(
                owner = owner,
                factKind = factKind,
                duplicateKeyName = duplicateKeyName,
                buffer = buffer,
                keyOf = keyOf,
                keyComparator = keyComparator,
                keyToString = keyToString,
            )

            buffer.sortWith(orderingComparator)

            requireNoComparatorTies(
                owner = owner,
                factKind = factKind,
                buffer = buffer,
                comparator = orderingComparator,
            )

            return fromOrderedBuffer(buffer)
        }

        /**
         * Builds a deterministic constructor-local compact-index sequence.
         *
         * Required shape:
         * - empty collection is valid
         * - non-empty collection must have exactly N distinct indices for N elements
         * - the valid index range is exactly 0..N-1
         * - duplicate index fails closed
         *
         * This is used for constructor parameters.
         */
        @JvmStatic
        fun <T> compactIndexedBy(
            owner: String,
            factKind: String,
            indexName: String,
            elements: Collection<T>,
            indexOf: (T) -> Int,
        ): MetamodelFactSequence<T> {
            val buffer = copyElements(elements)

            if (buffer.isEmpty()) {
                return fromOrderedBuffer(buffer)
            }

            val seen = BooleanArray(buffer.size)

            var i = 0
            while (i < buffer.size) {
                val index = indexOf(buffer[i])

                if (index < 0 || index >= buffer.size) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = factKind,
                        reason =
                            "$indexName must form a compact range with exactly " +
                                    "${buffer.size} distinct indices for ${buffer.size} elements; " +
                                    "valid range is 0..${buffer.size - 1}, actual=$index",
                    )
                }

                if (seen[index]) {
                    throw DuplicateMetamodelFactException(
                        owner = owner,
                        factKind = factKind,
                        duplicateKey = "$indexName=$index",
                        reason =
                            "Duplicate compact index in metamodel fact sequence. " +
                                    "Compact range requires exactly one fact per index.",
                    )
                }

                seen[index] = true
                i++
            }

            buffer.sortWith(
                Comparator { left, right ->
                    Integer.compare(indexOf(left), indexOf(right))
                },
            )

            return fromOrderedBuffer(buffer)
        }

        /**
         * Freezes a sequence that the caller claims is already order-ordered.
         *
         * Unlike a blind "alreadyOrdered" constructor, this method verifies that the
         * provided collection is strictly ascending according to the supplied comparator.
         *
         * This method exists for boundaries where sorting would hide a order bug.
         * Most DTO factories should prefer orderedByStrict / orderedUniqueBy /
         * compactIndexedBy.
         */
        @JvmStatic
        fun <T> verifiedAlreadyOrdered(
            owner: String,
            factKind: String,
            elements: Collection<T>,
            comparator: Comparator<in T>,
        ): MetamodelFactSequence<T> {
            val buffer = copyElements(elements)

            requireStrictAscendingOrder(
                owner = owner,
                factKind = factKind,
                buffer = buffer,
                comparator = comparator,
            )

            return fromOrderedBuffer(buffer)
        }

        private fun <T> copyElements(elements: Collection<T>): ArrayList<T> {
            val buffer = ArrayList<T>(elements.size)
            val iterator = elements.iterator()
            while (iterator.hasNext()) {
                buffer.add(iterator.next())
            }
            return buffer
        }

        /**
         * Sorts the given buffer by duplicate key and performs adjacent duplicate scan.
         *
         * This intentionally mutates the provided buffer to avoid an unnecessary copy.
         * The public orderedUniqueBy flow sorts the same buffer again by the final
         * ordering comparator after duplicate validation.
         */
        private fun <T, K : Any> sortByKeyAndRequireUniqueAdjacentKeys(
            owner: String,
            factKind: String,
            duplicateKeyName: String,
            buffer: ArrayList<T>,
            keyOf: (T) -> K,
            keyComparator: Comparator<in K>,
            keyToString: (K) -> String,
        ) {
            if (buffer.size < 2) {
                return
            }

            buffer.sortWith(
                Comparator { left, right ->
                    keyComparator.compare(keyOf(left), keyOf(right))
                },
            )

            var cursor = 1
            while (cursor < buffer.size) {
                val leftKey = keyOf(buffer[cursor - 1])
                val rightKey = keyOf(buffer[cursor])

                if (keyComparator.compare(leftKey, rightKey) == 0) {
                    throw DuplicateMetamodelFactException(
                        owner = owner,
                        factKind = factKind,
                        duplicateKey = "$duplicateKeyName=${keyToString(leftKey)}",
                        reason = "Duplicate metamodel fact key.",
                    )
                }

                cursor++
            }
        }

        private fun <T> requireNoComparatorTies(
            owner: String,
            factKind: String,
            buffer: List<T>,
            comparator: Comparator<in T>,
        ) {
            var i = 1
            while (i < buffer.size) {
                val left = buffer[i - 1]
                val right = buffer[i]

                if (comparator.compare(left, right) == 0) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = factKind,
                        reason = "Comparator is not strict enough to produce deterministic fact sequence.",
                    )
                }

                i++
            }
        }

        private fun <T> requireStrictAscendingOrder(
            owner: String,
            factKind: String,
            buffer: List<T>,
            comparator: Comparator<in T>,
        ) {
            var i = 1
            while (i < buffer.size) {
                val left = buffer[i - 1]
                val right = buffer[i]
                val comparison = comparator.compare(left, right)

                if (comparison >= 0) {
                    val reason =
                        if (comparison == 0) {
                            "Sequence contains a comparator tie and is not strictly ordered."
                        } else {
                            "Sequence is not sorted according to the supplied deterministic comparator."
                        }

                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = factKind,
                        reason = reason,
                    )
                }

                i++
            }
        }

        private fun <T> fromOrderedBuffer(buffer: List<T>): MetamodelFactSequence<T> {
            val snapshot = arrayOfNulls<Any?>(buffer.size)

            var i = 0
            while (i < buffer.size) {
                snapshot[i] = buffer[i]
                i++
            }

            return MetamodelFactSequence(snapshot)
        }
    }
}
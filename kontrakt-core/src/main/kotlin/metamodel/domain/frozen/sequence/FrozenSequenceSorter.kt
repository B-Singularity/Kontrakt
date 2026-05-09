package metamodel.domain.frozen.sequence

import metamodel.domain.exception.FrozenMetamodelSequenceViolationException
import metamodel.domain.frozen.image.FrozenMetamodelImageId

/**
 * Kontrakt-owned deterministic sorter for frozen object-array sequences.
 *
 * This sorter is part of the frozen sequence publication protocol.
 *
 * It deliberately does not use:
 *
 * - Arrays.sort(...);
 * - List.sortWith(...);
 * - TreeSet;
 * - SortedSet;
 * - TreeMap as a coalescing structure;
 * - distinctBy;
 * - first-wins duplicate elimination;
 * - last-wins duplicate elimination;
 * - platform sort stability as semantic authority.
 *
 * Algorithm:
 *
 * General key-ordered record sequences use iterative bottom-up merge sort.
 *
 * Constructor-parameter sequences use compact-index direct placement because
 * they already have a ratified local ordinal:
 *
 * ```text
 * parameterIndex == destination index
 * ```
 *
 * Merge-sort law:
 *
 * The general sorter provides:
 *
 * - O(N log N) worst-case comparison behavior;
 * - O(N) temporary reference storage;
 * - no recursion;
 * - deterministic control flow;
 * - comparator-equality fail-closed behavior;
 * - final strict-order verification;
 * - no reliance on JDK TimSort, platform sort stability, or collection
 *   coalescing behavior.
 *
 * Comparator equality law:
 *
 * A comparator result of 0 is never treated as permission to merge, drop, or
 * replace records.
 *
 * In frozen sequences, comparator equality means:
 *
 * ```text
 * same frozen semantic key
 * ```
 *
 * It does not mean:
 *
 * ```text
 * same full payload
 * safe duplicate
 * safe replacement
 * ```
 *
 * Therefore this sorter fails closed when two distinct elements compare equal.
 *
 * Final verification law:
 *
 * After sorting, the output is checked for strict ascending comparator order.
 *
 * This is intentionally redundant with merge-time duplicate checks.
 *
 * It protects the publication boundary from:
 *
 * - future implementation edits;
 * - comparator contract bugs;
 * - accidental weakening of strict ordering;
 * - missed duplicate cases introduced by later refactoring.
 *
 * Array ownership law:
 *
 * The returned array is owned by the sorter caller and must be treated as
 * immutable by the sequence implementation.
 *
 * The sorter defensively copies the input array before sorting. This protects
 * the sequence from caller-side array mutation.
 *
 * This is a shallow array copy by design.
 *
 * The records themselves must already be immutable frozen records. The sorter
 * does not deep-copy records, because deep-copying frozen records would:
 *
 * - duplicate already-frozen value material;
 * - obscure identity/continuity diagnostics;
 * - add avoidable allocation pressure;
 * - weaken the aggregate factory ownership model.
 *
 * Record immutability is enforced by:
 *
 * - private constructors;
 * - val fields;
 * - immutable nested sequences;
 * - no mutable collection exposure;
 * - architecture tests that reject backend handle and mutable acquisition-state
 *   reachability.
 *
 * Memory law:
 *
 * General merge sort owns two arrays of size N during publication:
 *
 * - source;
 * - target.
 *
 * This O(N) temporary reference storage is accepted at the freeze publication
 * boundary because the alternative is delegating protocol behavior to platform
 * sort machinery or using O(N^2) insertion sorting.
 *
 * The sorter returns one of the owned arrays directly. It does not allocate a
 * third full-size publication copy after sorting.
 *
 * Hash law:
 *
 * This sorter does not use hashCode() for ordering or duplicate detection.
 *
 * Frozen sequence layout must not depend on transitional JVM hash policy.
 */
internal object FrozenSequenceSorter {
    inline fun <reified T : Any> sortStrict(
        imageId: FrozenMetamodelImageId,
        sequenceTable: String,
        input: Array<T>,
        comparator: Comparator<in T>,
        crossinline referenceSummaryOf: (T) -> String,
        crossinline duplicateReason: (
            left: T,
            right: T,
            leftIndex: Int,
            rightIndex: Int,
        ) -> String,
    ): Array<T> {
        if (input.size <= 1) {
            return input.copyOf()
        }

        var source = input.copyOf()

        /*
         * Keep target as Array<T>, not Array<T?> or Array<Any?>.
         *
         * This avoids unsafe JVM array casts and preserves the exact runtime
         * component type of the input array.
         *
         * The initial contents of target are irrelevant because each merge pass
         * writes every position in the active range before swapping arrays.
         */
        var target = input.copyOf()

        var width = 1
        val size = source.size

        while (width < size) {
            var leftStart = 0

            while (leftStart < size) {
                val middle =
                    boundedAdd(
                        base = leftStart,
                        delta = width,
                        limit = size,
                    )

                val rightEnd =
                    boundedAdd(
                        base = middle,
                        delta = width,
                        limit = size,
                    )

                if (middle >= rightEnd) {
                    copyRange(
                        source = source,
                        target = target,
                        fromInclusive = leftStart,
                        toExclusive = rightEnd,
                    )
                } else {
                    mergeStrict(
                        imageId = imageId,
                        sequenceTable = sequenceTable,
                        source = source,
                        target = target,
                        leftStart = leftStart,
                        middle = middle,
                        rightEnd = rightEnd,
                        comparator = comparator,
                        referenceSummaryOf = referenceSummaryOf,
                        duplicateReason = duplicateReason,
                    )
                }

                leftStart = rightEnd
            }

            val previousSource = source
            source = target
            target = previousSource

            width =
                if (width > size / 2) {
                    size
                } else {
                    width * 2
                }
        }

        verifyStrictlyIncreasing(
            imageId = imageId,
            sequenceTable = sequenceTable,
            sorted = source,
            comparator = comparator,
            referenceSummaryOf = referenceSummaryOf,
            duplicateReason = duplicateReason,
        )

        return source
    }

    inline fun <reified T : Any> sortStrictByPlacement(
        imageId: FrozenMetamodelImageId,
        sequenceTable: String,
        input: Array<T>,
        ownerSummary: String,
        crossinline indexOf: (T) -> Int,
        crossinline ownerIsValid: (T) -> Boolean,
        crossinline ownerMismatchReason: (inputIndex: Int, record: T) -> String,
        crossinline duplicateReason: (previous: T, current: T, index: Int) -> String,
        crossinline missingReason: (missingIndex: Int) -> String,
        crossinline outOfRangeReason: (inputIndex: Int, record: T, index: Int, size: Int) -> String,
    ): Array<T> {
        if (input.isEmpty()) {
            return input.copyOf()
        }

        val placed = arrayOfNulls<T>(input.size)

        var inputIndex = 0

        while (inputIndex < input.size) {
            val record = input[inputIndex]

            if (!ownerIsValid(record)) {
                throw FrozenMetamodelSequenceViolationException(
                    imageId = imageId,
                    sequenceTable = sequenceTable,
                    referenceSummary = ownerSummary,
                    reason = ownerMismatchReason(
                        inputIndex,
                        record,
                    ),
                )
            }

            val index = indexOf(record)

            if (index < 0 || index >= input.size) {
                throw FrozenMetamodelSequenceViolationException(
                    imageId = imageId,
                    sequenceTable = sequenceTable,
                    referenceSummary = ownerSummary,
                    reason = outOfRangeReason(
                        inputIndex,
                        record,
                        index,
                        input.size,
                    ),
                )
            }

            val previous = placed[index]

            if (previous != null) {
                throw FrozenMetamodelSequenceViolationException(
                    imageId = imageId,
                    sequenceTable = sequenceTable,
                    referenceSummary = ownerSummary,
                    reason = duplicateReason(
                        previous,
                        record,
                        index,
                    ),
                )
            }

            placed[index] = record
            inputIndex += 1
        }

        /*
         * Keep result as Array<T> by reusing input.copyOf() as a correctly typed
         * destination array. Do not cast Array<Any?> or Array<T?> to Array<T>.
         */
        val result = input.copyOf()

        var index = 0

        while (index < placed.size) {
            val record =
                placed[index]
                    ?: throw FrozenMetamodelSequenceViolationException(
                        imageId = imageId,
                        sequenceTable = sequenceTable,
                        referenceSummary = ownerSummary,
                        reason = missingReason(index),
                    )

            result[index] = record
            index += 1
        }

        return result
    }

    inline fun <T : Any> mergeStrict(
        imageId: FrozenMetamodelImageId,
        sequenceTable: String,
        source: Array<T>,
        target: Array<T>,
        leftStart: Int,
        middle: Int,
        rightEnd: Int,
        comparator: Comparator<in T>,
        crossinline referenceSummaryOf: (T) -> String,
        crossinline duplicateReason: (
            left: T,
            right: T,
            leftIndex: Int,
            rightIndex: Int,
        ) -> String,
    ) {
        var leftIndex = leftStart
        var rightIndex = middle
        var writeIndex = leftStart

        while (leftIndex < middle && rightIndex < rightEnd) {
            val left = source[leftIndex]
            val right = source[rightIndex]

            val comparison =
                comparator.compare(
                    left,
                    right,
                )

            if (comparison == 0) {
                throw FrozenMetamodelSequenceViolationException(
                    imageId = imageId,
                    sequenceTable = sequenceTable,
                    referenceSummary = referenceSummaryOf(right),
                    reason = duplicateReason(
                        left,
                        right,
                        leftIndex,
                        rightIndex,
                    ),
                )
            }

            if (comparison < 0) {
                target[writeIndex] = left
                leftIndex += 1
            } else {
                target[writeIndex] = right
                rightIndex += 1
            }

            writeIndex += 1
        }

        while (leftIndex < middle) {
            target[writeIndex] = source[leftIndex]
            leftIndex += 1
            writeIndex += 1
        }

        while (rightIndex < rightEnd) {
            target[writeIndex] = source[rightIndex]
            rightIndex += 1
            writeIndex += 1
        }
    }

    inline fun <T : Any> verifyStrictlyIncreasing(
        imageId: FrozenMetamodelImageId,
        sequenceTable: String,
        sorted: Array<T>,
        comparator: Comparator<in T>,
        crossinline referenceSummaryOf: (T) -> String,
        crossinline duplicateReason: (
            left: T,
            right: T,
            leftIndex: Int,
            rightIndex: Int,
        ) -> String,
    ) {
        var index = 1

        while (index < sorted.size) {
            val previous = sorted[index - 1]
            val current = sorted[index]

            val comparison =
                comparator.compare(
                    previous,
                    current,
                )

            if (comparison == 0) {
                throw FrozenMetamodelSequenceViolationException(
                    imageId = imageId,
                    sequenceTable = sequenceTable,
                    referenceSummary = referenceSummaryOf(current),
                    reason = duplicateReason(
                        previous,
                        current,
                        index - 1,
                        index,
                    ),
                )
            }

            if (comparison > 0) {
                throw FrozenMetamodelSequenceViolationException(
                    imageId = imageId,
                    sequenceTable = sequenceTable,
                    referenceSummary = referenceSummaryOf(current),
                    reason = "Frozen sequence sorter produced non-increasing order: " +
                            "leftIndex=${index - 1}, rightIndex=$index",
                )
            }

            index += 1
        }
    }

    fun <T : Any> copyRange(
        source: Array<T>,
        target: Array<T>,
        fromInclusive: Int,
        toExclusive: Int,
    ) {
        var index = fromInclusive

        while (index < toExclusive) {
            target[index] = source[index]
            index += 1
        }
    }

    fun boundedAdd(
        base: Int,
        delta: Int,
        limit: Int,
    ): Int {
        val candidate = base + delta

        if (candidate < base || candidate > limit) {
            return limit
        }

        return candidate
    }
}
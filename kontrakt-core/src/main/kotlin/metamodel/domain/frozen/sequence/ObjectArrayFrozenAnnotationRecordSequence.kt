package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.frozen.order.FrozenAnnotationRecordOrder
import metamodel.domain.frozen.record.FrozenAnnotationRecord
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId

/**
 * Object-array-backed deterministic annotation record sequence.
 *
 * This is the Level 1 implementation of FrozenAnnotationRecordSequence.
 *
 * Construction law:
 *
 * - input order is non-authoritative;
 * - records are defensively copied;
 * - records are sorted by FrozenAnnotationRecordKey;
 * - duplicate annotation keys fail closed;
 * - comparator equality between distinct records fails closed;
 * - sequence storage is immutable after issue(...).
 *
 * Repeatable annotation law:
 *
 * Repeatable annotation behavior must not be represented by backend enumeration
 * order.
 *
 * If two repeatable annotations are semantically distinct, they must have
 * distinct canonicalPayloadKey values before this sequence is issued.
 *
 * If two records have the same key, this sequence rejects them. A future ADR may
 * introduce a repeatable-annotation merge or ordinal law, but this Level 1
 * sequence must fail closed.
 *
 * Equality law:
 *
 * Sequence equality is ordered structural equality:
 *
 * ```text
 * same size
 * and for every i: this[i] == other[i]
 * ```
 *
 * This ensures frozen parent records can safely include this sequence in their
 * own structural equality.
 *
 * Hash law:
 *
 * hashCode is an ordered transitional JVM equality-collection companion only.
 * It is not canonical digest, route key, PlanCacheKey material, or persistent
 * image identity.
 *
 * Sorting law:
 *
 * This sequence does not use platform sort.
 *
 * Ordering is delegated to FrozenSequenceSorter, which implements a
 * Kontrakt-owned deterministic bottom-up merge sort with comparator-equality
 * fail-closed behavior.
 *
 * This keeps frozen sequence publication independent from JDK TimSort,
 * platform collection semantics, and set-like duplicate coalescing.
 */
class ObjectArrayFrozenAnnotationRecordSequence private constructor(
    private val records: Array<FrozenAnnotationRecord>,
    private val precomputedHashCode: Int,
) : FrozenAnnotationRecordSequence {
    override val size: Int
        get() = records.size

    override fun get(
        index: Int,
    ): FrozenAnnotationRecord {
        if (index < 0 || index >= records.size) {
            throw IndexOutOfBoundsException(
                "Frozen annotation record sequence index out of bounds: index=$index, size=${records.size}",
            )
        }

        return records[index]
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenAnnotationRecordSequence) return false
        if (size != other.size) return false
        if (precomputedHashCode != other.hashCode()) return false

        var index = 0
        while (index < size) {
            if (this[index] != other[index]) {
                return false
            }

            index += 1
        }

        return true
    }

    override fun hashCode(): Int =
        precomputedHashCode

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            records: Array<FrozenAnnotationRecord>,
        ): ObjectArrayFrozenAnnotationRecordSequence {
            val ordered =
                FrozenSequenceSorter.sortStrict(
                    imageId = imageId,
                    sequenceTable = FrozenMetamodelImageTableId.ANNOTATION_RECORD_SEQUENCE.name,
                    input = records,
                    comparator = FrozenAnnotationRecordOrder,
                    referenceSummaryOf = { record ->
                        record.key.annotationType.renderSummary()
                    },
                    duplicateReason = { previous, current, leftIndex, rightIndex ->
                        "Duplicate or comparator-equal annotation record key during Kontrakt-owned merge sort: " +
                                "leftIndex=$leftIndex, rightIndex=$rightIndex, " +
                                "previous=${previous.key.renderSummary()}, " +
                                "current=${current.key.renderSummary()}"
                    },
                )

            return ObjectArrayFrozenAnnotationRecordSequence(
                records = ordered,
                precomputedHashCode = computeOrderedHashCode(ordered),
            )
        }

        private fun computeOrderedHashCode(
            records: Array<FrozenAnnotationRecord>,
        ): Int {
            var result = 1
            var index = 0

            while (index < records.size) {
                result = 31 * result + records[index].hashCode()
                index += 1
            }

            return result
        }
    }
}
package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.frozen.order.FrozenPropertyRecordOrder
import metamodel.domain.frozen.record.FrozenPropertyRecord
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId

/**
 * Object-array-backed deterministic property record sequence.
 *
 * Construction law:
 *
 * - input order is non-authoritative;
 * - records are defensively copied;
 * - records are sorted by FrozenPropertyRecordKey;
 * - duplicate property keys fail closed;
 * - comparator equality between distinct records fails closed;
 * - availability is record state, not key material;
 * - same key with conflicting payload fails closed;
 * - sequence storage is immutable after issue(...).
 *
 * The property key ordering law follows ADR-0039:
 *
 * ```text
 * ownerType canonical identity
 * -> propertyName canonical identifier order
 * -> propertyType canonical identity
 * -> visibilityRank
 * ```
 *
 * Equality law:
 *
 * Sequence equality is ordered structural equality.
 */
class ObjectArrayFrozenPropertyRecordSequence private constructor(
    private val imageId: FrozenMetamodelImageId,
    private val records: Array<FrozenPropertyRecord>,
    private val precomputedHashCode: Int,
) : FrozenPropertyRecordSequence {
    override val size: Int
        get() = records.size

    override fun get(
        index: Int,
    ): FrozenPropertyRecord {
        if (index < 0 || index >= records.size) {
            throw IndexOutOfBoundsException(
                "Frozen property record sequence index out of bounds: index=$index, size=${records.size}",
            )
        }

        return records[index]
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenPropertyRecordSequence) return false
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
            records: Array<FrozenPropertyRecord>,
        ): ObjectArrayFrozenPropertyRecordSequence {
            val ordered =
                FrozenSequenceSorter.sortStrict(
                    imageId = imageId,
                    sequenceTable = FrozenMetamodelImageTableId.PROPERTY_RECORD_SEQUENCE.name,
                    input = records,
                    comparator = FrozenPropertyRecordOrder,
                    referenceSummaryOf = { record ->
                        record.key.ownerType.renderSummary()
                    },
                    duplicateReason = { previous, current, leftIndex, rightIndex ->
                        "Duplicate or comparator-equal property record key during Kontrakt-owned merge sort: " +
                                "leftIndex=$leftIndex, rightIndex=$rightIndex, " +
                                "previous=${previous.key.renderSummary()}, " +
                                "current=${current.key.renderSummary()}"
                    },
                )

            return ObjectArrayFrozenPropertyRecordSequence(
                imageId = imageId,
                records = ordered,
                precomputedHashCode = computeOrderedHashCode(ordered),
            )
        }

        private fun computeOrderedHashCode(
            records: Array<FrozenPropertyRecord>,
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
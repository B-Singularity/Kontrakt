package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.frozen.order.FrozenConstructorRecordOrder
import metamodel.domain.frozen.record.FrozenConstructorRecord
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId

/**
 * Object-array-backed deterministic constructor record sequence.
 *
 * Construction law:
 *
 * - input order is non-authoritative;
 * - records are defensively copied;
 * - records are sorted by FrozenConstructorRecordKey;
 * - duplicate constructor keys fail closed;
 * - comparator equality between distinct records fails closed;
 * - availability is record state, not key material;
 * - same key with conflicting payload fails closed;
 * - sequence storage is immutable after issue(...).
 *
 * The constructor key ordering law follows ADR-0039:
 *
 * ```text
 * ownerType canonical identity
 * -> constructorSignature canonical order
 * -> parameterShapeSignature canonical order
 * ```
 *
 * Equality law:
 *
 * Sequence equality is ordered structural equality.
 */
class ObjectArrayFrozenConstructorRecordSequence private constructor(
    private val records: Array<FrozenConstructorRecord>,
    private val precomputedHashCode: Int,
) : FrozenConstructorRecordSequence {
    override val size: Int
        get() = records.size

    override fun get(
        index: Int,
    ): FrozenConstructorRecord {
        if (index < 0 || index >= records.size) {
            throw IndexOutOfBoundsException(
                "Frozen constructor record sequence index out of bounds: index=$index, size=${records.size}",
            )
        }

        return records[index]
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenConstructorRecordSequence) return false
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
            records: Array<FrozenConstructorRecord>,
        ): ObjectArrayFrozenConstructorRecordSequence {
            val ordered =
                FrozenSequenceSorter.sortStrict(
                    imageId = imageId,
                    sequenceTable = FrozenMetamodelImageTableId.CONSTRUCTOR_RECORD_SEQUENCE.name,
                    input = records,
                    comparator = FrozenConstructorRecordOrder,
                    referenceSummaryOf = { record ->
                        record.key.ownerType.renderSummary()
                    },
                    duplicateReason = { previous, current, leftIndex, rightIndex ->
                        "Duplicate or comparator-equal constructor record key during Kontrakt-owned merge sort: " +
                                "leftIndex=$leftIndex, rightIndex=$rightIndex, " +
                                "previous=${previous.key.renderSummary()}, " +
                                "current=${current.key.renderSummary()}"
                    },
                )

            return ObjectArrayFrozenConstructorRecordSequence(
                records = ordered,
                precomputedHashCode = computeOrderedHashCode(ordered),
            )
        }

        private fun computeOrderedHashCode(
            records: Array<FrozenConstructorRecord>,
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
package metamodel.domain.frozen.sequence

import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.frozen.record.FrozenConstructorParameterRecord
import metamodel.domain.frozen.record.FrozenConstructorRecordKey
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId

/**
 * Object-array-backed deterministic constructor-parameter sequence.
 *
 * This is the Level 1 implementation of FrozenConstructorParameterRecordSequence.
 *
 * Construction law:
 *
 * - input order is non-authoritative;
 * - records are defensively copied;
 * - records are sorted by owner constructor key and parameter index;
 * - all records must belong to the declared owner constructor key;
 * - parameter indexes must be compact: 0, 1, 2, ..., N - 1;
 * - duplicate parameter indexes fail closed;
 * - same key with conflicting payload fails closed;
 * - sequence storage is immutable after issue(...).
 *
 * Why compactness belongs here:
 *
 * A single FrozenConstructorParameterRecordKey can validate only
 * parameterIndex >= 0.
 *
 * Compactness is a property of the complete parameter sequence, so this class
 * is the correct aggregate boundary for:
 *
 * ```text
 * indexes == 0..N-1
 * ```
 *
 * Equality law:
 *
 * Sequence equality is ordered structural equality.
 *
 * Hash law:
 *
 * hashCode is an ordered transitional JVM equality-collection companion only.
 */
class ObjectArrayFrozenConstructorParameterRecordSequence private constructor(
    private val records: Array<FrozenConstructorParameterRecord>,
    private val precomputedHashCode: Int,
) : FrozenConstructorParameterRecordSequence {
    override val size: Int
        get() = records.size

    override fun get(
        index: Int,
    ): FrozenConstructorParameterRecord {
        if (index < 0 || index >= records.size) {
            throw IndexOutOfBoundsException(
                "Frozen constructor-parameter sequence index out of bounds: index=$index, size=${records.size}",
            )
        }

        return records[index]
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenConstructorParameterRecordSequence) return false
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
            ownerConstructorKey: FrozenConstructorRecordKey,
            records: Array<FrozenConstructorParameterRecord>,
        ): ObjectArrayFrozenConstructorParameterRecordSequence {
            val ordered =
                FrozenSequenceSorter.sortStrictByPlacement(
                    imageId = imageId,
                    sequenceTable = FrozenMetamodelImageTableId.CONSTRUCTOR_PARAMETER_SEQUENCE.name,
                    input = records,
                    ownerSummary = ownerConstructorKey.ownerType.renderSummary(),
                    indexOf = { record ->
                        record.key.parameterIndex
                    },
                    ownerIsValid = { record ->
                        record.key.ownerConstructorKey == ownerConstructorKey
                    },
                    ownerMismatchReason = { inputIndex, record ->
                        "Constructor parameter owner mismatch: " +
                                "inputIndex=$inputIndex, " +
                                "expected=${ownerConstructorKey.renderSummary()}, " +
                                "actual=${record.key.ownerConstructorKey.renderSummary()}"
                    },
                    duplicateReason = { previous, current, index ->
                        "Duplicate constructor parameter index: " +
                                "parameterIndex=$index, " +
                                "previous=${previous.key.renderSummary()}, " +
                                "current=${current.key.renderSummary()}"
                    },
                    missingReason = { missingIndex ->
                        "Missing constructor parameter index in compact sequence: " +
                                "missingIndex=$missingIndex"
                    },
                    outOfRangeReason = { inputIndex, _, index, size ->
                        "Constructor parameter index is outside compact range: " +
                                "inputIndex=$inputIndex, " +
                                "validRange=0..${size - 1}, " +
                                "actualIndex=$index"
                    },
                )

            return ObjectArrayFrozenConstructorParameterRecordSequence(
                records = ordered,
                precomputedHashCode = computeOrderedHashCode(ordered),
            )
        }

        private fun computeOrderedHashCode(
            records: Array<FrozenConstructorParameterRecord>,
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

    private fun computeOrderedHashCode(
        records: Array<FrozenConstructorParameterRecord>,
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

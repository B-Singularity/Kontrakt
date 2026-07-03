package stage.canonicalization.material.frozen.sequence

import stage.admission.diagnostics.evidence.FrozenMetamodelSequenceIndexOutOfBoundsException
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageId
import stage.canonicalization.material.frozen.record.FrozenConstructorParameterRecord
import stage.canonicalization.material.frozen.record.FrozenConstructorRecordKey
import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId

/**
 * Object-array-backed deterministic constructor-parameter sequence.
 *
 * This is the Level 1 implementation of FrozenConstructorParameterRecordSequence.
 *
 * Construction law:
 *
 * - input order is non-authoritative;
 * - records are defensively isolated through FrozenSequenceSorter direct placement;
 * - records are placed by compact parameter index;
 * - all records must belong to the declared owner constructor key;
 * - parameter indexes must be compact: 0, 1, 2, ..., N - 1;
 * - duplicate parameter indexes fail closed;
 * - missing parameter indexes fail closed;
 * - out-of-range parameter indexes fail closed;
 * - sequence storage is immutable after issue(...).
 *
 * Why compactness belongs here:
 *
 * A single FrozenConstructorParameterRecordKey can validate only:
 *
 * ```text
 * parameterIndex >= 0
 * ```
 *
 * Compactness is a property of the complete parameter sequence, so this class
 * is the aggregate boundary for:
 *
 * ```text
 * indexes == 0..N-1
 * ```
 *
 * Placement law:
 *
 * Constructor parameters do not use comparison sorting.
 *
 * The protocol index is already the destination address:
 *
 * ```text
 * destination[record.key.parameterIndex] = record
 * ```
 *
 * This gives deterministic O(N) publication and validates duplicate, missing,
 * out-of-range, and wrong-owner records in the same pass family.
 *
 * Density law:
 *
 * This sequence allocates placement storage from records.size, not from the
 * maximum observed parameterIndex.
 *
 * Therefore a polluted record with parameterIndex=999999 and records.size=2 is
 * rejected as out-of-range instead of inflating the destination array.
 *
 * A maliciously or accidentally huge records array must be rejected by the
 * metamodel acquisition/session capacity policy before this sequence is issued.
 *
 * Ownership law:
 *
 * Ownership is validated by structural equality of FrozenConstructorRecordKey.
 *
 * If two adapters produce different owner keys for the same semantic
 * constructor, this sequence must fail closed. The fix belongs to canonical
 * constructor key issuance, not to sequence-level fuzzy matching.
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
 * Hash law:
 *
 * hashCode is an ordered transitional JVM equality-collection companion only.
 *
 * The precomputed hashCode is a cheap negative filter. A matching hashCode does
 * not prove equality; equals(...) still performs ordered element-by-element
 * comparison.
 *
 * This hashCode is not:
 *
 * - canonical fingerprint;
 * - persistent frozen-image identity;
 * - route key;
 * - L1/L2 partition key;
 * - PlanCacheKey material;
 * - cross-runtime protocol digest.
 *
 * The later BLAKE3 / metadata-hash refactoring may replace this transitional
 * hashCode strategy globally.
 */
class ObjectArrayFrozenConstructorParameterRecordSequence private constructor(
    private val imageId: FrozenMetamodelImageId,
    private val records: Array<FrozenConstructorParameterRecord>,
    private val precomputedHashCode: Int,
) : FrozenConstructorParameterRecordSequence {
    override val size: Int
        get() = records.size

    override fun get(
        index: Int,
    ): FrozenConstructorParameterRecord {
        if (index < 0 || index >= records.size) {
            throw FrozenMetamodelSequenceIndexOutOfBoundsException(
                imageId = imageId,
                sequenceTable = FrozenMetamodelImageTableId.CONSTRUCTOR_RECORD_SEQUENCE.name,
                index = index,
                size = records.size,
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

        /*
         * Cheap negative filter only.
         *
         * A matching hashCode never proves equality. Structural equality is
         * always completed by the ordered element-by-element loop below.
         */
        if (precomputedHashCode != other.hashCode()) {
            return false
        }

        var index = 0

        while (index < size) {
            if (this[index] != other[index]) {
                return false
            }

            index += 1
        }

        return true
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return "ObjectArrayFrozenConstructorParameterRecordSequence(size=$size)"
    }

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
                imageId = imageId,
                records = ordered,
                precomputedHashCode = computeOrderedHashCode(ordered),
            )
        }

        /**
         * Computes the ordered transitional JVM hashCode companion.
         *
         * This deliberately follows the current metamodel VO family until the
         * later BLAKE3 / metadata-hash refactoring replaces hash policy globally.
         *
         * This value is only a local equality fast-fail companion.
         *
         * It must not be used as:
         *
         * - canonical fingerprint;
         * - persistent frozen-image identity;
         * - route key;
         * - L1/L2 partition key;
         * - PlanCacheKey material;
         * - cross-runtime protocol digest.
         */
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
}
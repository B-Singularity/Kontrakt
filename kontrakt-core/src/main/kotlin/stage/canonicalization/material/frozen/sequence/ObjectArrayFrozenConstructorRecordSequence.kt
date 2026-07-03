package stage.canonicalization.material.frozen.sequence

import stage.admission.diagnostics.evidence.FrozenMetamodelSequenceIndexOutOfBoundsException
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageId
import stage.canonicalization.material.frozen.order.FrozenConstructorRecordOrder
import stage.canonicalization.material.frozen.record.FrozenConstructorRecord
import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId

/**
 * Object-array-backed deterministic constructor record sequence.
 *
 * This is the Level 1 implementation of FrozenConstructorRecordSequence.
 *
 * Construction law:
 *
 * - input order is non-authoritative;
 * - records are defensively isolated through FrozenSequenceSorter;
 * - records are ordered by FrozenConstructorRecordKey;
 * - duplicate constructor keys fail closed;
 * - comparator equality is never treated as merge permission;
 * - sequence storage is immutable after issue(...).
 *
 * Sorting law:
 *
 * This sequence uses Kontrakt-owned deterministic bottom-up merge sort.
 *
 * It does not use:
 *
 * - Arrays.sort(...);
 * - List.sortWith(...);
 * - TreeSet;
 * - SortedSet;
 * - TreeMap as a duplicate-coalescing structure;
 * - first-wins duplicate elimination;
 * - last-wins duplicate elimination.
 *
 * Rationale:
 *
 * Frozen sequence publication is a protocol boundary. Ordering, duplicate
 * detection, and conflict behavior must be owned by Kontrakt rather than by JDK
 * sort implementation details or platform collection semantics.
 *
 * Cost law:
 *
 * Publication costs O(N log N) comparisons and O(N) temporary reference storage.
 *
 * This is accepted at the freeze boundary because this work happens once during
 * frozen image publication, not during planning hot-path traversal.
 *
 * Object-array layout law:
 *
 * This Level 1 representation stores Array<FrozenConstructorRecord>.
 *
 * It pays:
 *
 * - one sequence object;
 * - one reference array;
 * - one JVM object header per FrozenConstructorRecord;
 * - pointer chasing through Record -> Key -> TypeReference/signature material.
 *
 * This is not the final physical layout. Future interning and ordinal/slab
 * lowering may replace constructor records with owner-addressed ranges or
 * constructor ordinals, but this Level 1 sequence owns deterministic ordering
 * and fail-closed duplicate semantics now.
 *
 * Diagnostic identity law:
 *
 * [imageId] is retained only for domain exception diagnostics.
 *
 * It is not sequence equality material.
 * It is not sequence hash material.
 * It is not constructor identity material.
 * It is not planning key material.
 *
 * Access law:
 *
 * Invalid indexed access throws
 * FrozenMetamodelSequenceIndexOutOfBoundsException, not a raw JVM/Kotlin
 * collection exception.
 *
 * Frozen deterministic sequences are metamodel-domain structures, so their
 * public access boundary must preserve the domain exception taxonomy.
 *
 * Canonical input law:
 *
 * This sequence cannot normalize constructor signatures.
 *
 * constructorSignature and parameterShapeSignature must already be lowered and
 * validated by FrozenConstructorRecordKey.issue(...). If two adapters emit
 * different signature tokens for the same semantic constructor, this sequence
 * must fail closed rather than guess equivalence.
 *
 * Shallow-copy law:
 *
 * The sorter defensively copies the input array but does not deep-copy records.
 *
 * FrozenConstructorRecord instances must already be immutable frozen material.
 * If a record can mutate after issue(...), the broken boundary is the record
 * factory or acquisition assembler, not this sequence.
 *
 * Capacity law:
 *
 * A maliciously or accidentally huge records array must be rejected by the
 * metamodel acquisition/session capacity policy before this sequence is issued.
 * This sequence does not own global resource governance.
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
 * [imageId] is intentionally excluded from equality. Two sequences from
 * different diagnostic images may still be structurally equal.
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
class ObjectArrayFrozenConstructorRecordSequence private constructor(
    private val imageId: FrozenMetamodelImageId,
    private val records: Array<FrozenConstructorRecord>,
    private val precomputedHashCode: Int,
) : FrozenConstructorRecordSequence {
    override val size: Int
        get() = records.size

    override fun get(
        index: Int,
    ): FrozenConstructorRecord {
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
        if (other !is FrozenConstructorRecordSequence) return false
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
        return "ObjectArrayFrozenConstructorRecordSequence(size=$size)"
    }

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
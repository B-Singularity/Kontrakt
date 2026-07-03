package stage.canonicalization.material.frozen.table

import stage.canonicalization.material.TypeReference
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageId
import stage.canonicalization.material.frozen.order.FrozenTypeReferenceOrder
import stage.canonicalization.material.frozen.sequence.FrozenSequenceSorter
import stage.input.diagnostics.FrozenMetamodelSequenceIndexOutOfBoundsException
import stage.input.diagnostics.FrozenMetamodelSequenceViolationException
import versioning.coordinate.material.value.FrozenMetamodelImageSchemaVersion

/**
 * Object-array-backed frozen TypeReference index.
 *
 * This is the Level 1 frozen image-local TypeReference ordinal authority.
 *
 * It is intentionally still object-array based:
 *
 * ```text
 * Array<TypeReference>
 * ```
 *
 * It is not:
 *
 * - a LongArray slab;
 * - an off-heap table;
 * - a persistent index;
 * - a global interner;
 * - a process-global registry;
 * - a PlanCacheKey table;
 * - a route64 table.
 *
 * Identity split:
 *
 * TypeReference owns semantic equality.
 *
 * This index owns image-local frozen ordinals.
 *
 * The ordinal assigned by this index is:
 *
 * - deterministic for the same TypeReference set;
 * - local to one FrozenMetamodelImage;
 * - assigned after deterministic ordering;
 * - never stored in TypeReference;
 * - never adapter acquisition order;
 * - never persistent identity.
 *
 * Publication law:
 *
 * Input order is non-authoritative.
 *
 * The issue(...) factory:
 *
 * - defensively isolates the caller-owned array;
 * - orders TypeReference values through FrozenTypeReferenceOrder;
 * - rejects duplicate TypeReference entries;
 * - rejects comparator-equal but structurally different TypeReference values;
 * - publishes an immutable object-array index.
 *
 * Comparator equality law:
 *
 * If FrozenTypeReferenceOrder returns 0 and TypeReference.equals(...) is false,
 * this index fails closed.
 *
 * That case means the ordering law is missing an equality axis or a TypeReference
 * was polluted with inconsistent material. Silently keeping either side would
 * corrupt image coverage.
 *
 * Lookup law:
 *
 * ordinalOf(reference) performs deterministic binary search over the frozen
 * ordered array.
 *
 * It returns FrozenTypeReferenceIndex.MISSING_ORDINAL for absent references.
 *
 * It does not throw for lookup miss because providers own the user-facing
 * missing-reference exception taxonomy.
 *
 * Access law:
 *
 * referenceAt(ordinal) throws FrozenMetamodelSequenceIndexOutOfBoundsException
 * for invalid ordinals.
 *
 * This index is a domain-owned frozen structure. It must not leak raw
 * ArrayIndexOutOfBoundsException or IndexOutOfBoundsException through its public
 * access boundary.
 *
 * Hash law:
 *
 * This index does not use TypeReference.hashCode() for ordering.
 *
 * TypeReference.hashCode() is transitional in-memory collection material only.
 * Frozen ordinal assignment must not depend on transitional JVM hash policy.
 *
 * Future lowering:
 *
 * A later interning/slab phase may replace this representation with:
 *
 * ```text
 * TypeReference -> StableTypeReferenceInternId
 * StableTypeReferenceInternId -> FrozenTypeOrdinal
 * LongArray / IntArray-backed index
 * ```
 *
 * That future design must preserve the same deterministic ordering, duplicate
 * rejection, and image-local ordinal law.
 *
 * Lookup cost law:
 *
 * ordinalOf(reference) uses deterministic binary search over the ordered
 * TypeReference array.
 *
 * The asymptotic lookup cost is:
 *
 * ```text
 * O(log N * TypeReference comparison cost)
 * ```
 *
 * This is acceptable for the Level 1 object-array foundation because it keeps
 * the index simple, deterministic, backend-neutral, and independent from
 * transitional hash policy.
 *
 * It is not the final hot-path shape.
 *
 * Future interning/lowering may replace this with stable integer-id lookup or
 * primitive ordinal tables so provider hot paths can avoid repeated structural
 * TypeReference comparison.
 *
 *
 * Immutability law:
 *
 * The index defensively owns the reference array, but it does not deep-copy
 * TypeReference instances.
 *
 * This is intentional.
 *
 * TypeReference must already be immutable canonical metamodel material before
 * entering this index. If a TypeReference can mutate after index publication,
 * the binary-search invariant is invalidated and the broken boundary is the
 * TypeReference issuer or acquisition assembler, not this index.
 *
 * Architecture tests must reject mutable backend/acquisition-state reachability
 * from TypeReference material.
 */
class ObjectArrayFrozenTypeReferenceIndex private constructor(
    private val imageId: FrozenMetamodelImageId,
    override val schemaVersion: FrozenMetamodelImageSchemaVersion,
    private val references: Array<TypeReference>,
) : FrozenTypeReferenceIndex {
    override val size: Int
        get() = references.size

    override fun ordinalOf(
        reference: TypeReference,
    ): Int {
        var low = 0
        var high = references.size - 1

        while (low <= high) {
            val middle =
                low + ((high - low) ushr 1)

            val candidate = references[middle]

            val comparison =
                FrozenTypeReferenceOrder.compare(
                    left = candidate,
                    right = reference,
                )

            if (comparison == 0) {
                /*
                 * Comparator equality should imply equality for indexed
                 * TypeReference material because issue(...) rejects
                 * comparator-equal but structurally different values.
                 *
                 * Still verify here defensively. If this ever fails, the index
                 * has been corrupted or a comparator/equality law drifted.
                 */
                return if (candidate == reference) {
                    middle
                } else {
                    FrozenTypeReferenceIndex.MISSING_ORDINAL
                }
            }

            if (comparison < 0) {
                low = middle + 1
            } else {
                high = middle - 1
            }
        }

        return FrozenTypeReferenceIndex.MISSING_ORDINAL
    }

    override fun referenceAt(
        frozenOrdinal: Int,
    ): TypeReference {
        if (frozenOrdinal < 0 || frozenOrdinal >= references.size) {
            throw FrozenMetamodelSequenceIndexOutOfBoundsException(
                imageId = imageId,
                sequenceTable = FrozenMetamodelImageTableId.TYPE_INDEX.name,
                index = frozenOrdinal,
                size = references.size,
            )
        }

        return references[frozenOrdinal]
    }

    override fun toString(): String {
        return "ObjectArrayFrozenTypeReferenceIndex(size=$size, schemaVersion=$schemaVersion)"
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            references: Array<TypeReference>,
        ): ObjectArrayFrozenTypeReferenceIndex {
            val ordered =
                FrozenSequenceSorter.sortStrict(
                    imageId = imageId,
                    sequenceTable = FrozenMetamodelImageTableId.TYPE_INDEX.name,
                    input = references,
                    comparator = FrozenTypeReferenceOrder,
                    referenceSummaryOf = { reference ->
                        reference.renderSummary()
                    },
                    duplicateReason = { previous, current, leftIndex, rightIndex ->
                        duplicateTypeReferenceReason(
                            previous = previous,
                            current = current,
                            leftIndex = leftIndex,
                            rightIndex = rightIndex,
                        )
                    },
                )

            requireComparatorEqualityImpliesStructuralEquality(
                imageId = imageId,
                ordered = ordered,
            )

            return ObjectArrayFrozenTypeReferenceIndex(
                imageId = imageId,
                schemaVersion = schemaVersion,
                references = ordered,
            )
        }

        /**
         * Defensive final scan for TypeReference equality/order coherence.
         *
         * FrozenSequenceSorter already rejects comparator-equal adjacent values.
         *
         * This scan is intentionally retained as a TypeReference-index-specific
         * guard. It protects the coverage authority from future sorter changes,
         * comparator changes, or TypeReference equality law changes.
         */
        private fun requireComparatorEqualityImpliesStructuralEquality(
            imageId: FrozenMetamodelImageId,
            ordered: Array<TypeReference>,
        ) {
            var index = 1

            while (index < ordered.size) {
                val previous = ordered[index - 1]
                val current = ordered[index]

                val comparison =
                    FrozenTypeReferenceOrder.compare(
                        left = previous,
                        right = current,
                    )

                if (comparison == 0 && previous != current) {
                    throw FrozenMetamodelSequenceViolationException(
                        imageId = imageId,
                        sequenceTable = FrozenMetamodelImageTableId.TYPE_INDEX.name,
                        referenceSummary = current.renderSummary(),
                        reason = duplicateTypeReferenceReason(
                            previous = previous,
                            current = current,
                            leftIndex = index - 1,
                            rightIndex = index,
                        ),
                    )
                }

                index += 1
            }
        }

        private fun duplicateTypeReferenceReason(
            previous: TypeReference,
            current: TypeReference,
            leftIndex: Int,
            rightIndex: Int,
        ): String {
            return "Duplicate or comparator-equal TypeReference during frozen type index publication: " +
                    "leftIndex=$leftIndex, rightIndex=$rightIndex, " +
                    "previous=${previous.renderSummary()}, " +
                    "current=${current.renderSummary()}"
        }
    }
}
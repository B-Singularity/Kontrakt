package metamodel.domain.frozen.table

import metamodel.domain.exception.FrozenMetamodelSequenceViolationException
import metamodel.domain.protocol.MetamodelProtocolOrdering
import metamodel.domain.vo.ArrayComponentShapeHint
import metamodel.domain.vo.TypeReference
import metamodel.domain.vo.TypeShapeSummary
import java.util.Arrays

/**
 * Object-array-backed FrozenTypeReferenceIndex.
 *
 * This is the Level 1/2 frozen index implementation:
 *
 * - Level 1: private immutable object array;
 * - Level 2: deterministic image-local ordinal addressing.
 *
 * It is intentionally not a HashMap-backed index.
 *
 * Reason:
 *
 * - HashMap iteration order must not influence frozen ordinal assignment;
 * - object-array binary search keeps ordering law explicit;
 * - later primitive routing/index tables can replace ordinalOf(...) without
 *   changing provider contracts.
 *
 * Construction law:
 *
 * Input order is non-authoritative. The factory defensively copies, sorts by
 * Kontrakt metamodel protocol order, and rejects comparator-equal duplicates.
 */
class ObjectArrayFrozenTypeReferenceIndex private constructor(
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
            val mid = (low + high) ushr 1
            val candidate = references[mid]
            val comparison =
                TypeReferenceOrder.compare(
                    left = candidate,
                    right = reference,
                )

            when {
                comparison < 0 -> low = mid + 1
                comparison > 0 -> high = mid - 1
                candidate == reference -> return mid
                else -> return FrozenTypeReferenceIndex.MISSING_ORDINAL
            }
        }

        return FrozenTypeReferenceIndex.MISSING_ORDINAL
    }

    override fun referenceAt(
        frozenTypeOrdinal: Int,
    ): TypeReference {
        if (frozenTypeOrdinal < 0 || frozenTypeOrdinal >= references.size) {
            throw IndexOutOfBoundsException(
                "Frozen type ordinal out of bounds: frozenTypeOrdinal=$frozenTypeOrdinal, size=${references.size}",
            )
        }

        return references[frozenTypeOrdinal]
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: Any,
            references: Array<TypeReference>,
        ): ObjectArrayFrozenTypeReferenceIndex {
            val copied = references.copyOf()

            Arrays.sort(
                copied,
                TypeReferenceOrder,
            )

            var index = 1
            while (index < copied.size) {
                val previous = copied[index - 1]
                val current = copied[index]

                if (TypeReferenceOrder.compare(previous, current) == 0) {
                    throw FrozenMetamodelSequenceViolationException(
                        imageId = imageId,
                        sequenceTable = FrozenMetamodelImageTableId.TYPE_INDEX.name,
                        referenceSummary = current.renderSummary(),
                        reason = "Duplicate or comparator-equal TypeReference in frozen type index.",
                    )
                }

                index += 1
            }

            return ObjectArrayFrozenTypeReferenceIndex(
                references = copied,
            )
        }
    }
}

/**
 * Deterministic in-memory order for TypeReference values inside one frozen image.
 *
 * This is not canonical byte encoding.
 * This is not persistent ordering material.
 * This is not L2 cache key material.
 *
 * It exists only to assign deterministic image-local frozen type ordinals.
 */
private object TypeReferenceOrder : Comparator<TypeReference> {
    override fun compare(
        left: TypeReference,
        right: TypeReference,
    ): Int {
        compareString(left.id.value, right.id.value).ifNonZero { return it }
        compareShapeSummary(left.id.shapeSummary, right.id.shapeSummary).ifNonZero { return it }
        compareString(left.id.classifierId, right.id.classifierId).ifNonZero { return it }
        compareString(left.id.classifierVersion, right.id.classifierVersion).ifNonZero { return it }

        compareString(
            left.id.ratificationFingerprint.algorithmId,
            right.id.ratificationFingerprint.algorithmId,
        ).ifNonZero { return it }

        compareString(
            left.id.ratificationFingerprint.algorithmVersion,
            right.id.ratificationFingerprint.algorithmVersion,
        ).ifNonZero { return it }

        compareInt(
            left.id.ratificationFingerprint.valueEncoding.protocolOrder,
            right.id.ratificationFingerprint.valueEncoding.protocolOrder,
        ).ifNonZero { return it }

        compareString(
            left.id.ratificationFingerprint.value,
            right.id.ratificationFingerprint.value,
        ).ifNonZero { return it }

        compareString(left.cycleKey.value, right.cycleKey.value).ifNonZero { return it }
        compareString(left.signature.value, right.signature.value).ifNonZero { return it }
        compareInt(left.signature.schemaVersion, right.signature.schemaVersion).ifNonZero { return it }

        compareAnnotations(left, right).ifNonZero { return it }

        return compareInt(
            left.typeNestingDepth,
            right.typeNestingDepth,
        )
    }

    private fun compareAnnotations(
        left: TypeReference,
        right: TypeReference,
    ): Int {
        compareInt(
            left.useSiteAnnotations.size,
            right.useSiteAnnotations.size,
        ).ifNonZero { return it }

        var index = 0
        while (index < left.useSiteAnnotations.size) {
            left.useSiteAnnotations[index]
                .compareTo(right.useSiteAnnotations[index])
                .ifNonZero { return it }

            index += 1
        }

        return 0
    }

    private fun compareShapeSummary(
        left: TypeShapeSummary,
        right: TypeShapeSummary,
    ): Int {
        compareInt(left.schemaVersion, right.schemaVersion).ifNonZero { return it }
        compareInt(left.kind.protocolOrder, right.kind.protocolOrder).ifNonZero { return it }
        compareInt(left.genericArity, right.genericArity).ifNonZero { return it }
        compareInt(left.arrayRank, right.arrayRank).ifNonZero { return it }

        compareInt(
            left.atomicFamily?.protocolOrder ?: -1,
            right.atomicFamily?.protocolOrder ?: -1,
        ).ifNonZero { return it }

        compareArrayComponentHint(
            left.arrayComponentHint,
            right.arrayComponentHint,
        ).ifNonZero { return it }

        return compareInt(
            left.expansionSurface.protocolOrder,
            right.expansionSurface.protocolOrder,
        )
    }

    private fun compareArrayComponentHint(
        left: ArrayComponentShapeHint?,
        right: ArrayComponentShapeHint?,
    ): Int {
        if (left == null && right == null) return 0
        if (left == null) return -1
        if (right == null) return 1

        compareBoolean(left.hasGenericComponent, right.hasGenericComponent).ifNonZero { return it }

        compareInt(
            left.componentGenericArityHint ?: -1,
            right.componentGenericArityHint ?: -1,
        ).ifNonZero { return it }

        return compareInt(
            left.componentShapeKindHint?.protocolOrder ?: -1,
            right.componentShapeKindHint?.protocolOrder ?: -1,
        )
    }

    private fun compareString(
        left: String,
        right: String,
    ): Int =
        MetamodelProtocolOrdering.compareUtf16CodeUnits(
            left = left,
            right = right,
        )

    private fun compareInt(
        left: Int,
        right: Int,
    ): Int =
        MetamodelProtocolOrdering.compareInt(
            left = left,
            right = right,
        )

    private fun compareBoolean(
        left: Boolean,
        right: Boolean,
    ): Int =
        MetamodelProtocolOrdering.compareBoolean(
            left = left,
            right = right,
        )

    private inline fun Int.ifNonZero(
        block: (Int) -> Unit,
    ) {
        if (this != 0) {
            block(this)
        }
    }
}
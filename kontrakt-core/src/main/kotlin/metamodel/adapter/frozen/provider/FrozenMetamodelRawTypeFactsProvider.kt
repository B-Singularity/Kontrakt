package metamodel.adapter.frozen.provider

import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelUnknownTypeReferenceException
import metamodel.domain.frozen.image.FrozenMetamodelImage
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.vo.TypeReference
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.RawTypeFactsResolution

/**
 * Planning-facing RawTypeFactsProvider backed by FrozenMetamodelImage.
 *
 * Hot-path lookup law:
 *
 * ```text
 * TypeReference -> frozenTypeOrdinal -> rawFactTable[frozenTypeOrdinal]
 * ```
 *
 * This provider performs one TypeReference lookup against the image-local type
 * index, then reads raw facts by primitive frozen type ordinal.
 *
 * Primitive ordinal safety:
 *
 * The primitive ordinal is intentionally kept as a short-lived local variable.
 * It is not stored, returned, persisted, or mixed with any other ordinal kind.
 *
 * Integrity boundary:
 *
 * FrozenMetamodelImage.issue(...) must already have validated table-size
 * equality and table coverage before this provider is constructed.
 *
 * Therefore, null from rawFactTable.findFactsAt(frozenTypeOrdinal) is treated as
 * a frozen-image integrity failure, not as an ordinary miss.
 *
 * Accounting rule:
 *
 * Reading from a FrozenMetamodelImage is a frozen/cache hit.
 * It is not actual backend fact discovery.
 * Therefore the returned resolution is RawTypeFactsResolution.cacheHit(...).
 */
class FrozenMetamodelRawTypeFactsProvider private constructor(
    private val image: FrozenMetamodelImage,
) : RawTypeFactsProvider {
    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        val frozenTypeOrdinal = image.typeIndex.ordinalOf(reference)

        if (frozenTypeOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
            )
        }

        val facts =
            image.rawFactTable.findFactsAt(frozenTypeOrdinal)
                ?: throw FrozenMetamodelIncompleteTableException(
                    imageId = image.imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                    reason = "TypeReference exists in the frozen type index but has no raw fact record at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )

        return RawTypeFactsResolution.cacheHit(facts)
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelRawTypeFactsProvider =
            FrozenMetamodelRawTypeFactsProvider(
                image = image,
            )
    }
}
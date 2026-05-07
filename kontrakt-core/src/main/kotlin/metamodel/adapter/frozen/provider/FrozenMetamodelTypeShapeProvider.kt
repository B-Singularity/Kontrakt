package metamodel.adapter.frozen.provider

import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelUnknownTypeReferenceException
import metamodel.domain.frozen.image.FrozenMetamodelImage
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.vo.TypeReference
import planning.domain.port.outgoing.TypeShapeProvider

/**
 * Planning-facing TypeShapeProvider backed by FrozenMetamodelImage.
 *
 * Hot-path lookup law:
 *
 * ```text
 * TypeReference -> frozenTypeOrdinal -> shapeTable[frozenTypeOrdinal]
 * ```
 *
 * This provider performs one TypeReference lookup against the image-local type
 * index, then reads shape material by primitive frozen type ordinal.
 *
 * Primitive ordinal safety:
 *
 * The primitive ordinal is intentionally kept as a short-lived local variable.
 * It is not stored, returned, persisted, or mixed with any other ordinal kind.
 *
 * Integrity boundary:
 *
 * FrozenMetamodelImage.issue(...) must already have validated table-size
 * equality, table coverage, and shape.subject continuity before this provider
 * is constructed.
 *
 * Therefore, null from shapeTable.findShapeAt(frozenTypeOrdinal) is treated as
 * a frozen-image integrity failure, not as an ordinary miss.
 */
class FrozenMetamodelTypeShapeProvider private constructor(
    private val image: FrozenMetamodelImage,
) : TypeShapeProvider {
    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        val frozenTypeOrdinal = image.typeIndex.ordinalOf(reference)

        if (frozenTypeOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
            )
        }

        return image.shapeTable.findShapeAt(frozenTypeOrdinal)
            ?: throw FrozenMetamodelIncompleteTableException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
                reason = "TypeReference exists in the frozen type index but has no shape record at frozenTypeOrdinal=$frozenTypeOrdinal.",
            )
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelTypeShapeProvider =
            FrozenMetamodelTypeShapeProvider(
                image = image,
            )
    }
}
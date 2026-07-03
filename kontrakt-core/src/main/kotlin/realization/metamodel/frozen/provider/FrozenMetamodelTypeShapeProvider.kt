package realization.metamodel.frozen.provider

import stage.admission.diagnostics.evidence.FrozenMetamodelIncompleteTableException
import stage.admission.diagnostics.evidence.FrozenMetamodelUnknownTypeReferenceException
import stage.canonicalization.material.TypeReference
import stage.canonicalization.material.frozen.image.FrozenMetamodelImage
import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId
import stage.canonicalization.material.frozen.table.FrozenTypeReferenceIndex
import stage.input.presentation.raw.ResolvedTypeShape
import stage.lowering.boundary.TypeShapeProvider

/**
 * Planning-facing TypeShapeProvider backed by FrozenMetamodelImage.
 *
 * Hexagonal role:
 *
 * - outbound port implementation for Planning;
 * - does not depend on Reflection/KSP/bytecode/source APIs;
 * - receives FrozenMetamodelImage only, never FrozenMetamodelImageEnvelope or
 *   diagnostic provenance.
 *
 * Compiler role:
 *
 * - reads pre-frozen shape material;
 * - does not enumerate constructors;
 * - does not enumerate properties;
 * - does not perform backend discovery;
 * - does not reopen backend handles.
 *
 * Lookup law:
 *
 * This provider uses the frozen image's ordinal path:
 *
 * ```text
 * TypeReference
 * -> FrozenTypeReferenceIndex.ordinalOf(reference)
 * -> FrozenTypeShapeTable.findShapeAt(frozenOrdinal)
 * ```
 *
 * It must not use the old double lookup pattern:
 *
 * ```text
 * typeIndex.contains(reference)
 * shapeTable.findShape(reference)
 * ```
 *
 * Exception law:
 *
 * - absent from type index:
 *   FrozenMetamodelUnknownTypeReferenceException
 *
 * - present in type index but missing shape slot:
 *   FrozenMetamodelIncompleteTableException
 */
class FrozenMetamodelTypeShapeProvider private constructor(
    private val image: FrozenMetamodelImage,
) : TypeShapeProvider {
    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        val frozenOrdinal =
            image.typeIndex.ordinalOf(
                reference = reference,
            )

        if (frozenOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
            )
        }

        return image.shapeTable.findShapeAt(
            frozenOrdinal = frozenOrdinal,
        ) ?: throw FrozenMetamodelIncompleteTableException(
            imageId = image.imageId,
            referenceSummary = reference.renderSummary(),
            missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
            reason = "TypeReference exists in type index but has no shape record: " +
                    "frozenOrdinal=$frozenOrdinal.",
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelTypeShapeProvider {
            return FrozenMetamodelTypeShapeProvider(
                image = image,
            )
        }
    }
}
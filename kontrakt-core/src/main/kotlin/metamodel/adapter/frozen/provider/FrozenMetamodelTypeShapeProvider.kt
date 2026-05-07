package metamodel.adapter.frozen.provider

import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelUnknownTypeReferenceException
import metamodel.domain.frozen.image.FrozenMetamodelImage
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.vo.TypeReference
import planning.domain.port.outgoing.TypeShapeProvider

/**
 * Planning-facing TypeShapeProvider backed by FrozenMetamodelImage.
 *
 * Hexagonal role:
 * - implements the Planning outbound port;
 * - reads only adapter-neutral frozen metamodel material;
 * - does not depend on Reflection, KSP, bytecode, source-analysis, or generated backend handles.
 *
 * Compiler-stage role:
 * - returns coarse type-shape material;
 * - does not enumerate constructors;
 * - does not enumerate properties;
 * - does not perform active-member projection;
 * - does not perform active-member ordering.
 *
 * ADR-0039 boundary:
 * - this provider receives FrozenMetamodelImage only;
 * - it must not receive FrozenMetamodelImageEnvelope;
 * - it must not receive FrozenMetamodelImageDiagnosticHeader;
 * - it must not branch on source adapter provenance.
 */
class FrozenMetamodelTypeShapeProvider private constructor(
    private val image: FrozenMetamodelImage,
) : TypeShapeProvider {
    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        if (!image.typeIndex.contains(reference)) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
            )
        }

        return image.shapeTable.findShape(reference)
            ?: throw FrozenMetamodelIncompleteTableException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
                reason = "TypeReference exists in the frozen type index but has no shape record.",
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
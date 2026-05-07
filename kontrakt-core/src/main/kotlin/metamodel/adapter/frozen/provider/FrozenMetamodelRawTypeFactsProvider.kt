package metamodel.adapter.frozen.provider

import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelUnknownTypeReferenceException
import metamodel.domain.frozen.image.FrozenMetamodelImage
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.vo.TypeReference
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.RawTypeFactsResolution

/**
 * Planning-facing RawTypeFactsProvider backed by FrozenMetamodelImage.
 *
 * This provider reads frozen adapter-neutral raw fact material.
 *
 * It must not:
 * - perform backend discovery;
 * - reopen KType/KClass/KSType/KSDeclaration handles;
 * - access adapter-local handle registries;
 * - branch on source adapter provenance.
 *
 * Accounting rule:
 * - reading from a FrozenMetamodelImage is a frozen/cache hit;
 * - it is not actual backend fact discovery;
 * - therefore the returned resolution is RawTypeFactsResolution.cacheHit(...).
 *
 * ADR-0037 boundary:
 * - this provider is called only after active-cycle detection reports cycle miss;
 * - cycle-hit paths must not call this provider for the current cycle-hit type.
 *
 * ADR-0039 boundary:
 * - this provider receives FrozenMetamodelImage only;
 * - it must not receive FrozenMetamodelImageEnvelope;
 * - it must not receive FrozenMetamodelImageDiagnosticHeader.
 */
class FrozenMetamodelRawTypeFactsProvider private constructor(
    private val image: FrozenMetamodelImage,
) : RawTypeFactsProvider {
    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        if (!image.typeIndex.contains(reference)) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
            )
        }

        val facts =
            image.rawFactTable.findFacts(reference)
                ?: throw FrozenMetamodelIncompleteTableException(
                    imageId = image.imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                    reason = "TypeReference exists in the frozen type index but has no raw fact record.",
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
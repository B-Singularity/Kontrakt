package metamodel.adapter.frozen.provider

import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId
import stage.canonicalization.material.frozen.table.FrozenTypeReferenceIndex
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.RawTypeFactsResolution
import stage.canonicalization.material.TypeReference
import stage.canonicalization.material.frozen.image.FrozenMetamodelImage
import stage.input.diagnostics.FrozenMetamodelIncompleteTableException
import stage.input.diagnostics.FrozenMetamodelUnknownTypeReferenceException

/**
 * Planning-facing RawTypeFactsProvider backed by FrozenMetamodelImage.
 *
 * This provider must not perform backend discovery.
 *
 * Even if RawTypeFactsDTO is materialized lazily from a frozen raw fact record,
 * the correct accounting category is cacheHit/frozen-hit, not actual backend
 * resolution.
 *
 * Hexagonal role:
 *
 * - outbound port implementation for Planning;
 * - does not depend on Reflection/KSP/bytecode/source APIs;
 * - receives FrozenMetamodelImage only, never FrozenMetamodelImageEnvelope or
 *   diagnostic provenance.
 *
 * Lookup law:
 *
 * This provider uses the frozen image's ordinal path:
 *
 * ```text
 * TypeReference
 * -> FrozenTypeReferenceIndex.ordinalOf(reference)
 * -> FrozenRawFactTable.findFactsAt(frozenOrdinal)
 * ```
 *
 * It must not use the old double lookup pattern:
 *
 * ```text
 * typeIndex.contains(reference)
 * rawFactTable.findFacts(reference)
 * ```
 *
 * Resolution accounting law:
 *
 * A successful frozen read returns:
 *
 * ```text
 * RawTypeFactsResolution.cacheHit(...)
 * ```
 *
 * It must not report backend actual resolution because all backend discovery
 * has already happened before freeze publication.
 *
 * Exception law:
 *
 * - absent from type index:
 *   FrozenMetamodelUnknownTypeReferenceException
 *
 * - present in type index but missing raw fact slot:
 *   FrozenMetamodelIncompleteTableException
 */
class FrozenMetamodelRawTypeFactsProvider private constructor(
    private val image: FrozenMetamodelImage,
) : RawTypeFactsProvider {
    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        val frozenOrdinal =
            image.typeIndex.ordinalOf(
                reference = reference,
            )

        if (frozenOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
            )
        }

        val facts =
            image.rawFactTable.findFactsAt(
                frozenOrdinal = frozenOrdinal,
            ) ?: throw FrozenMetamodelIncompleteTableException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                reason = "TypeReference exists in type index but has no raw fact record: " +
                        "frozenOrdinal=$frozenOrdinal.",
            )

        return RawTypeFactsResolution.cacheHit(
            facts = facts,
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelRawTypeFactsProvider {
            return FrozenMetamodelRawTypeFactsProvider(
                image = image,
            )
        }
    }
}
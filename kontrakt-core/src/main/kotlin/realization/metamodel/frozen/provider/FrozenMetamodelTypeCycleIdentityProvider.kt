package realization.metamodel.frozen.provider

import stage.admission.diagnostics.evidence.FrozenMetamodelIncompleteTableException
import stage.admission.diagnostics.evidence.FrozenMetamodelUnknownTypeReferenceException
import stage.canonicalization.material.frozen.image.FrozenMetamodelImage
import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId
import stage.canonicalization.material.frozen.table.FrozenTypeReferenceIndex
import stage.canonicalization.material.representation.TypeReference
import stage.lowering.boundary.TypeCycleIdentityProvider
import stage.lowering.material.expansion.TypeCycleIdentity

/**
 * Planning-facing TypeCycleIdentityProvider backed by FrozenMetamodelImage.
 *
 * Hexagonal role:
 *
 * - outbound port implementation for Planning;
 * - does not depend on Reflection/KSP/bytecode/source APIs;
 * - receives FrozenMetamodelImage only, never FrozenMetamodelImageEnvelope or
 *   diagnostic provenance.
 *
 * Algorithm authority law:
 *
 * This provider does not accept caller-supplied identity algorithm metadata.
 *
 * The cycle identity algorithm authority belongs to
 * FrozenTypeCycleIdentityTable.
 *
 * Therefore:
 *
 * ```text
 * provider.identityAlgorithmId
 *     == image.cycleIdentityTable.identityAlgorithmId
 *
 * provider.identityAlgorithmVersion
 *     == image.cycleIdentityTable.identityAlgorithmVersion
 * ```
 *
 * This prevents provider/table metadata drift.
 *
 * Lookup law:
 *
 * This provider uses the frozen image's ordinal path:
 *
 * ```text
 * TypeReference
 * -> FrozenTypeReferenceIndex.ordinalOf(reference)
 * -> FrozenTypeCycleIdentityTable.findCycleIdentityAt(frozenOrdinal)
 * ```
 *
 * It must not use the old double lookup pattern:
 *
 * ```text
 * typeIndex.contains(reference)
 * cycleIdentityTable.findCycleIdentity(reference)
 * ```
 *
 * Exception law:
 *
 * - absent from type index:
 *   FrozenMetamodelUnknownTypeReferenceException
 *
 * - present in type index but missing cycle identity slot:
 *   FrozenMetamodelIncompleteTableException
 */
class FrozenMetamodelTypeCycleIdentityProvider private constructor(
    private val image: FrozenMetamodelImage,
) : TypeCycleIdentityProvider {
    override val identityAlgorithmId: String
        get() = image.cycleIdentityTable.identityAlgorithmId

    override val identityAlgorithmVersion: Long
        get() = image.cycleIdentityTable.identityAlgorithmVersion

    override fun resolveCycleIdentity(
        reference: TypeReference,
    ): TypeCycleIdentity {
        val frozenOrdinal =
            image.typeIndex.ordinalOf(
                reference = reference,
            )

        if (frozenOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
            )
        }

        return image.cycleIdentityTable.findCycleIdentityAt(
            frozenTypeOrdinal = frozenOrdinal,
        ) ?: throw FrozenMetamodelIncompleteTableException(
            imageId = image.imageId,
            referenceSummary = reference.renderSummary(),
            missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
            reason = "TypeReference exists in type index but has no cycle identity records: " +
                    "frozenOrdinal=$frozenOrdinal.",
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelTypeCycleIdentityProvider {
            return FrozenMetamodelTypeCycleIdentityProvider(
                image = image,
            )
        }
    }
}
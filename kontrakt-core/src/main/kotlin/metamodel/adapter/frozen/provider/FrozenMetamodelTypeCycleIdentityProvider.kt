package metamodel.adapter.frozen.provider

import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelUnknownTypeReferenceException
import metamodel.domain.frozen.image.FrozenMetamodelImage
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.vo.TypeReference
import planning.domain.expansion.TypeCycleIdentity
import planning.domain.port.outgoing.TypeCycleIdentityProvider

/**
 * Planning-facing TypeCycleIdentityProvider backed by FrozenMetamodelImage.
 *
 * Hot-path lookup law:
 *
 * ```text
 * TypeReference -> frozenTypeOrdinal -> cycleIdentityTable[frozenTypeOrdinal]
 * ```
 *
 * The provider's identityAlgorithmId and identityAlgorithmVersion are derived
 * from the frozen cycle identity table itself.
 *
 * This prevents a caller from accidentally constructing a provider whose
 * declared algorithm metadata differs from the image it reads.
 *
 * Algorithm validation is not performed on the hot path.
 * FrozenMetamodelImage.issue(...) must validate:
 *
 * - table algorithm id/version;
 * - every entry's algorithm id/version;
 * - every entry's subject continuity with the type index.
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
        val frozenTypeOrdinal = image.typeIndex.ordinalOf(reference)

        if (frozenTypeOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
            )
        }

        return image.cycleIdentityTable.findCycleIdentityAt(frozenTypeOrdinal)
            ?: throw FrozenMetamodelIncompleteTableException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
                reason = "TypeReference exists in the frozen type index but has no cycle identity record at frozenTypeOrdinal=$frozenTypeOrdinal.",
            )
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelTypeCycleIdentityProvider =
            FrozenMetamodelTypeCycleIdentityProvider(
                image = image,
            )
    }
}
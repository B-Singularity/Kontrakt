package metamodel.adapter.frozen.provider

import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelUnknownTypeReferenceException
import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.frozen.image.FrozenMetamodelImage
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import metamodel.domain.vo.TypeReference
import planning.domain.expansion.TypeCycleIdentity
import planning.domain.port.outgoing.TypeCycleIdentityProvider

/**
 * Planning-facing TypeCycleIdentityProvider backed by FrozenMetamodelImage.
 *
 * This provider reads already-frozen, adapter-neutral cycle identity material.
 *
 * It must not:
 * - derive identity from KType;
 * - derive identity from KClass;
 * - derive identity from KSType;
 * - derive identity from KSDeclaration;
 * - reopen backend handle registries;
 * - inspect source adapter provenance.
 *
 * ADR-0037 boundary:
 * - cycle identity preflight happens before raw facts;
 * - this provider does not enumerate constructors;
 * - this provider does not enumerate properties;
 * - this provider does not project or order active members.
 *
 * ADR-0039 boundary:
 * - this provider receives FrozenMetamodelImage only;
 * - it must not receive FrozenMetamodelImageEnvelope;
 * - it must not receive FrozenMetamodelImageDiagnosticHeader.
 */
class FrozenMetamodelTypeCycleIdentityProvider private constructor(
    private val image: FrozenMetamodelImage,
    override val identityAlgorithmId: String,
    override val identityAlgorithmVersion: Long,
) : TypeCycleIdentityProvider {
    override fun resolveCycleIdentity(
        reference: TypeReference,
    ): TypeCycleIdentity {
        if (!image.typeIndex.contains(reference)) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                requestedTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
            )
        }

        return image.cycleIdentityTable.findCycleIdentity(reference)
            ?: throw FrozenMetamodelIncompleteTableException(
                imageId = image.imageId,
                referenceSummary = reference.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
                reason = "TypeReference exists in the frozen type index but has no cycle identity record.",
            )
    }

    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
            identityAlgorithmId: String,
            identityAlgorithmVersion: Long,
        ): FrozenMetamodelTypeCycleIdentityProvider {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenMetamodelTypeCycleIdentityProvider.identityAlgorithmId",
                value = identityAlgorithmId,
                maxChars = 128,
            )

            if (identityAlgorithmVersion < 0L) {
                throw MetamodelFactContractViolationException(
                    "FrozenMetamodelTypeCycleIdentityProvider.identityAlgorithmVersion must be non-negative: " +
                            "identityAlgorithmVersion=$identityAlgorithmVersion",
                )
            }

            return FrozenMetamodelTypeCycleIdentityProvider(
                image = image,
                identityAlgorithmId = identityAlgorithmId,
                identityAlgorithmVersion = identityAlgorithmVersion,
            )
        }
    }
}
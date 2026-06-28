package metamodel.domain.frozen.table

import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import planning.domain.expansion.TypeCycleIdentity
import stage.input.diagnostics.FrozenMetamodelSequenceIndexOutOfBoundsException
import stage.input.diagnostics.MetamodelFactContractViolationException
import versioning.coordinate.contract.frozen.image.FrozenMetamodelImageSchemaVersion

/**
 * Object-array-backed FrozenTypeCycleIdentityTable.
 *
 * This is the Level 1 ordinal-addressed frozen cycle-identity table.
 *
 * Algorithm authority law:
 *
 * The table owns the cycle identity algorithm metadata for every entry it
 * contains.
 *
 * Every non-null TypeCycleIdentity returned by this table must match:
 *
 * ```text
 * identity.identityAlgorithmId == identityAlgorithmId
 * identity.identityAlgorithmVersion == identityAlgorithmVersion
 * ```
 *
 * Provider implementations must derive TypeCycleIdentityProvider metadata from
 * this table instead of accepting separate caller-supplied metadata.
 *
 * Lookup law:
 *
 * This table is addressed only by FrozenTypeReferenceIndex image-local frozen
 * type ordinal.
 *
 * Invalid ordinal access is a domain contract violation and must not be hidden
 * as a nullable miss.
 *
 * Difference:
 *
 * - valid ordinal + null slot:
 *   missing cycle-identity coverage;
 *
 * - invalid ordinal:
 *   caller/index/table contract violation.
 *
 * Allocation law:
 *
 * findCycleIdentityAt(...) must not create a fresh TypeCycleIdentity on every
 * lookup.
 *
 * The baseline object-array implementation returns pre-frozen object
 * references.
 */
class ObjectArrayFrozenTypeCycleIdentityTable private constructor(
    private val imageId: FrozenMetamodelImageId,
    override val schemaVersion: FrozenMetamodelImageSchemaVersion,
    override val identityAlgorithmId: String,
    override val identityAlgorithmVersion: Long,
    private val identities: Array<TypeCycleIdentity?>,
) : FrozenTypeCycleIdentityTable {
    override val size: Int
        get() = identities.size

    override fun containsAt(
        frozenOrdinal: Int,
    ): Boolean {
        requireValidOrdinal(
            frozenOrdinal = frozenOrdinal,
        )

        return identities[frozenOrdinal] != null
    }

    override fun findCycleIdentityAt(
        frozenOrdinal: Int,
    ): TypeCycleIdentity? {
        requireValidOrdinal(
            frozenOrdinal = frozenOrdinal,
        )

        return identities[frozenOrdinal]
    }

    private fun requireValidOrdinal(
        frozenOrdinal: Int,
    ) {
        if (frozenOrdinal >= 0 && frozenOrdinal < identities.size) {
            return
        }

        throw FrozenMetamodelSequenceIndexOutOfBoundsException(
            imageId = imageId,
            sequenceTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
            index = frozenOrdinal,
            size = identities.size,
        )
    }

    override fun toString(): String {
        return "ObjectArrayFrozenTypeCycleIdentityTable(" +
                "size=$size, " +
                "schemaVersion=$schemaVersion, " +
                "identityAlgorithmId=$identityAlgorithmId, " +
                "identityAlgorithmVersion=$identityAlgorithmVersion" +
                ")"
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            identityAlgorithmId: String,
            identityAlgorithmVersion: Long,
            identities: Array<TypeCycleIdentity?>,
        ): ObjectArrayFrozenTypeCycleIdentityTable {
            requireIdentityAlgorithmId(
                identityAlgorithmId = identityAlgorithmId,
            )

            requireIdentityAlgorithmVersion(
                identityAlgorithmVersion = identityAlgorithmVersion,
            )

            requireEntryAlgorithmContinuity(
                identityAlgorithmId = identityAlgorithmId,
                identityAlgorithmVersion = identityAlgorithmVersion,
                identities = identities,
            )

            return ObjectArrayFrozenTypeCycleIdentityTable(
                imageId = imageId,
                schemaVersion = schemaVersion,
                identityAlgorithmId = identityAlgorithmId,
                identityAlgorithmVersion = identityAlgorithmVersion,
                identities = identities.copyOf(),
            )
        }

        private fun requireIdentityAlgorithmId(
            identityAlgorithmId: String,
        ) {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "ObjectArrayFrozenTypeCycleIdentityTable.identityAlgorithmId",
                value = identityAlgorithmId,
                maxChars = MAX_ALGORITHM_ID_CHARS,
            )
        }

        private fun requireIdentityAlgorithmVersion(
            identityAlgorithmVersion: Long,
        ) {
            if (identityAlgorithmVersion >= 0L) {
                return
            }

            throw MetamodelFactContractViolationException(
                "ObjectArrayFrozenTypeCycleIdentityTable.identityAlgorithmVersion must be non-negative: " +
                        "identityAlgorithmVersion=$identityAlgorithmVersion",
            )
        }

        private fun requireEntryAlgorithmContinuity(
            identityAlgorithmId: String,
            identityAlgorithmVersion: Long,
            identities: Array<TypeCycleIdentity?>,
        ) {
            var index = 0

            while (index < identities.size) {
                val identity = identities[index]

                if (identity != null) {
                    requireEntryAlgorithmContinuity(
                        identityAlgorithmId = identityAlgorithmId,
                        identityAlgorithmVersion = identityAlgorithmVersion,
                        identity = identity,
                        index = index,
                    )
                }

                index += 1
            }
        }

        private fun requireEntryAlgorithmContinuity(
            identityAlgorithmId: String,
            identityAlgorithmVersion: Long,
            identity: TypeCycleIdentity,
            index: Int,
        ) {
            if (
                identity.identityAlgorithmId == identityAlgorithmId &&
                identity.identityAlgorithmVersion == identityAlgorithmVersion
            ) {
                return
            }

            throw MetamodelFactContractViolationException(
                "ObjectArrayFrozenTypeCycleIdentityTable entry algorithm mismatch: " +
                        "index=$index, " +
                        "table.identityAlgorithmId=$identityAlgorithmId, " +
                        "table.identityAlgorithmVersion=$identityAlgorithmVersion, " +
                        "entry.identityAlgorithmId=${identity.identityAlgorithmId}, " +
                        "entry.identityAlgorithmVersion=${identity.identityAlgorithmVersion}",
            )
        }

        private const val MAX_ALGORITHM_ID_CHARS: Int = 128
    }
}
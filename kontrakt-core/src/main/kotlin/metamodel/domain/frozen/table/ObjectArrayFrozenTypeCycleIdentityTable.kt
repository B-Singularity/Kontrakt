package metamodel.domain.frozen.table

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import planning.domain.expansion.TypeCycleIdentity

/**
 * Object-array-backed FrozenTypeCycleIdentityTable.
 *
 * The table owns the cycle identity algorithm metadata for all entries.
 *
 * Provider implementations must derive TypeCycleIdentityProvider metadata from
 * this table instead of accepting separate caller-supplied metadata.
 */
class ObjectArrayFrozenTypeCycleIdentityTable private constructor(
    override val identityAlgorithmId: String,
    override val identityAlgorithmVersion: Long,
    private val identities: Array<TypeCycleIdentity?>,
) : FrozenTypeCycleIdentityTable {
    override val size: Int
        get() = identities.size

    override fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean =
        isInBounds(frozenTypeOrdinal) && identities[frozenTypeOrdinal] != null

    override fun findCycleIdentityAt(
        frozenTypeOrdinal: Int,
    ): TypeCycleIdentity? {
        if (!isInBounds(frozenTypeOrdinal)) {
            return null
        }

        return identities[frozenTypeOrdinal]
    }

    private fun isInBounds(
        frozenTypeOrdinal: Int,
    ): Boolean =
        frozenTypeOrdinal >= 0 && frozenTypeOrdinal < identities.size

    companion object {
        @JvmStatic
        fun issue(
            identityAlgorithmId: String,
            identityAlgorithmVersion: Long,
            identities: Array<TypeCycleIdentity?>,
        ): ObjectArrayFrozenTypeCycleIdentityTable {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "ObjectArrayFrozenTypeCycleIdentityTable.identityAlgorithmId",
                value = identityAlgorithmId,
                maxChars = MAX_ALGORITHM_ID_CHARS,
            )

            if (identityAlgorithmVersion < 0L) {
                throw MetamodelFactContractViolationException(
                    "ObjectArrayFrozenTypeCycleIdentityTable.identityAlgorithmVersion must be non-negative: " +
                            "identityAlgorithmVersion=$identityAlgorithmVersion",
                )
            }

            return ObjectArrayFrozenTypeCycleIdentityTable(
                identityAlgorithmId = identityAlgorithmId,
                identityAlgorithmVersion = identityAlgorithmVersion,
                identities = identities.copyOf(),
            )
        }

        private const val MAX_ALGORITHM_ID_CHARS: Int = 128
    }
}
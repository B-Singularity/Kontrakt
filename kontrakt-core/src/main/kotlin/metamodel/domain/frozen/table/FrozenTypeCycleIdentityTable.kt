package metamodel.domain.frozen.table

import planning.domain.expansion.TypeCycleIdentity

/**
 * Frozen type cycle identity table.
 *
 * This table is addressed by FrozenTypeReferenceIndex's image-local primitive
 * frozen type ordinal.
 *
 * Algorithm law:
 *
 * The table owns the cycle-identity algorithm metadata for all entries it
 * contains. Every non-null TypeCycleIdentity returned by this table must match:
 *
 * ```text
 * identity.identityAlgorithmId == identityAlgorithmId
 * identity.identityAlgorithmVersion == identityAlgorithmVersion
 * ```
 *
 * Provider implementations must derive their TypeCycleIdentityProvider metadata
 * from this table. They must not accept a separate caller-supplied algorithm
 * id/version.
 *
 * Allocation law:
 *
 * findCycleIdentityAt(...) must not create a fresh TypeCycleIdentity on every
 * lookup. The baseline object-array implementation returns pre-frozen object
 * references.
 */
interface FrozenTypeCycleIdentityTable {
    val size: Int

    val identityAlgorithmId: String

    val identityAlgorithmVersion: Long

    fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean

    fun findCycleIdentityAt(
        frozenTypeOrdinal: Int,
    ): TypeCycleIdentity?
}
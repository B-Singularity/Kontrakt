package metamodel.domain.frozen.table

import planning.domain.expansion.TypeCycleIdentity

/**
 * Frozen type cycle identity table.
 *
 * Implementations must be immutable after freeze.
 *
 * Algorithm law:
 *
 * The table owns the cycle-identity algorithm metadata for all entries it
 * contains.
 *
 * Every non-null TypeCycleIdentity returned by this table must have:
 *
 * ```text
 * identity.identityAlgorithmId == identityAlgorithmId
 * identity.identityAlgorithmVersion == identityAlgorithmVersion
 * ```
 *
 * FrozenMetamodelImage.issue(...) must validate this before publication.
 *
 * The provider must derive its TypeCycleIdentityProvider algorithm metadata from
 * this table. The provider must not accept an independent caller-supplied
 * algorithm id/version that can drift from the frozen image.
 *
 * Source adapter provenance is intentionally not part of this validation.
 * Provenance is diagnostic-only; cycle identity algorithm metadata is semantic
 * protocol material.
 *
 * Size law:
 *
 * - size must equal FrozenTypeReferenceIndex.size for the owning image;
 * - FrozenMetamodelImage.issue(...) must validate this before publication;
 * - concrete implementations must reject or safely return null/false for
 *   out-of-range ordinals.
 *
 * The value returned from this table must already be adapter-neutral cycle
 * identity material.
 *
 * It must not be derived from KType/KClass/KSType/KSDeclaration at provider read
 * time.
 *
 * Ordinal access law:
 *
 * This table is addressed by FrozenTypeReferenceIndex's image-local primitive
 * frozen type ordinal.
 *
 * This table must not perform its own TypeReference hash lookup on the planning
 * hot path.
 *
 * Implementation law:
 *
 * Table implementations must be plain-data, object-array-backed,
 * ordinal-indexed, slab-backed, or primitive-array-backed.
 *
 * They must not store:
 *
 * - lambdas;
 * - suppliers;
 * - lazy delegates;
 * - service locators;
 * - callbacks;
 * - closures capturing KType/KClass/KSType/KSDeclaration;
 * - registry keys that can recover backend handles.
 *
 * Allocation law:
 *
 * findCycleIdentityAt(...) must not create a fresh TypeCycleIdentity object on
 * every lookup.
 *
 * The baseline implementation should return a pre-frozen immutable object
 * reference. Later slab-backed implementations may materialize through a
 * dedicated non-allocating or bounded-allocation protocol, but the default
 * object-array table must not allocate per provider read.
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
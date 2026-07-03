package stage.canonicalization.material.frozen.table

import realization.planning.expansion.TypeCycleIdentity
import versioning.coordinate.material.value.FrozenMetamodelImageSchemaVersion

/**
 * Frozen type cycle-identity table.
 *
 * This table is addressed by the FrozenTypeReferenceIndex image-local frozen
 * type ordinal.
 *
 * It is not keyed by TypeReference at read time.
 *
 * Reason:
 *
 * TypeReference lookup authority belongs to FrozenTypeReferenceIndex.
 * Once a caller has resolved:
 *
 * ```text
 * reference -> frozenTypeOrdinal
 * ```
 *
 * cycle identity reads must be ordinal-based:
 *
 * ```text
 * cycleIdentityTable.findCycleIdentityAt(frozenTypeOrdinal)
 * ```
 *
 * This removes duplicate lookup pressure from provider hot paths.
 *
 * Algorithm authority law:
 *
 * This table owns the cycle-identity algorithm metadata for every entry it
 * contains.
 *
 * Every non-null TypeCycleIdentity returned by this table must match:
 *
 * ```text
 * identity.identityAlgorithmId == identityAlgorithmId
 * identity.identityAlgorithmVersion == identityAlgorithmVersion
 * ```
 *
 * Provider implementations must derive their exposed TypeCycleIdentityProvider
 * metadata from this table.
 *
 * They must not accept a separate caller-supplied algorithm id or algorithm
 * version, because doing so would create two authorities:
 *
 * ```text
 * table payload algorithm metadata
 * provider-declared algorithm metadata
 * ```
 *
 * That split can cause algorithm drift where the provider claims one identity
 * algorithm while the frozen table contains identities produced by another.
 *
 * Coverage law:
 *
 * containsAt(frozenTypeOrdinal) means this table has explicit frozen
 * cycle-identity coverage for the TypeReference stored at the same ordinal in
 * the owning FrozenTypeReferenceIndex.
 *
 * Missing coverage for an indexed ordinal is an incomplete frozen image and
 * must be rejected before FrozenMetamodelImage publication.
 *
 * Schema law:
 *
 * [schemaVersion] must match the owning FrozenMetamodelImage schema version.
 *
 * Size law:
 *
 * [size] must match the owning FrozenTypeReferenceIndex size.
 *
 * Allocation law:
 *
 * findCycleIdentityAt(...) must not create a fresh TypeCycleIdentity on every
 * lookup.
 *
 * The baseline object-array implementation returns pre-frozen object
 * references.
 *
 * Backend-erasure law:
 *
 * Returned TypeCycleIdentity values must already be adapter-neutral cycle
 * identity material.
 *
 * The table must not derive cycle identity from KType/KSP/bytecode/source
 * handles at provider read time.
 *
 * Table implementations must not store:
 *
 * - KType;
 * - KClass;
 * - KSType;
 * - KSDeclaration;
 * - reflection handles;
 * - bytecode parser handles;
 * - source AST/PSI handles;
 * - lambdas;
 * - suppliers;
 * - lazy delegates;
 * - service locators;
 * - callbacks;
 * - registry keys that can recover backend handles.
 *
 * Access law:
 *
 * Invalid ordinal access must fail through a metamodel-domain exception from the
 * concrete table implementation.
 *
 * It must not leak raw JVM array exceptions across the frozen-domain boundary.
 */
interface FrozenTypeCycleIdentityTable {
    val schemaVersion: FrozenMetamodelImageSchemaVersion

    val size: Int

    /**
     * Frozen table-wide cycle identity algorithm id.
     *
     * This is table metadata, not provider-local metadata.
     *
     * It must be validated at table issue time and cross-checked against every
     * non-null TypeCycleIdentity entry.
     */
    val identityAlgorithmId: String

    /**
     * Frozen table-wide cycle identity algorithm version.
     *
     * This is table metadata, not provider-local metadata.
     *
     * It must be validated at table issue time and cross-checked against every
     * non-null TypeCycleIdentity entry.
     */
    val identityAlgorithmVersion: Long

    fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean

    fun findCycleIdentityAt(
        frozenTypeOrdinal: Int,
    ): TypeCycleIdentity?
}
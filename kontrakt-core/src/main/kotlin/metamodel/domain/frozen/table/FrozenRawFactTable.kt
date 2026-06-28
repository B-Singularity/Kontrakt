package metamodel.domain.frozen.table

import stage.input.material.RawTypeFactsDTO
import versioning.coordinate.contract.frozen.image.FrozenMetamodelImageSchemaVersion

/**
 * Frozen raw fact table.
 *
 * This table is indexed by frozen type ordinal.
 *
 * It may return an already materialized RawTypeFactsDTO or may materialize it
 * from frozen adapter-neutral raw fact records.
 *
 * It must not return to backend-native handles.
 *
 * Lookup law:
 *
 * TypeReference lookup authority belongs to FrozenTypeReferenceIndex.
 *
 * Once a caller has resolved:
 *
 * ```text
 * reference -> frozenOrdinal
 * ```
 *
 * raw fact reads must be ordinal-based:
 *
 * ```text
 * rawFactTable.findFactsAt(frozenOrdinal)
 * ```
 *
 * This avoids the old double-lookup pattern:
 *
 * ```text
 * typeIndex.contains(reference)
 * rawFactTable.findFacts(reference)
 * ```
 *
 * Coverage law:
 *
 * containsAt(ordinal) means this table has explicit frozen raw-fact coverage
 * for the TypeReference stored at the same ordinal in the owning
 * FrozenTypeReferenceIndex.
 *
 * Coverage may be:
 *
 * - materialized RawTypeFactsDTO;
 * - a frozen raw fact record;
 * - a deterministic sentinel record such as TRUNCATED;
 * - a deterministic sentinel record such as FILTERED_BY_POLICY;
 * - a deterministic sentinel record such as UNAVAILABLE_FROM_BACKEND;
 * - an ACQUISITION_FAILED diagnostic record.
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
 * Materialization law:
 *
 * findFactsAt(...) returns frozen adapter-neutral raw fact material.
 *
 * It must not:
 *
 * - perform backend discovery;
 * - reopen backend handles;
 * - touch reflection/KSP/source/bytecode handles;
 * - allocate closure-backed lazy cells;
 * - be metered as backend raw fact resolution.
 *
 * Backend-erasure law:
 *
 * Table implementations must be immutable, closure-free, and adapter-neutral.
 *
 * They must not store:
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
 * concrete table implementation. It must not leak raw JVM array exceptions
 * across the frozen-domain boundary.
 */
interface FrozenRawFactTable {
    val schemaVersion: FrozenMetamodelImageSchemaVersion

    val size: Int

    fun containsAt(
        frozenOrdinal: Int,
    ): Boolean

    fun findFactsAt(
        frozenOrdinal: Int,
    ): RawTypeFactsDTO?
}
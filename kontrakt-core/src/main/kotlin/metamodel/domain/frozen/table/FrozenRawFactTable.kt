package metamodel.domain.frozen.table

import metamodel.domain.dto.RawTypeFactsDTO

/**
 * Frozen raw fact table.
 *
 * The table may return an already materialized RawTypeFactsDTO or may
 * materialize it from frozen adapter-neutral raw fact records.
 *
 * It must not return to backend-native handles.
 *
 * Size law:
 *
 * - size must equal FrozenTypeReferenceIndex.size for the owning image;
 * - FrozenMetamodelImage.issue(...) must validate this before publication;
 * - concrete implementations must reject or safely return null/false for
 *   out-of-range ordinals.
 *
 * Coverage law:
 *
 * containsAt(frozenTypeOrdinal) means this table has explicit frozen coverage
 * for the TypeReference assigned to that image-local ordinal.
 *
 * Coverage may be:
 *
 * - a materialized RawTypeFactsDTO;
 * - a frozen raw fact record;
 * - a deterministic sentinel record such as TRUNCATED;
 * - a deterministic sentinel record such as FILTERED_BY_POLICY;
 * - a deterministic sentinel record such as UNAVAILABLE_FROM_BACKEND;
 * - an ACQUISITION_FAILED diagnostic record.
 *
 * Missing coverage is an incomplete frozen image.
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
 */
interface FrozenRawFactTable {
    val size: Int

    fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean

    fun findFactsAt(
        frozenTypeOrdinal: Int,
    ): RawTypeFactsDTO?
}
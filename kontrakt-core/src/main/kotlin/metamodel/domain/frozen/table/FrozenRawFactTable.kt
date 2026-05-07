package metamodel.domain.frozen.table

import metamodel.domain.dto.RawTypeFactsDTO

/**
 * Frozen raw fact table.
 *
 * This table is addressed by FrozenTypeReferenceIndex's image-local primitive
 * frozen type ordinal.
 *
 * The baseline object-array implementation stores already-materialized
 * RawTypeFactsDTO references.
 *
 * Later implementations may materialize DTOs from frozen adapter-neutral raw
 * records, but must still not return to backend-native handles.
 *
 * Coverage law:
 *
 * containsAt(frozenTypeOrdinal) means the table has explicit coverage for the
 * type assigned to that frozen type ordinal.
 *
 * Missing coverage is an incomplete frozen image and must be rejected by
 * FrozenMetamodelImage.issue(...) before publication.
 *
 * Allocation law:
 *
 * findFactsAt(...) should not create avoidable per-read wrapper churn. The
 * baseline table returns a pre-frozen RawTypeFactsDTO reference.
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
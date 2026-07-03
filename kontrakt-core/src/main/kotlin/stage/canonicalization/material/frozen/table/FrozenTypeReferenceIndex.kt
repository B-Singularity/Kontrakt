package stage.canonicalization.material.frozen.table

import stage.canonicalization.material.frozen.table.FrozenTypeReferenceIndex.Companion.MISSING_ORDINAL
import stage.canonicalization.material.representation.TypeReference
import versioning.coordinate.material.value.FrozenMetamodelImageSchemaVersion

/**
 * Frozen TypeReference index.
 *
 * The type index is the coverage authority for one FrozenMetamodelImage.
 *
 * Ordinal law:
 *
 * - frozen type ordinal is local to exactly one FrozenMetamodelImage;
 * - frozen type ordinal is assigned only after deterministic reference ordering;
 * - frozen type ordinal must not be stored in TypeReference;
 * - frozen type ordinal must not be persisted as semantic identity;
 * - frozen type ordinal must not encode adapter acquisition order;
 * - frozen type ordinal must not be mixed with declaration ordinals,
 *   parameter indexes, constructor indexes, member local ordinals, or table
 *   implementation offsets;
 * - frozen type ordinal is a mechanical table-addressing value only.
 *
 * Determinism law:
 *
 * For the same semantic TypeReference set and the same schema/lowering law,
 * referenceAt(i) and ordinalOf(reference) must be independent from:
 *
 * - adapter acquisition order;
 * - reflection/KSP/backend enumeration order;
 * - HashMap iteration order;
 * - JVM object identity;
 * - classloader object identity;
 * - thread scheduling.
 *
 * Primitive safety rule:
 *
 * This interface deliberately uses Int rather than a wrapper/value class.
 *
 * Callers must keep the value short-lived and name it frozenTypeOrdinal.
 */
interface FrozenTypeReferenceIndex {
    val schemaVersion: FrozenMetamodelImageSchemaVersion

    val size: Int

    /**
     * Resolves image-local frozen type ordinal.
     *
     * Returns [MISSING_ORDINAL] if the reference is not part of this image.
     */
    fun ordinalOf(
        reference: TypeReference,
    ): Int

    fun contains(
        reference: TypeReference,
    ): Boolean =
        ordinalOf(reference) != MISSING_ORDINAL

    /**
     * Deterministic order access.
     *
     * This must not expose acquisition order.
     */
    fun referenceAt(
        frozenTypeOrdinal: Int,
    ): TypeReference

    companion object {
        const val MISSING_ORDINAL: Int = -1
    }
}
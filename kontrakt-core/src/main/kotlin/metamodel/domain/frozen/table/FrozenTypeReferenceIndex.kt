package metamodel.domain.frozen.table

import metamodel.domain.frozen.table.FrozenTypeReferenceIndex.Companion.MISSING_ORDINAL
import metamodel.domain.vo.TypeReference

/**
 * Frozen TypeReference index.
 *
 * The type index is the coverage authority for a FrozenMetamodelImage.
 *
 * Unknown reference and incomplete table are intentionally different failures:
 *
 * - absent from type index:
 *   the caller likely mixed image/scope material;
 *
 * - present in type index but missing from a table:
 *   freeze produced an incomplete image.
 *
 * Image-local ordinal law:
 *
 * - frozen ordinal is local to exactly one FrozenMetamodelImage;
 * - frozen ordinal is assigned only after deterministic reference ordering;
 * - frozen ordinal must not be stored in TypeReference;
 * - frozen ordinal must not be persisted as semantic identity;
 * - frozen ordinal must not encode adapter acquisition order;
 * - frozen ordinal must not be mixed with member ordinals, parameter ordinals,
 *   constructor ordinals, source declaration ordinals, or table-internal offsets;
 * - frozen ordinal is a mechanical table-addressing value only.
 *
 * Primitive safety rule:
 *
 * This interface deliberately uses Int rather than a value class.
 *
 * Because Int cannot encode its semantic role at compile time, implementations
 * and callers must follow a stricter discipline:
 *
 * - name variables `frozenTypeOrdinal`, not `ordinal`, `index`, or `id`;
 * - resolve the ordinal immediately before table access;
 * - do not store it in long-lived objects;
 * - do not pass it across image boundaries;
 * - do not perform arithmetic on it except deterministic validation loops;
 * - validate bounds inside concrete table implementations.
 *
 * Hot-path law:
 *
 * Planning-facing providers should resolve a TypeReference to one primitive Int
 * ordinal once, then read all frozen tables by ordinal.
 *
 * This avoids repeated hash/equality lookups against multiple tables.
 */
interface FrozenTypeReferenceIndex {
    val size: Int

    /**
     * Resolves the image-local frozen type ordinal for reference.
     *
     * Returns [MISSING_ORDINAL] when the reference is not part of this image.
     *
     * Implementations MUST NOT return any negative value other than
     * [MISSING_ORDINAL].
     */
    fun ordinalOf(
        reference: TypeReference,
    ): Int

    fun contains(
        reference: TypeReference,
    ): Boolean =
        ordinalOf(reference) != MISSING_ORDINAL

    /**
     * Deterministic canonical-order access for freeze-time validation and
     * diagnostics.
     *
     * This does not expose backend enumeration order.
     */
    fun referenceAt(
        frozenTypeOrdinal: Int,
    ): TypeReference

    companion object {
        const val MISSING_ORDINAL: Int = -1
    }
}
package metamodel.domain.frozen.table

import metamodel.domain.vo.TypeReference

/**
 * Frozen TypeReference index.
 *
 * Unknown reference and incomplete table are intentionally different failures:
 *
 * - absent from type index:
 *     the caller likely mixed images/scopes;
 *
 * - present in type index but missing from a table:
 *     freeze produced an incomplete image.
 *
 * Ordinal law:
 *
 * - frozenOrdinal is local to this image;
 * - it must be assigned after deterministic ordering;
 * - it must not be stored in TypeReference;
 * - it must not encode adapter acquisition order.
 */
interface FrozenTypeReferenceIndex {
    val size: Int

    fun contains(
        reference: TypeReference,
    ): Boolean

    /**
     * Deterministic canonical-order access for freeze-time validation.
     *
     * This does not expose backend enumeration order.
     */
    fun referenceAt(
        frozenOrdinal: Int,
    ): TypeReference
}
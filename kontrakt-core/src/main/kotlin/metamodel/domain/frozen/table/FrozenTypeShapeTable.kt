package metamodel.domain.frozen.table

import metamodel.domain.dto.ResolvedTypeShape

/**
 * Frozen type shape table.
 *
 * This table is addressed by FrozenTypeReferenceIndex's image-local primitive
 * frozen type ordinal.
 *
 * Size law:
 *
 * - size must equal FrozenTypeReferenceIndex.size for the owning image;
 * - FrozenMetamodelImage.issue(...) must validate this before publication;
 * - concrete implementations must safely return false/null for out-of-range
 *   ordinals.
 *
 * Allocation law:
 *
 * findShapeAt(...) must not create a fresh ResolvedTypeShape on every lookup.
 * The baseline object-array implementation returns pre-frozen object references.
 *
 * Erasure law:
 *
 * Table implementations must not store lambdas, suppliers, lazy delegates,
 * service locators, callbacks, closure-backed cells, or backend-handle recovery
 * keys.
 */
interface FrozenTypeShapeTable {
    val size: Int

    fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean

    fun findShapeAt(
        frozenTypeOrdinal: Int,
    ): ResolvedTypeShape?
}
package metamodel.domain.frozen.table

import metamodel.domain.dto.ResolvedTypeShape

/**
 * Frozen type shape table.
 *
 * Implementations must be immutable after freeze.
 *
 * Size law:
 *
 * - size must equal FrozenTypeReferenceIndex.size for the owning image;
 * - FrozenMetamodelImage.issue(...) must validate this before publication;
 * - concrete implementations must safely return false/null for out-of-range
 *   frozen type ordinals.
 *
 * Ordinal access law:
 *
 * This table is addressed by FrozenTypeReferenceIndex's image-local primitive
 * frozen type ordinal.
 *
 * This table must not perform its own TypeReference hash lookup on the planning
 * hot path.
 *
 * Subject-continuity law:
 *
 * Every non-null ResolvedTypeShape returned at frozenTypeOrdinal must have:
 *
 * ```text
 * shape.subject == typeIndex.referenceAt(frozenTypeOrdinal)
 * ```
 *
 * FrozenMetamodelImage.issue(...) must validate this before publication.
 *
 * Allocation law:
 *
 * findShapeAt(...) must not allocate a fresh ResolvedTypeShape object on every
 * lookup.
 *
 * The baseline implementation should return a pre-frozen immutable object
 * reference. Later slab-backed implementations may lower the representation
 * further, but provider reads must not create per-lookup DTO churn.
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
interface FrozenTypeShapeTable {
    val size: Int

    fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean

    fun findShapeAt(
        frozenTypeOrdinal: Int,
    ): ResolvedTypeShape?
}
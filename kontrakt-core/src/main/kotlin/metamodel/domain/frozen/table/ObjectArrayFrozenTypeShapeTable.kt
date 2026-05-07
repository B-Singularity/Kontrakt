package metamodel.domain.frozen.table

import metamodel.domain.dto.ResolvedTypeShape

/**
 * Object-array-backed FrozenTypeShapeTable.
 *
 * This implementation is intentionally simple:
 *
 * - one array slot per frozen type ordinal;
 * - no TypeReference lookup inside the table;
 * - no lambdas/suppliers/lazy delegates;
 * - no backend handle recovery key;
 * - no per-read ResolvedTypeShape allocation.
 *
 * The table does not validate subject continuity by itself.
 * FrozenMetamodelImage.issue(...) owns cross-table validation because only the
 * image has both the type index and all tables.
 */
class ObjectArrayFrozenTypeShapeTable private constructor(
    private val shapes: Array<ResolvedTypeShape?>,
) : FrozenTypeShapeTable {
    override val size: Int
        get() = shapes.size

    override fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean =
        isInBounds(frozenTypeOrdinal) && shapes[frozenTypeOrdinal] != null

    override fun findShapeAt(
        frozenTypeOrdinal: Int,
    ): ResolvedTypeShape? {
        if (!isInBounds(frozenTypeOrdinal)) {
            return null
        }

        return shapes[frozenTypeOrdinal]
    }

    private fun isInBounds(
        frozenTypeOrdinal: Int,
    ): Boolean =
        frozenTypeOrdinal >= 0 && frozenTypeOrdinal < shapes.size

    companion object {
        @JvmStatic
        fun issue(
            shapes: Array<ResolvedTypeShape?>,
        ): ObjectArrayFrozenTypeShapeTable =
            ObjectArrayFrozenTypeShapeTable(
                shapes = shapes.copyOf(),
            )
    }
}
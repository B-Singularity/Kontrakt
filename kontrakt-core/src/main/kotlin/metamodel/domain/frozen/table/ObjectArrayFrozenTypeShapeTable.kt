package metamodel.domain.frozen.table

import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.frozen.image.FrozenMetamodelImageSchemaVersion

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
    override val schemaVersion: FrozenMetamodelImageSchemaVersion,
    private val shapes: Array<ResolvedTypeShape?>,
) : FrozenTypeShapeTable {
    override val size: Int
        get() = shapes.size

    override fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean =
        frozenTypeOrdinal >= 0 &&
                frozenTypeOrdinal < shapes.size &&
                shapes[frozenTypeOrdinal] != null

    override fun findShapeAt(
        frozenTypeOrdinal: Int,
    ): ResolvedTypeShape? {
        if (frozenTypeOrdinal < 0 || frozenTypeOrdinal >= shapes.size) {
            return null
        }

        return shapes[frozenTypeOrdinal]
    }

    companion object {
        @JvmStatic
        fun issue(
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            shapes: Array<ResolvedTypeShape?>,
        ): ObjectArrayFrozenTypeShapeTable =
            ObjectArrayFrozenTypeShapeTable(
                schemaVersion = schemaVersion,
                shapes = shapes.copyOf(),
            )
    }
}
package stage.canonicalization.material.frozen.table

import stage.canonicalization.material.frozen.image.FrozenMetamodelImageId
import stage.input.diagnostics.FrozenMetamodelSequenceIndexOutOfBoundsException
import stage.input.material.ResolvedTypeShape
import versioning.coordinate.contract.frozen.image.FrozenMetamodelImageSchemaVersion

/**
 * Object-array-backed FrozenTypeShapeTable.
 *
 * This is the Level 1 ordinal-addressed frozen type-shape table.
 *
 * It is intentionally simple:
 *
 * - one nullable array slot per frozen type ordinal;
 * - no TypeReference lookup inside the table;
 * - no lambdas/suppliers/lazy delegates;
 * - no backend handle recovery key;
 * - no per-read ResolvedTypeShape allocation.
 *
 * Lookup law:
 *
 * This table is addressed only by FrozenTypeReferenceIndex image-local frozen
 * type ordinal.
 *
 * Invalid ordinal access is a domain contract violation and must not be hidden
 * as a nullable miss.
 *
 * Difference:
 *
 * - valid ordinal + null slot:
 *   missing table coverage;
 *
 * - invalid ordinal:
 *   caller/index/table contract violation.
 *
 * Subject-continuity law:
 *
 * The table does not validate subject continuity by itself.
 *
 * FrozenMetamodelImage.issue(...) owns cross-table validation because only the
 * image has both the type index and all tables.
 *
 * Shallow-copy law:
 *
 * issue(...) defensively copies the input array, but does not deep-copy
 * ResolvedTypeShape instances.
 *
 * ResolvedTypeShape must already be adapter-neutral frozen material.
 */
class ObjectArrayFrozenTypeShapeTable private constructor(
    private val imageId: FrozenMetamodelImageId,
    override val schemaVersion: FrozenMetamodelImageSchemaVersion,
    private val shapes: Array<ResolvedTypeShape?>,
) : FrozenTypeShapeTable {
    override val size: Int
        get() = shapes.size

    override fun containsAt(
        frozenOrdinal: Int,
    ): Boolean {
        requireValidOrdinal(
            frozenOrdinal = frozenOrdinal,
        )

        return shapes[frozenOrdinal] != null
    }

    override fun findShapeAt(
        frozenOrdinal: Int,
    ): ResolvedTypeShape? {
        requireValidOrdinal(
            frozenOrdinal = frozenOrdinal,
        )

        return shapes[frozenOrdinal]
    }

    private fun requireValidOrdinal(
        frozenOrdinal: Int,
    ) {
        if (frozenOrdinal >= 0 && frozenOrdinal < shapes.size) {
            return
        }

        throw FrozenMetamodelSequenceIndexOutOfBoundsException(
            imageId = imageId,
            sequenceTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
            index = frozenOrdinal,
            size = shapes.size,
        )
    }

    override fun toString(): String {
        return "ObjectArrayFrozenTypeShapeTable(size=$size, schemaVersion=$schemaVersion)"
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            shapes: Array<ResolvedTypeShape?>,
        ): ObjectArrayFrozenTypeShapeTable {
            return ObjectArrayFrozenTypeShapeTable(
                imageId = imageId,
                schemaVersion = schemaVersion,
                shapes = shapes.copyOf(),
            )
        }
    }
}
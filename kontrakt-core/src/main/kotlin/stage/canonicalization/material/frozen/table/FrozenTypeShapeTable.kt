package stage.canonicalization.material.frozen.table

import stage.input.presentation.raw.ResolvedTypeShape
import versioning.coordinate.material.value.FrozenMetamodelImageSchemaVersion

/**
 * Frozen type-shape table.
 *
 * This table is indexed by frozen type ordinal.
 *
 * It is not keyed by TypeReference at read time.
 *
 * Reason:
 *
 * TypeReference lookup authority belongs to FrozenTypeReferenceIndex.
 * Once a caller has resolved:
 *
 * ```text
 * reference -> frozenOrdinal
 * ```
 *
 * table reads must be ordinal-based:
 *
 * ```text
 * shapeTable.findShapeAt(frozenOrdinal)
 * ```
 *
 * This prevents provider hot paths from performing duplicate reference lookup
 * work across the type index and the table.
 *
 * Coverage law:
 *
 * containsAt(ordinal) means this table has explicit frozen shape coverage for
 * the TypeReference stored at the same ordinal in the owning
 * FrozenTypeReferenceIndex.
 *
 * Missing coverage for an indexed ordinal is an incomplete frozen image and
 * must be rejected before FrozenMetamodelImage publication.
 *
 * Schema law:
 *
 * [schemaVersion] must match the owning FrozenMetamodelImage schema version.
 *
 * Size law:
 *
 * [size] must match the owning FrozenTypeReferenceIndex size.
 *
 * Backend-erasure law:
 *
 * Table implementations must be immutable, closure-free, and adapter-neutral.
 *
 * They must not store:
 *
 * - KType;
 * - KClass;
 * - KSType;
 * - KSDeclaration;
 * - reflection handles;
 * - bytecode parser handles;
 * - source AST/PSI handles;
 * - lambdas;
 * - suppliers;
 * - lazy delegates;
 * - service locators;
 * - callbacks;
 * - registry keys that can recover backend handles.
 *
 * Access law:
 *
 * Invalid ordinal access must fail through a metamodel-domain exception from the
 * concrete table implementation. It must not leak raw JVM array exceptions
 * across the frozen-domain boundary.
 */
interface FrozenTypeShapeTable {
    val schemaVersion: FrozenMetamodelImageSchemaVersion

    val size: Int

    fun containsAt(
        frozenOrdinal: Int,
    ): Boolean

    fun findShapeAt(
        frozenOrdinal: Int,
    ): ResolvedTypeShape?
}
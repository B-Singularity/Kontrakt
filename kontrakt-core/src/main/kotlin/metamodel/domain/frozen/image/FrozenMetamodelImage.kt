package metamodel.domain.frozen.image

import metamodel.domain.frozen.table.FrozenRawFactTable
import metamodel.domain.frozen.table.FrozenTypeCycleIdentityTable
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.frozen.table.FrozenTypeShapeTable
import versioning.coordinate.contract.frozen.image.FrozenMetamodelImageSchemaVersion

/**
 * Immutable adapter-neutral metamodel image.
 *
 * This object is planning-visible semantic material.
 *
 * It deliberately does not expose source-adapter provenance.
 *
 * Provenance is diagnostic material and must not be available to planning
 * providers as an ordinary branch condition.
 *
 * Publication law:
 *
 * A FrozenMetamodelImage is published only after freeze-final integrity
 * validation succeeds.
 *
 * The publication gate validates:
 *
 * - schema version consistency;
 * - table size consistency;
 * - ordinal slot coverage;
 * - shape subject continuity;
 * - cycle identity subject and algorithm continuity;
 * - raw fact identity continuity;
 * - child TypeReference coverage.
 *
 * Authority split:
 *
 * - [typeIndex] owns TypeReference -> frozen ordinal resolution;
 * - [shapeTable] owns shape payload coverage by frozen ordinal;
 * - [cycleIdentityTable] owns cycle identity payload and algorithm metadata by
 *   frozen ordinal;
 * - [rawFactTable] owns raw fact payload coverage by frozen ordinal.
 *
 * This image does not perform provider lookup itself.
 * Planning-facing providers must use the two-step ordinal path:
 *
 * ```text
 * reference -> typeIndex.ordinalOf(reference) -> table.findAt(ordinal)
 * ```
 */
class FrozenMetamodelImage private constructor(
    val imageId: FrozenMetamodelImageId,
    val schemaVersion: FrozenMetamodelImageSchemaVersion,
    val typeIndex: FrozenTypeReferenceIndex,
    val shapeTable: FrozenTypeShapeTable,
    val cycleIdentityTable: FrozenTypeCycleIdentityTable,
    val rawFactTable: FrozenRawFactTable,
) {
    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            typeIndex: FrozenTypeReferenceIndex,
            shapeTable: FrozenTypeShapeTable,
            cycleIdentityTable: FrozenTypeCycleIdentityTable,
            rawFactTable: FrozenRawFactTable,
        ): FrozenMetamodelImage {
            FrozenMetamodelImageIntegrityValidator.requireCompleteCoverage(
                imageId = imageId,
                schemaVersion = schemaVersion,
                typeIndex = typeIndex,
                shapeTable = shapeTable,
                cycleIdentityTable = cycleIdentityTable,
                rawFactTable = rawFactTable,
            )

            return FrozenMetamodelImage(
                imageId = imageId,
                schemaVersion = schemaVersion,
                typeIndex = typeIndex,
                shapeTable = shapeTable,
                cycleIdentityTable = cycleIdentityTable,
                rawFactTable = rawFactTable,
            )
        }
    }
}
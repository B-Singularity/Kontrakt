package metamodel.domain.frozen.image

import metamodel.domain.frozen.table.FrozenRawFactTable
import metamodel.domain.frozen.table.FrozenTypeCycleIdentityTable
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.frozen.table.FrozenTypeShapeTable

/**
 * Immutable adapter-neutral metamodel image.
 *
 * This object is planning-visible semantic material.
 *
 * It deliberately does not expose source-adapter provenance. Provenance is
 * diagnostic material and must not be available to planning providers as an
 * ordinary branch condition.
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
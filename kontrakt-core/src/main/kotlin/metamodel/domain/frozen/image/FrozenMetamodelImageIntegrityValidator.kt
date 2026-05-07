package metamodel.domain.frozen.image

import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelIntegrityViolationException
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.frozen.table.FrozenRawFactTable
import metamodel.domain.frozen.table.FrozenTypeCycleIdentityTable
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.frozen.table.FrozenTypeShapeTable

/**
 * Freeze-final integrity validator.
 *
 * This validator guarantees that a frozen image is not published with a type
 * index/table coverage mismatch or subject-continuity mismatch.
 *
 * Provider reads must not be the first place where incomplete shape coverage is
 * discovered. Publication must fail before the image becomes planning-visible.
 */
internal object FrozenMetamodelImageIntegrityValidator {
    fun requireCompleteCoverage(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        shapeTable: FrozenTypeShapeTable,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        rawFactTable: FrozenRawFactTable,
    ) {
        requireTableSizeMatchesTypeIndex(
            imageId = imageId,
            tableId = FrozenMetamodelImageTableId.SHAPE_TABLE,
            expectedSize = typeIndex.size,
            actualSize = shapeTable.size,
        )

        requireTableSizeMatchesTypeIndex(
            imageId = imageId,
            tableId = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE,
            expectedSize = typeIndex.size,
            actualSize = cycleIdentityTable.size,
        )

        requireTableSizeMatchesTypeIndex(
            imageId = imageId,
            tableId = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
            expectedSize = typeIndex.size,
            actualSize = rawFactTable.size,
        )

        var frozenTypeOrdinal = 0

        while (frozenTypeOrdinal < typeIndex.size) {
            val reference =
                typeIndex.referenceAt(
                    frozenTypeOrdinal = frozenTypeOrdinal,
                )

            val shape =
                shapeTable.findShapeAt(
                    frozenTypeOrdinal = frozenTypeOrdinal,
                ) ?: throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
                    reason = "TypeReference exists in type index but has no shape table coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )

            if (shape.subject != reference) {
                throw FrozenMetamodelIntegrityViolationException(
                    imageId = imageId,
                    reason = "ResolvedTypeShape subject mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                            "expected=${reference.renderSummary()}, actual=${shape.subject.renderSummary()}",
                )
            }

            if (!cycleIdentityTable.containsAt(frozenTypeOrdinal)) {
                throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
                    reason = "TypeReference exists in type index but has no cycle identity table coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )
            }

            if (!rawFactTable.containsAt(frozenTypeOrdinal)) {
                throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                    reason = "TypeReference exists in type index but has no raw fact table coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )
            }

            frozenTypeOrdinal += 1
        }
    }

    private fun requireTableSizeMatchesTypeIndex(
        imageId: FrozenMetamodelImageId,
        tableId: FrozenMetamodelImageTableId,
        expectedSize: Int,
        actualSize: Int,
    ) {
        if (actualSize == expectedSize) {
            return
        }

        throw FrozenMetamodelIncompleteTableException(
            imageId = imageId,
            referenceSummary = "<image-wide>",
            missingTable = tableId.name,
            reason = "Frozen table size does not match type index size: expectedSize=$expectedSize, actualSize=$actualSize.",
        )
    }
}
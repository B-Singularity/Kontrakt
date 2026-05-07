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
 * Publication must fail before the image becomes planning-visible if any table
 * is incomplete or internally inconsistent.
 *
 * This is the boundary that prevents planning providers from discovering
 * freeze bugs on the hot path.
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
                    reason = "Missing shape coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )

            if (shape.subject != reference) {
                throw FrozenMetamodelIntegrityViolationException(
                    imageId = imageId,
                    reason = "ResolvedTypeShape subject mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                            "expected=${reference.renderSummary()}, actual=${shape.subject.renderSummary()}",
                )
            }

            val cycleIdentity =
                cycleIdentityTable.findCycleIdentityAt(
                    frozenTypeOrdinal = frozenTypeOrdinal,
                ) ?: throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
                    reason = "Missing cycle identity coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )

            if (cycleIdentity.subject != reference) {
                throw FrozenMetamodelIntegrityViolationException(
                    imageId = imageId,
                    reason = "Cycle identity subject mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                            "expected=${reference.renderSummary()}, actual=${cycleIdentity.subject.renderSummary()}",
                )
            }

            if (cycleIdentity.identityAlgorithmId != cycleIdentityTable.identityAlgorithmId) {
                throw FrozenMetamodelIntegrityViolationException(
                    imageId = imageId,
                    reason = "Cycle identity algorithm id mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                            "expected=${cycleIdentityTable.identityAlgorithmId}, actual=${cycleIdentity.identityAlgorithmId}",
                )
            }

            if (cycleIdentity.identityAlgorithmVersion != cycleIdentityTable.identityAlgorithmVersion) {
                throw FrozenMetamodelIntegrityViolationException(
                    imageId = imageId,
                    reason = "Cycle identity algorithm version mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                            "expected=${cycleIdentityTable.identityAlgorithmVersion}, actual=${cycleIdentity.identityAlgorithmVersion}",
                )
            }

            if (!rawFactTable.containsAt(frozenTypeOrdinal)) {
                throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                    reason = "Missing raw fact coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
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
package metamodel.domain.frozen.image

import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.frozen.table.FrozenRawFactTable
import metamodel.domain.frozen.table.FrozenTypeCycleIdentityTable
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.frozen.table.FrozenTypeShapeTable

/**
 * Freeze-final integrity validator.
 *
 * This validator guarantees that a frozen image is not published with a type
 * index/table coverage mismatch.
 *
 * Coverage law:
 *
 * - the type index is the coverage authority;
 * - every indexed TypeReference must have explicit shape coverage;
 * - every indexed TypeReference must have explicit cycle identity coverage;
 * - every indexed TypeReference must have explicit raw fact coverage.
 *
 * Raw fact coverage does not necessarily mean eager RawTypeFactsDTO
 * materialization.
 *
 * It may be:
 *
 * - materialized DTO;
 * - frozen raw fact record;
 * - TRUNCATED sentinel record;
 * - FILTERED_BY_POLICY sentinel record;
 * - UNAVAILABLE_FROM_BACKEND sentinel record;
 * - ACQUISITION_FAILED diagnostic record.
 */
internal object FrozenMetamodelImageIntegrityValidator {
    fun requireCompleteCoverage(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        shapeTable: FrozenTypeShapeTable,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        rawFactTable: FrozenRawFactTable,
    ) {
        var ordinal = 0

        while (ordinal < typeIndex.size) {
            val reference =
                typeIndex.referenceAt(
                    frozenOrdinal = ordinal,
                )

            if (!shapeTable.contains(reference)) {
                throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
                    reason = "TypeReference exists in type index but has no shape table coverage.",
                )
            }

            if (!cycleIdentityTable.contains(reference)) {
                throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
                    reason = "TypeReference exists in type index but has no cycle identity table coverage.",
                )
            }

            if (!rawFactTable.contains(reference)) {
                throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                    reason = "TypeReference exists in type index but has no raw fact table coverage.",
                )
            }

            ordinal += 1
        }
    }
}
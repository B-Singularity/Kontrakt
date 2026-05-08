package metamodel.domain.frozen.image

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.exception.FrozenMetamodelIncompleteTableException
import metamodel.domain.exception.FrozenMetamodelIntegrityViolationException
import metamodel.domain.frozen.table.FrozenMetamodelImageTableId
import metamodel.domain.frozen.table.FrozenRawFactTable
import metamodel.domain.frozen.table.FrozenTypeCycleIdentityTable
import metamodel.domain.frozen.table.FrozenTypeReferenceIndex
import metamodel.domain.frozen.table.FrozenTypeShapeTable
import metamodel.domain.vo.TypeReference
import planning.domain.expansion.TypeCycleIdentity

/**
 * Freeze-final integrity validator.
 *
 * This validator is the publication gate for FrozenMetamodelImage.
 *
 * It validates:
 *
 * - schema version consistency;
 * - table size consistency;
 * - ordinal slot coverage;
 * - shape subject continuity;
 * - shape child-reference coverage;
 * - cycle identity subject continuity;
 * - cycle identity algorithm continuity;
 * - raw fact identity continuity;
 * - raw fact child-reference coverage.
 *
 * It does not validate diagnostic provenance because provenance is not
 * planning-visible semantic material.
 */
internal object FrozenMetamodelImageIntegrityValidator {
    fun requireCompleteCoverage(
        imageId: FrozenMetamodelImageId,
        schemaVersion: FrozenMetamodelImageSchemaVersion,
        typeIndex: FrozenTypeReferenceIndex,
        shapeTable: FrozenTypeShapeTable,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        rawFactTable: FrozenRawFactTable,
    ) {
        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableName = FrozenMetamodelImageTableId.TYPE_INDEX.name,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = typeIndex.schemaVersion,
        )

        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableName = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = shapeTable.schemaVersion,
        )

        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableName = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = cycleIdentityTable.schemaVersion,
        )

        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableName = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = rawFactTable.schemaVersion,
        )

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

            requireShapeContinuity(
                imageId = imageId,
                typeIndex = typeIndex,
                shapeTable = shapeTable,
                cycleIdentityTable = cycleIdentityTable,
                rawFactTable = rawFactTable,
                frozenTypeOrdinal = frozenTypeOrdinal,
                expectedReference = reference,
                shape = shape,
            )

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

            val rawFacts =
                rawFactTable.findFactsAt(
                    frozenTypeOrdinal = frozenTypeOrdinal,
                ) ?: throw FrozenMetamodelIncompleteTableException(
                    imageId = imageId,
                    referenceSummary = reference.renderSummary(),
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                    reason = "Missing raw fact coverage at frozenTypeOrdinal=$frozenTypeOrdinal.",
                )

            requireRawFactContinuity(
                imageId = imageId,
                typeIndex = typeIndex,
                shapeTable = shapeTable,
                cycleIdentityTable = cycleIdentityTable,
                rawFactTable = rawFactTable,
                frozenTypeOrdinal = frozenTypeOrdinal,
                expectedReference = reference,
                cycleIdentity = cycleIdentity,
                rawFacts = rawFacts,
            )

            frozenTypeOrdinal += 1
        }
    }

    private fun requireShapeContinuity(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        shapeTable: FrozenTypeShapeTable,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        rawFactTable: FrozenRawFactTable,
        frozenTypeOrdinal: Int,
        expectedReference: TypeReference,
        shape: ResolvedTypeShape,
    ) {
        if (shape.subject != expectedReference) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "ResolvedTypeShape subject mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${expectedReference.renderSummary()}, actual=${shape.subject.renderSummary()}",
            )
        }

        requireReferencedTypeHasCompleteFrozenSlot(
            imageId = imageId,
            owner = expectedReference,
            fieldName = "ResolvedTypeShape.elementType",
            referenced = shape.elementType,
            typeIndex = typeIndex,
            shapeTable = shapeTable,
            cycleIdentityTable = cycleIdentityTable,
            rawFactTable = rawFactTable,
        )

        requireReferencedTypeHasCompleteFrozenSlot(
            imageId = imageId,
            owner = expectedReference,
            fieldName = "ResolvedTypeShape.keyType",
            referenced = shape.keyType,
            typeIndex = typeIndex,
            shapeTable = shapeTable,
            cycleIdentityTable = cycleIdentityTable,
            rawFactTable = rawFactTable,
        )

        requireReferencedTypeHasCompleteFrozenSlot(
            imageId = imageId,
            owner = expectedReference,
            fieldName = "ResolvedTypeShape.valueType",
            referenced = shape.valueType,
            typeIndex = typeIndex,
            shapeTable = shapeTable,
            cycleIdentityTable = cycleIdentityTable,
            rawFactTable = rawFactTable,
        )

        requireReferencedTypeHasCompleteFrozenSlot(
            imageId = imageId,
            owner = expectedReference,
            fieldName = "ResolvedTypeShape.componentType",
            referenced = shape.componentType,
            typeIndex = typeIndex,
            shapeTable = shapeTable,
            cycleIdentityTable = cycleIdentityTable,
            rawFactTable = rawFactTable,
        )
    }

    /**
     * Validates that frozen raw facts belong to the same cycle-identity subject
     * already validated for the same frozen type ordinal.
     *
     * Authority split:
     *
     * - TypeReference / TypeCycleKey is canonical type identity material.
     * - TypeCycleIdentity owns the primitive active-cycle routing identity.
     * - RawTypeFactsDTO carries the lowered raw-fact identity emitted by the
     *   metamodel acquisition/freeze path.
     *
     * This validator must therefore compare RawTypeFactsDTO identity material
     * against TypeCycleIdentity, not against TypeCycleKey.
     *
     * Rationale:
     *
     * TypeCycleKey intentionally does not expose identityBits64. It is a
     * structural key value object, not the primitive cycle-routing authority.
     * The same law is used by TypeExpansionPipeline after active-cycle miss:
     *
     * ```text
     * rawFacts.typeIdentity64 == preflight.cycleIdentity.identityBits64
     * rawFacts.typeIdentityAlgorithmId == preflight.cycleIdentity.identityAlgorithmId
     * rawFacts.typeIdentityAlgorithmVersion == preflight.cycleIdentity.identityAlgorithmVersion
     * ```
     */
    private fun requireRawFactContinuity(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        shapeTable: FrozenTypeShapeTable,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        rawFactTable: FrozenRawFactTable,
        frozenTypeOrdinal: Int,
        expectedReference: TypeReference,
        cycleIdentity: TypeCycleIdentity,
        rawFacts: RawTypeFactsDTO,
    ) {
        if (rawFacts.typeIdentity64 != cycleIdentity.identityBits64) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "RawTypeFactsDTO identity mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expectedCycleIdentityBits64=${cycleIdentity.identityBits64}, " +
                        "actualTypeIdentity64=${rawFacts.typeIdentity64}, " +
                        "subject=${expectedReference.renderSummary()}",
            )
        }

        if (rawFacts.typeIdentityAlgorithmId != cycleIdentity.identityAlgorithmId) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "RawTypeFactsDTO identity algorithm id mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${cycleIdentity.identityAlgorithmId}, " +
                        "actual=${rawFacts.typeIdentityAlgorithmId}, " +
                        "subject=${expectedReference.renderSummary()}",
            )
        }

        if (rawFacts.typeIdentityAlgorithmVersion != cycleIdentity.identityAlgorithmVersion) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "RawTypeFactsDTO identity algorithm version mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${cycleIdentity.identityAlgorithmVersion}, " +
                        "actual=${rawFacts.typeIdentityAlgorithmVersion}, " +
                        "subject=${expectedReference.renderSummary()}",
            )
        }

        var constructorIndex = 0
        while (constructorIndex < rawFacts.constructors.size) {
            val constructor = rawFacts.constructors[constructorIndex]

            var parameterIndex = 0
            while (parameterIndex < constructor.parameters.size) {
                val parameter = constructor.parameters[parameterIndex]

                requireReferencedTypeHasCompleteFrozenSlot(
                    imageId = imageId,
                    owner = expectedReference,
                    fieldName = "constructor[$constructorIndex].parameter[$parameterIndex].typeReference",
                    referenced = parameter.typeReference,
                    typeIndex = typeIndex,
                    shapeTable = shapeTable,
                    cycleIdentityTable = cycleIdentityTable,
                    rawFactTable = rawFactTable,
                )

                parameterIndex += 1
            }

            constructorIndex += 1
        }

        var propertyIndex = 0
        while (propertyIndex < rawFacts.properties.size) {
            val property = rawFacts.properties[propertyIndex]

            requireReferencedTypeHasCompleteFrozenSlot(
                imageId = imageId,
                owner = expectedReference,
                fieldName = "property[$propertyIndex].typeReference",
                referenced = property.typeReference,
                typeIndex = typeIndex,
                shapeTable = shapeTable,
                cycleIdentityTable = cycleIdentityTable,
                rawFactTable = rawFactTable,
            )

            propertyIndex += 1
        }
    }

    /**
     * Validates that a TypeReference reached through frozen shape/raw-fact
     * material is not merely present in the type index, but also has a complete
     * frozen table slot.
     *
     * This check intentionally stops at one edge.
     *
     * It does not recursively validate the referenced type's own children.
     * Recursive graph validation is unnecessary because requireCompleteCoverage
     * already performs a deterministic full-image ordinal sweep:
     *
     * ```text
     * for frozenTypeOrdinal in 0 until typeIndex.size:
     *     validate slot(frozenTypeOrdinal)
     * ```
     *
     * Therefore, this helper provides immediate, local diagnostic precision for
     * outbound references while the outer sweep provides full graph coverage.
     *
     * Performance note:
     *
     * This helper performs one TypeReference -> frozenTypeOrdinal lookup for
     * each referenced type. That is acceptable at the freeze publication boundary
     * because this validator is a heavy bridge, not a planning hot path.
     *
     * Table checks after ordinal resolution are primitive ordinal reads and
     * should be cheap for object-array/slab-backed frozen tables.
     */
    private fun requireReferencedTypeHasCompleteFrozenSlot(
        imageId: FrozenMetamodelImageId,
        owner: TypeReference,
        fieldName: String,
        referenced: TypeReference?,
        typeIndex: FrozenTypeReferenceIndex,
        shapeTable: FrozenTypeShapeTable,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        rawFactTable: FrozenRawFactTable,
    ) {
        if (referenced == null) {
            return
        }

        val referencedFrozenTypeOrdinal = typeIndex.ordinalOf(referenced)

        if (referencedFrozenTypeOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "Frozen image contains a reference to a TypeReference outside the type index: " +
                        "owner=${owner.renderSummary()}, field=$fieldName, referenced=${referenced.renderSummary()}",
            )
        }

        if (!shapeTable.containsAt(referencedFrozenTypeOrdinal)) {
            throw FrozenMetamodelIncompleteTableException(
                imageId = imageId,
                referenceSummary = referenced.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE.name,
                reason = "Referenced TypeReference has no shape coverage: " +
                        "owner=${owner.renderSummary()}, field=$fieldName, " +
                        "referencedFrozenTypeOrdinal=$referencedFrozenTypeOrdinal.",
            )
        }

        if (!cycleIdentityTable.containsAt(referencedFrozenTypeOrdinal)) {
            throw FrozenMetamodelIncompleteTableException(
                imageId = imageId,
                referenceSummary = referenced.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE.name,
                reason = "Referenced TypeReference has no cycle identity coverage: " +
                        "owner=${owner.renderSummary()}, field=$fieldName, " +
                        "referencedFrozenTypeOrdinal=$referencedFrozenTypeOrdinal.",
            )
        }

        if (!rawFactTable.containsAt(referencedFrozenTypeOrdinal)) {
            throw FrozenMetamodelIncompleteTableException(
                imageId = imageId,
                referenceSummary = referenced.renderSummary(),
                missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
                reason = "Referenced TypeReference has no raw fact coverage: " +
                        "owner=${owner.renderSummary()}, field=$fieldName, " +
                        "referencedFrozenTypeOrdinal=$referencedFrozenTypeOrdinal.",
            )
        }
    }

    private fun requireSchemaVersionMatchesImage(
        imageId: FrozenMetamodelImageId,
        tableName: String,
        imageSchemaVersion: FrozenMetamodelImageSchemaVersion,
        tableSchemaVersion: FrozenMetamodelImageSchemaVersion,
    ) {
        if (tableSchemaVersion == imageSchemaVersion) {
            return
        }

        throw FrozenMetamodelIntegrityViolationException(
            imageId = imageId,
            reason = "Frozen table schema version mismatch: table=$tableName, " +
                    "imageSchemaVersion=${imageSchemaVersion.renderSummary()}, " +
                    "tableSchemaVersion=${tableSchemaVersion.renderSummary()}",
        )
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
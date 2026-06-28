package stage.canonicalization.material.frozen.image

import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId
import stage.canonicalization.material.frozen.table.FrozenRawFactTable
import stage.canonicalization.material.frozen.table.FrozenTypeCycleIdentityTable
import stage.canonicalization.material.frozen.table.FrozenTypeReferenceIndex
import stage.canonicalization.material.frozen.table.FrozenTypeShapeTable
import stage.lowering.material.expansion.TypeCycleIdentity
import stage.canonicalization.material.TypeReference
import stage.input.diagnostics.FrozenMetamodelIncompleteTableException
import stage.input.diagnostics.FrozenMetamodelIntegrityViolationException
import stage.input.material.RawTypeFactsDTO
import stage.input.material.ResolvedTypeShape
import versioning.coordinate.contract.frozen.image.FrozenMetamodelImageSchemaVersion

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
 *
 * Authority split:
 *
 * - FrozenTypeReferenceIndex owns TypeReference -> frozen ordinal resolution.
 * - FrozenTypeShapeTable owns shape payload coverage by frozen ordinal.
 * - FrozenTypeCycleIdentityTable owns cycle identity payload coverage and
 *   table-wide identity algorithm metadata.
 * - FrozenRawFactTable owns raw fact payload coverage by frozen ordinal.
 *
 * This validator does not perform backend discovery.
 * It does not reopen backend handles.
 * It does not normalize polluted TypeReference or signature material.
 *
 * If any mismatch is found, publication fails closed.
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
            tableId = FrozenMetamodelImageTableId.TYPE_INDEX,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = typeIndex.schemaVersion,
        )

        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableId = FrozenMetamodelImageTableId.SHAPE_TABLE,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = shapeTable.schemaVersion,
        )

        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableId = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE,
            imageSchemaVersion = schemaVersion,
            tableSchemaVersion = cycleIdentityTable.schemaVersion,
        )

        requireSchemaVersionMatchesImage(
            imageId = imageId,
            tableId = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
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
                    frozenOrdinal = frozenTypeOrdinal,
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

            requireCycleIdentityContinuity(
                imageId = imageId,
                cycleIdentityTable = cycleIdentityTable,
                frozenTypeOrdinal = frozenTypeOrdinal,
                expectedReference = reference,
                cycleIdentity = cycleIdentity,
            )

            val rawFacts =
                rawFactTable.findFactsAt(
                    frozenOrdinal = frozenTypeOrdinal,
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
     * Validates that the cycle identity stored at an ordinal belongs to the same
     * TypeReference exposed by the type index and that it uses the table-owned
     * identity algorithm metadata.
     *
     * Algorithm metadata authority belongs to FrozenTypeCycleIdentityTable.
     * Providers must derive their exposed algorithm metadata from the table, not
     * from separate caller-supplied fields.
     */
    private fun requireCycleIdentityContinuity(
        imageId: FrozenMetamodelImageId,
        cycleIdentityTable: FrozenTypeCycleIdentityTable,
        frozenTypeOrdinal: Int,
        expectedReference: TypeReference,
        cycleIdentity: TypeCycleIdentity,
    ) {
        if (cycleIdentity.subject != expectedReference) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "Cycle identity subject mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${expectedReference.renderSummary()}, actual=${cycleIdentity.subject.renderSummary()}",
            )
        }

        if (cycleIdentity.identityAlgorithmId != cycleIdentityTable.identityAlgorithmId) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "Cycle identity algorithm id mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${cycleIdentityTable.identityAlgorithmId}, actual=${cycleIdentity.identityAlgorithmId}, " +
                        "subject=${expectedReference.renderSummary()}",
            )
        }

        if (cycleIdentity.identityAlgorithmVersion != cycleIdentityTable.identityAlgorithmVersion) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "Cycle identity algorithm version mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${cycleIdentityTable.identityAlgorithmVersion}, " +
                        "actual=${cycleIdentity.identityAlgorithmVersion}, " +
                        "subject=${expectedReference.renderSummary()}",
            )
        }
    }

    /**
     * Validates that frozen raw facts belong to the same cycle-identity subject
     * already validated for the same frozen type ordinal.
     *
     * Authority split:
     *
     * - TypeReference / TypeCycleKey is canonical type identity material.
     * - TypeCycleIdentity owns the primitive active-cycle routing identity.
     * - FrozenTypeCycleIdentityTable owns the table-wide cycle identity
     *   algorithm metadata.
     * - RawTypeFactsDTO carries the lowered raw-fact identity emitted by the
     *   metamodel acquisition/freeze path.
     *
     * This validator must compare RawTypeFactsDTO identity material against
     * TypeCycleIdentity bits and table-owned algorithm metadata.
     *
     * It must not compare against TypeCycleKey.hashCode().
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

        if (rawFacts.typeIdentityAlgorithmId != cycleIdentityTable.identityAlgorithmId) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "RawTypeFactsDTO identity algorithm id mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${cycleIdentityTable.identityAlgorithmId}, " +
                        "actual=${rawFacts.typeIdentityAlgorithmId}, " +
                        "subject=${expectedReference.renderSummary()}",
            )
        }

        if (rawFacts.typeIdentityAlgorithmVersion != cycleIdentityTable.identityAlgorithmVersion) {
            throw FrozenMetamodelIntegrityViolationException(
                imageId = imageId,
                reason = "RawTypeFactsDTO identity algorithm version mismatch at frozenTypeOrdinal=$frozenTypeOrdinal: " +
                        "expected=${cycleIdentityTable.identityAlgorithmVersion}, " +
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
     * Recursive graph validation is unnecessary because requireCompleteCoverage
     * already performs a deterministic full-image ordinal sweep:
     *
     * ```text
     * for frozenTypeOrdinal in 0 until typeIndex.size:
     *     validate slot(frozenTypeOrdinal)
     * ```
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

        val referencedFrozenTypeOrdinal =
            typeIndex.ordinalOf(
                reference = referenced,
            )

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
        tableId: FrozenMetamodelImageTableId,
        imageSchemaVersion: FrozenMetamodelImageSchemaVersion,
        tableSchemaVersion: FrozenMetamodelImageSchemaVersion,
    ) {
        if (tableSchemaVersion == imageSchemaVersion) {
            return
        }

        throw FrozenMetamodelIntegrityViolationException(
            imageId = imageId,
            reason = "Frozen table schema version mismatch: table=${tableId.name}, " +
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
            reason = "Frozen table size does not match type index size: " +
                    "expectedSize=$expectedSize, actualSize=$actualSize.",
        )
    }
}
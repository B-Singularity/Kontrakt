package stage.canonicalization.material.frozen.image

import stage.admission.diagnostics.evidence.FrozenMetamodelIntegrityViolationException
import stage.admission.diagnostics.evidence.FrozenMetamodelSequenceViolationException
import stage.canonicalization.material.TypeReference
import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId
import stage.canonicalization.material.frozen.table.FrozenTypeReferenceIndex
import stage.canonicalization.material.frozen.table.ObjectArrayFrozenRawFactTable
import stage.canonicalization.material.frozen.table.ObjectArrayFrozenTypeCycleIdentityTable
import stage.canonicalization.material.frozen.table.ObjectArrayFrozenTypeReferenceIndex
import stage.canonicalization.material.frozen.table.ObjectArrayFrozenTypeShapeTable
import stage.input.presentation.dto.RawTypeFactsDTO
import stage.input.presentation.raw.ResolvedTypeShape
import stage.lowering.material.expansion.TypeCycleIdentity

/**
 * Domain-owned assembler for publishing a FrozenMetamodelImage from
 * adapter-neutral frozen material.
 *
 * This service is the bridge between acquisition/freeze output and the
 * published frozen image.
 *
 * It is not:
 *
 * - an adapter;
 * - a reflection/KSP/bytecode/source reader;
 * - a planning provider;
 * - a diagnostic envelope builder;
 * - a cache key builder.
 *
 * Single-entrypoint law:
 *
 * This assembler accepts only FrozenMetamodelImageAssemblyInput.
 *
 * It deliberately does not expose a long-parameter issue(...) overload.
 *
 * Reason:
 *
 * Frozen image publication is an atomic command, not a loose collection of
 * parallel arrays. Keeping a long-parameter overload would re-open the same
 * fragmented assembly boundary that FrozenMetamodelImageAssemblyInput exists to
 * close.
 *
 * Callers must first issue FrozenMetamodelImageAssemblyInput, which owns:
 *
 * - caller-array snapshotting;
 * - correlated batch grouping;
 * - local entry-count sanity checks;
 * - assembly input diagnostics.
 *
 * Why this exists:
 *
 * Object-array tables are ordinal-addressed.
 *
 * The adapter/acquisition layer may produce material in any order. That order
 * must never become frozen semantic order.
 *
 * Therefore publication must follow this sequence:
 *
 * ```text
 * FrozenMetamodelImageAssemblyInput
 * -> ObjectArrayFrozenTypeReferenceIndex.issue(...)
 * -> deterministic frozen ordinals
 * -> align shape/cycle/raw entries by ordinal
 * -> object-array frozen tables
 * -> FrozenMetamodelImage.issue(...)
 * -> integrity validator
 * ```
 *
 * Assembly boundary law:
 *
 * This assembler is the only domain authority that aligns adapter-neutral
 * `(TypeReference, payload)` entries to image-local frozen ordinals.
 *
 * Adapter/infrastructure code may collect entries, but it must not assign
 * frozen ordinals, sort table arrays, or rely on backend discovery order as
 * frozen table order.
 *
 * Ordinal alignment law:
 *
 * Table payload arrays must be aligned to the frozen type index, not to adapter
 * discovery order.
 *
 * The assembler is the only component in this cut that turns:
 *
 * ```text
 * (TypeReference, payload)
 * ```
 *
 * into:
 *
 * ```text
 * tablePayload[frozenOrdinal]
 * ```
 *
 * Input law:
 *
 * The input command owns caller-array snapshotting and exposes fresh copies to
 * this assembler.
 *
 * This assembler consumes those copies in a single pass and does not repeatedly
 * request copy arrays.
 *
 * Payload immutability law:
 *
 * Payload objects are not deep-copied here.
 *
 * ResolvedTypeShape, TypeCycleIdentity, and RawTypeFactsDTO must already be
 * immutable adapter-neutral frozen material before entering this assembler.
 *
 * If payloads can mutate after image publication, the broken boundary is the
 * payload issuer or upstream freeze lowering step, not this ordinal alignment
 * service.
 *
 * Capacity law:
 *
 * This assembler allocates Level 1 object-array tables sized by the published
 * FrozenTypeReferenceIndex.
 *
 * It does not own global resource governance.
 *
 * Excessively large TypeReference sets must be rejected by acquisition/session
 * capacity policy before this assembler is called.
 *
 * Missing coverage law:
 *
 * This assembler does not require every indexed TypeReference to have every
 * payload entry before constructing table arrays.
 *
 * Missing slots remain null and are rejected by FrozenMetamodelImage.issue(...)
 * through FrozenMetamodelImageIntegrityValidator.
 *
 * Reason:
 *
 * Keeping the complete-coverage decision in the image publication gate keeps
 * all table consistency errors centralized in one validator.
 *
 * Duplicate entry law:
 *
 * Multiple payload entries for the same frozen ordinal fail closed.
 *
 * The assembler must never use first-wins, last-wins, merge, or coalescing
 * behavior.
 *
 * Unknown reference law:
 *
 * A payload entry whose TypeReference is absent from the type index fails
 * closed.
 *
 * The assembler must not widen the type index after it has been issued.
 *
 * Backend-erasure law:
 *
 * All inputs must already be adapter-neutral frozen material.
 *
 * This assembler must not:
 *
 * - read backend handles;
 * - derive TypeReference from backend APIs;
 * - perform reflection/KSP/bytecode/source discovery;
 * - recover diagnostic provenance;
 * - branch on source adapter provenance.
 */
internal object FrozenMetamodelImageAssembler {
    @JvmStatic
    fun issue(
        input: FrozenMetamodelImageAssemblyInput,
    ): FrozenMetamodelImage {
        val typeReferences =
            input.copyTypeReferences()

        val shapeEntries =
            input.copyShapeEntries()

        val cycleIdentityEntries =
            input.copyCycleIdentityEntries()

        val rawFactEntries =
            input.copyRawFactEntries()

        val typeIndex =
            ObjectArrayFrozenTypeReferenceIndex.issue(
                imageId = input.imageId,
                schemaVersion = input.schemaVersion,
                references = typeReferences,
            )

        val shapeTable =
            ObjectArrayFrozenTypeShapeTable.issue(
                imageId = input.imageId,
                schemaVersion = input.schemaVersion,
                shapes = alignShapeEntries(
                    imageId = input.imageId,
                    typeIndex = typeIndex,
                    entries = shapeEntries,
                ),
            )

        val cycleIdentityTable =
            ObjectArrayFrozenTypeCycleIdentityTable.issue(
                imageId = input.imageId,
                schemaVersion = input.schemaVersion,
                identityAlgorithmId = input.cycleIdentityAlgorithmId,
                identityAlgorithmVersion = input.cycleIdentityAlgorithmVersion,
                identities = alignCycleIdentityEntries(
                    imageId = input.imageId,
                    typeIndex = typeIndex,
                    entries = cycleIdentityEntries,
                ),
            )

        val rawFactTable =
            ObjectArrayFrozenRawFactTable.issue(
                imageId = input.imageId,
                schemaVersion = input.schemaVersion,
                facts = alignRawFactEntries(
                    imageId = input.imageId,
                    typeIndex = typeIndex,
                    entries = rawFactEntries,
                ),
            )

        return FrozenMetamodelImage.issue(
            imageId = input.imageId,
            schemaVersion = input.schemaVersion,
            typeIndex = typeIndex,
            shapeTable = shapeTable,
            cycleIdentityTable = cycleIdentityTable,
            rawFactTable = rawFactTable,
        )
    }

    private fun alignShapeEntries(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        entries: Array<FrozenTypeShapeTableEntry>,
    ): Array<ResolvedTypeShape?> {
        val aligned =
            arrayOfNulls<ResolvedTypeShape>(
                typeIndex.size,
            )

        var entryIndex = 0

        while (entryIndex < entries.size) {
            val entry = entries[entryIndex]

            val frozenOrdinal =
                requireEntryReferenceIsIndexed(
                    imageId = imageId,
                    tableId = FrozenMetamodelImageTableId.SHAPE_TABLE,
                    entryIndex = entryIndex,
                    typeIndex = typeIndex,
                    reference = entry.reference,
                )

            if (aligned[frozenOrdinal] != null) {
                throwDuplicateEntry(
                    imageId = imageId,
                    tableId = FrozenMetamodelImageTableId.SHAPE_TABLE,
                    reference = entry.reference,
                    frozenOrdinal = frozenOrdinal,
                    entryIndex = entryIndex,
                )
            }

            aligned[frozenOrdinal] = entry.shape
            entryIndex += 1
        }

        return aligned
    }

    private fun alignCycleIdentityEntries(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        entries: Array<FrozenTypeCycleIdentityTableEntry>,
    ): Array<TypeCycleIdentity?> {
        val aligned =
            arrayOfNulls<TypeCycleIdentity>(
                typeIndex.size,
            )

        var entryIndex = 0

        while (entryIndex < entries.size) {
            val entry = entries[entryIndex]

            val frozenOrdinal =
                requireEntryReferenceIsIndexed(
                    imageId = imageId,
                    tableId = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE,
                    entryIndex = entryIndex,
                    typeIndex = typeIndex,
                    reference = entry.reference,
                )

            if (aligned[frozenOrdinal] != null) {
                throwDuplicateEntry(
                    imageId = imageId,
                    tableId = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE,
                    reference = entry.reference,
                    frozenOrdinal = frozenOrdinal,
                    entryIndex = entryIndex,
                )
            }

            aligned[frozenOrdinal] = entry.identity
            entryIndex += 1
        }

        return aligned
    }

    private fun alignRawFactEntries(
        imageId: FrozenMetamodelImageId,
        typeIndex: FrozenTypeReferenceIndex,
        entries: Array<FrozenRawFactTableEntry>,
    ): Array<RawTypeFactsDTO?> {
        val aligned =
            arrayOfNulls<RawTypeFactsDTO>(
                typeIndex.size,
            )

        var entryIndex = 0

        while (entryIndex < entries.size) {
            val entry = entries[entryIndex]

            val frozenOrdinal =
                requireEntryReferenceIsIndexed(
                    imageId = imageId,
                    tableId = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
                    entryIndex = entryIndex,
                    typeIndex = typeIndex,
                    reference = entry.reference,
                )

            if (aligned[frozenOrdinal] != null) {
                throwDuplicateEntry(
                    imageId = imageId,
                    tableId = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
                    reference = entry.reference,
                    frozenOrdinal = frozenOrdinal,
                    entryIndex = entryIndex,
                )
            }

            aligned[frozenOrdinal] = entry.facts
            entryIndex += 1
        }

        return aligned
    }

    private fun requireEntryReferenceIsIndexed(
        imageId: FrozenMetamodelImageId,
        tableId: FrozenMetamodelImageTableId,
        entryIndex: Int,
        typeIndex: FrozenTypeReferenceIndex,
        reference: TypeReference,
    ): Int {
        val frozenOrdinal =
            typeIndex.ordinalOf(
                reference = reference,
            )

        if (frozenOrdinal != FrozenTypeReferenceIndex.MISSING_ORDINAL) {
            return frozenOrdinal
        }

        throw FrozenMetamodelIntegrityViolationException(
            imageId = imageId,
            reason = "Frozen table entry references a TypeReference outside the type index: " +
                    "table=${tableId.name}, " +
                    "entryIndex=$entryIndex, " +
                    "reference=${reference.renderSummary()}",
        )
    }

    private fun throwDuplicateEntry(
        imageId: FrozenMetamodelImageId,
        tableId: FrozenMetamodelImageTableId,
        reference: TypeReference,
        frozenOrdinal: Int,
        entryIndex: Int,
    ): Nothing {
        throw FrozenMetamodelSequenceViolationException(
            imageId = imageId,
            sequenceTable = tableId.name,
            referenceSummary = reference.renderSummary(),
            reason = "Duplicate frozen table entry for the same TypeReference: " +
                    "table=${tableId.name}, " +
                    "frozenOrdinal=$frozenOrdinal, " +
                    "entryIndex=$entryIndex",
        )
    }
}

/**
 * Shape payload entry before frozen ordinal alignment.
 *
 * Assembly-only entry law:
 *
 * This entry is transient frozen-image assembly material.
 *
 * It must not be used by planning providers, planning domain services, cache
 * keys, or runtime traversal logic.
 *
 * It exists only to carry adapter-neutral `(TypeReference, payload)` material
 * into FrozenMetamodelImageAssembler before image-local ordinal alignment.
 *
 * Payload law:
 *
 * [shape] must already be immutable adapter-neutral frozen material.
 *
 * This entry does not clone or sanitize the payload.
 *
 * Backend-erasure law:
 *
 * This object must not contain backend handles, adapter-local ids, acquisition
 * slots, or source adapter provenance.
 */
internal class FrozenTypeShapeTableEntry private constructor(
    val reference: TypeReference,
    val shape: ResolvedTypeShape,
) {
    companion object {
        @JvmStatic
        fun issue(
            reference: TypeReference,
            shape: ResolvedTypeShape,
        ): FrozenTypeShapeTableEntry {
            return FrozenTypeShapeTableEntry(
                reference = reference,
                shape = shape,
            )
        }
    }
}

/**
 * Cycle identity payload entry before frozen ordinal alignment.
 *
 * Assembly-only entry law:
 *
 * This entry is transient frozen-image assembly material.
 *
 * It must not be used by planning providers, planning domain services, cache
 * keys, or runtime traversal logic.
 *
 * It exists only to carry adapter-neutral `(TypeReference, payload)` material
 * into FrozenMetamodelImageAssembler before image-local ordinal alignment.
 *
 * Algorithm authority law:
 *
 * The cycle identity table owns algorithm metadata validation.
 *
 * This entry only pairs the intended TypeReference with the already-frozen
 * TypeCycleIdentity payload.
 *
 * Payload law:
 *
 * [identity] must already be immutable adapter-neutral frozen material.
 *
 * This entry does not clone or sanitize the payload.
 */
internal class FrozenTypeCycleIdentityTableEntry private constructor(
    val reference: TypeReference,
    val identity: TypeCycleIdentity,
) {
    companion object {
        @JvmStatic
        fun issue(
            reference: TypeReference,
            identity: TypeCycleIdentity,
        ): FrozenTypeCycleIdentityTableEntry {
            return FrozenTypeCycleIdentityTableEntry(
                reference = reference,
                identity = identity,
            )
        }
    }
}

/**
 * Raw fact payload entry before frozen ordinal alignment.
 *
 * Assembly-only entry law:
 *
 * This entry is transient frozen-image assembly material.
 *
 * It must not be used by planning providers, planning domain services, cache
 * keys, or runtime traversal logic.
 *
 * It exists only to carry adapter-neutral `(TypeReference, payload)` material
 * into FrozenMetamodelImageAssembler before image-local ordinal alignment.
 *
 * Raw fact publication path law:
 *
 * This entry currently carries materialized RawTypeFactsDTO.
 *
 * If the acquisition path produces FrozenRawFactRecord, that record must be
 * materialized through its frozen adapter-neutral materialization path before
 * entering this DTO-backed table, or this entry type must be revised to carry
 * FrozenRawFactRecord explicitly.
 *
 * Do not maintain competing raw fact publication authorities.
 *
 * Payload law:
 *
 * [facts] must already be immutable adapter-neutral frozen material.
 *
 * This entry does not clone or sanitize the payload.
 */
internal class FrozenRawFactTableEntry private constructor(
    val reference: TypeReference,
    val facts: RawTypeFactsDTO,
) {
    companion object {
        @JvmStatic
        fun issue(
            reference: TypeReference,
            facts: RawTypeFactsDTO,
        ): FrozenRawFactTableEntry {
            return FrozenRawFactTableEntry(
                reference = reference,
                facts = facts,
            )
        }
    }
}
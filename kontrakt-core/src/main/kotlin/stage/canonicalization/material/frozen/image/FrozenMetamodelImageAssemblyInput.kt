package stage.canonicalization.material.frozen.image

import stage.canonicalization.material.frozen.table.FrozenMetamodelImageTableId
import stage.canonicalization.material.TypeReference
import stage.input.diagnostics.FrozenMetamodelSequenceViolationException
import versioning.coordinate.contract.frozen.image.FrozenMetamodelImageSchemaVersion

/**
 * Immutable command object for frozen metamodel image assembly.
 *
 * This object groups all adapter-neutral material required to publish one
 * FrozenMetamodelImage.
 *
 * It is assembly input, not planning-visible semantic material.
 *
 * Why this exists:
 *
 * Frozen image publication needs several correlated inputs:
 *
 * - TypeReference set;
 * - shape entries;
 * - cycle identity entries;
 * - raw fact entries;
 * - cycle identity algorithm metadata.
 *
 * Passing them as independent parameters makes it too easy for callers to mix
 * material from different acquisition batches.
 *
 * This input object creates a single atomic assembly command:
 *
 * ```text
 * one image id
 * one schema version
 * one type reference set
 * one shape entry set
 * one cycle identity entry set
 * one raw fact entry set
 * one cycle identity algorithm declaration
 * ```
 *
 * Snapshot law:
 *
 * JVM arrays are mutable.
 *
 * The issue(...) factory defensively snapshots every caller-owned array.
 *
 * The stored arrays are private and are never returned directly. Consumers must
 * request fresh copies through copy* methods.
 *
 * This gives the following ownership boundary:
 *
 * ```text
 * caller-owned mutable arrays
 * -> FrozenMetamodelImageAssemblyInput.issue(...)
 * -> assembly-owned private array snapshots
 * -> assembler receives fresh publication copies
 * ```
 *
 * Single-pass consumption law:
 *
 * copyTypeReferences(), copyShapeEntries(), copyCycleIdentityEntries(), and
 * copyRawFactEntries() each allocate a fresh array snapshot.
 *
 * Assemblers must call each copy method at most once per publication pass and
 * store the returned arrays in local variables.
 *
 * They must not repeatedly call copy* methods inside loops or helper methods.
 *
 * Reason:
 *
 * This input object protects its private snapshots from mutation by returning
 * copies. Repeated calls would create avoidable short-lived arrays and increase
 * freeze-time allocation pressure.
 *
 * Payload immutability law:
 *
 * This input snapshots arrays, not payload object graphs.
 *
 * TypeReference, FrozenTypeShapeTableEntry, FrozenTypeCycleIdentityTableEntry,
 * FrozenRawFactTableEntry, ResolvedTypeShape, TypeCycleIdentity, and
 * RawTypeFactsDTO must already be immutable adapter-neutral frozen material
 * before entering this command object.
 *
 * This object must not deep-copy payloads.
 *
 * Deep-copying here would:
 *
 * - hide upstream mutability bugs;
 * - duplicate frozen material;
 * - obscure ownership diagnostics;
 * - add avoidable allocation pressure;
 * - blur the boundary between payload issuance and image assembly.
 *
 * Architecture tests must reject mutable collections, backend handles, lazy
 * delegates, service callbacks, mutable acquisition slots, and closure-backed
 * recovery paths reachable from frozen payload material.
 *
 * Capacity law:
 *
 * This object performs only local structural sanity checks.
 *
 * It does not own global capacity governance.
 *
 * Global limits such as maximum type count, maximum property count, maximum raw
 * fact count, and maximum annotation count belong to acquisition/session
 * capacity policy.
 *
 * However, a payload table cannot contain more entries than the type index can
 * possibly address. Therefore this object rejects:
 *
 * ```text
 * shapeEntries.size > typeReferences.size
 * cycleIdentityEntries.size > typeReferences.size
 * rawFactEntries.size > typeReferences.size
 * ```
 *
 * That is not a resource policy. It is an ordinal-table consistency law.
 *
 * Backend-erasure law:
 *
 * This object must not contain backend handles, adapter-local registries,
 * source adapter provenance, diagnostic envelopes, or planning providers.
 */
internal class FrozenMetamodelImageAssemblyInput private constructor(
    val imageId: FrozenMetamodelImageId,
    val schemaVersion: FrozenMetamodelImageSchemaVersion,
    val cycleIdentityAlgorithmId: String,
    val cycleIdentityAlgorithmVersion: Long,
    private val typeReferencesSnapshot: Array<TypeReference>,
    private val shapeEntriesSnapshot: Array<FrozenTypeShapeTableEntry>,
    private val cycleIdentityEntriesSnapshot: Array<FrozenTypeCycleIdentityTableEntry>,
    private val rawFactEntriesSnapshot: Array<FrozenRawFactTableEntry>,
) {
    val typeReferenceCount: Int
        get() = typeReferencesSnapshot.size

    val shapeEntryCount: Int
        get() = shapeEntriesSnapshot.size

    val cycleIdentityEntryCount: Int
        get() = cycleIdentityEntriesSnapshot.size

    val rawFactEntryCount: Int
        get() = rawFactEntriesSnapshot.size

    fun copyTypeReferences(): Array<TypeReference> {
        return typeReferencesSnapshot.copyOf()
    }

    fun copyShapeEntries(): Array<FrozenTypeShapeTableEntry> {
        return shapeEntriesSnapshot.copyOf()
    }

    fun copyCycleIdentityEntries(): Array<FrozenTypeCycleIdentityTableEntry> {
        return cycleIdentityEntriesSnapshot.copyOf()
    }

    fun copyRawFactEntries(): Array<FrozenRawFactTableEntry> {
        return rawFactEntriesSnapshot.copyOf()
    }

    override fun toString(): String {
        return "FrozenMetamodelImageAssemblyInput(" +
                "imageId=${imageId.renderSummary()}, " +
                "schemaVersion=${schemaVersion.renderSummary()}, " +
                "typeReferenceCount=$typeReferenceCount, " +
                "shapeEntryCount=$shapeEntryCount, " +
                "cycleIdentityEntryCount=$cycleIdentityEntryCount, " +
                "rawFactEntryCount=$rawFactEntryCount, " +
                "cycleIdentityAlgorithmId=$cycleIdentityAlgorithmId, " +
                "cycleIdentityAlgorithmVersion=$cycleIdentityAlgorithmVersion" +
                ")"
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            typeReferences: Array<TypeReference>,
            shapeEntries: Array<FrozenTypeShapeTableEntry>,
            cycleIdentityAlgorithmId: String,
            cycleIdentityAlgorithmVersion: Long,
            cycleIdentityEntries: Array<FrozenTypeCycleIdentityTableEntry>,
            rawFactEntries: Array<FrozenRawFactTableEntry>,
        ): FrozenMetamodelImageAssemblyInput {
            val typeReferencesSnapshot =
                typeReferences.copyOf()

            val shapeEntriesSnapshot =
                shapeEntries.copyOf()

            val cycleIdentityEntriesSnapshot =
                cycleIdentityEntries.copyOf()

            val rawFactEntriesSnapshot =
                rawFactEntries.copyOf()

            requireEntryCountDoesNotExceedTypeReferenceCount(
                imageId = imageId,
                tableId = FrozenMetamodelImageTableId.SHAPE_TABLE,
                entryCount = shapeEntriesSnapshot.size,
                typeReferenceCount = typeReferencesSnapshot.size,
            )

            requireEntryCountDoesNotExceedTypeReferenceCount(
                imageId = imageId,
                tableId = FrozenMetamodelImageTableId.CYCLE_IDENTITY_TABLE,
                entryCount = cycleIdentityEntriesSnapshot.size,
                typeReferenceCount = typeReferencesSnapshot.size,
            )

            requireEntryCountDoesNotExceedTypeReferenceCount(
                imageId = imageId,
                tableId = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
                entryCount = rawFactEntriesSnapshot.size,
                typeReferenceCount = typeReferencesSnapshot.size,
            )

            return FrozenMetamodelImageAssemblyInput(
                imageId = imageId,
                schemaVersion = schemaVersion,
                cycleIdentityAlgorithmId = cycleIdentityAlgorithmId,
                cycleIdentityAlgorithmVersion = cycleIdentityAlgorithmVersion,
                typeReferencesSnapshot = typeReferencesSnapshot,
                shapeEntriesSnapshot = shapeEntriesSnapshot,
                cycleIdentityEntriesSnapshot = cycleIdentityEntriesSnapshot,
                rawFactEntriesSnapshot = rawFactEntriesSnapshot,
            )
        }

        private fun requireEntryCountDoesNotExceedTypeReferenceCount(
            imageId: FrozenMetamodelImageId,
            tableId: FrozenMetamodelImageTableId,
            entryCount: Int,
            typeReferenceCount: Int,
        ) {
            if (entryCount <= typeReferenceCount) {
                return
            }

            throw FrozenMetamodelSequenceViolationException(
                imageId = imageId,
                sequenceTable = tableId.name,
                referenceSummary = "<image-wide>",
                reason = "Frozen assembly input contains more table entries than indexed TypeReferences: " +
                        "table=${tableId.name}, " +
                        "entryCount=$entryCount, " +
                        "typeReferenceCount=$typeReferenceCount. " +
                        "A valid ordinal-addressed frozen table can contain at most one entry per indexed TypeReference.",
            )
        }
    }
}
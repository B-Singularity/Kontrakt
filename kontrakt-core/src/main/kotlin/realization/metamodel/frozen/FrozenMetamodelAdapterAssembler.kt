package realization.metamodel.frozen

import realization.metamodel.frozen.provider.FrozenMetamodelProviderBundle
import stage.admission.diagnostics.evidence.MetamodelAdapterAssemblyException
import stage.admission.diagnostics.evidence.MetamodelException
import stage.canonicalization.material.frozen.image.FrozenMetamodelImage
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageAssembler
import stage.canonicalization.material.frozen.image.FrozenMetamodelImageAssemblyInput

/**
 * Frozen metamodel adapter composition root.
 *
 * This assembler connects adapter-neutral frozen assembly material to
 * planning-facing frozen providers.
 *
 * It is the frozen-image counterpart of the live reflection adapter composition
 * root, but it deliberately has no backend-handle lifecycle.
 *
 * Responsibility:
 *
 * ```text
 * FrozenMetamodelImageAssemblyInput
 * -> FrozenMetamodelImageAssembler
 * -> FrozenMetamodelImage
 * -> FrozenMetamodelProviderBundle
 * -> planning-facing outbound ports
 * ```
 *
 * This object does not:
 *
 * - perform reflection/KSP/bytecode/source discovery;
 * - issue TypeReference directly;
 * - derive canonical type identity;
 * - assign frozen ordinals itself;
 * - sort table arrays itself;
 * - build diagnostic envelopes;
 * - expose source adapter provenance to planning;
 * - own cache/interner behavior;
 * - own runtime lifecycle after assembly.
 *
 * Command-input law:
 *
 * This assembler accepts FrozenMetamodelImageAssemblyInput as its only frozen
 * image assembly input.
 *
 * It does not expose long-parameter assembly overloads.
 *
 * Adapter/freeze collectors must build one command object first, then pass that
 * command to this assembler.
 *
 * Hexagonal boundary:
 *
 * Planning code should receive only FrozenMetamodelProviderBundle's outbound
 * ports:
 *
 * - TypeShapeProvider
 * - TypeCycleIdentityProvider
 * - RawTypeFactsProvider
 *
 * Planning code must not depend on:
 *
 * - FrozenMetamodelImage internals;
 * - frozen table implementations;
 * - FrozenMetamodelImageEnvelope;
 * - FrozenMetamodelImageDiagnosticHeader;
 * - source adapter provenance;
 * - adapter acquisition order.
 *
 * Ordinal authority law:
 *
 * This assembler delegates all image-local ordinal alignment to
 * FrozenMetamodelImageAssembler.
 *
 * Adapter/infrastructure code may collect `(TypeReference, payload)` entries,
 * but it must not assign frozen ordinals, pre-sort table arrays, or rely on
 * backend discovery order as frozen table order.
 *
 * Lifecycle law:
 *
 * This assembler returns immutable frozen provider wiring and owns no resources.
 *
 * It does not implement AutoCloseable.
 *
 * If a caller needs to close backend handles, those handles belong to the live
 * acquisition adapter that produced the frozen material, not to the frozen
 * provider bundle.
 *
 * Exception law:
 *
 * Domain publication exceptions are rethrown unchanged.
 *
 * Reason:
 *
 * Frozen publication failures such as incomplete table coverage, integrity
 * mismatch, invalid ordinal access, duplicate entries, or unknown references
 * are part of the metamodel-domain exception taxonomy and should not be hidden
 * behind a generic adapter assembly wrapper.
 *
 * Unexpected runtime failures are wrapped as MetamodelAdapterAssemblyException
 * with assembly context.
 *
 * Serialization law:
 *
 * The returned bundle is runtime provider wiring, not serializable state.
 *
 * Do not serialize this assembler result for cache payloads, diagnostic
 * payloads, session storage, network transport, or persistent frozen image
 * storage.
 */
internal object FrozenMetamodelAdapterAssembler {
    @JvmStatic
    fun assemble(
        input: FrozenMetamodelImageAssemblyInput,
    ): FrozenMetamodelProviderBundle {
        return try {
            val image =
                assembleImage(
                    input = input,
                )

            FrozenMetamodelProviderBundle.issue(
                image = image,
            )
        } catch (e: MetamodelAdapterAssemblyException) {
            throw e
        } catch (e: MetamodelException) {
            throw e
        } catch (e: RuntimeException) {
            throw MetamodelAdapterAssemblyException(
                "Frozen metamodel adapter assembly failed. " +
                        "blame=frozen-adapter-composition, " +
                        "imageId=${input.imageId.renderSummary()}, " +
                        "schemaVersion=${input.schemaVersion.renderSummary()}, " +
                        "typeReferenceCount=${input.typeReferenceCount}, " +
                        "shapeEntryCount=${input.shapeEntryCount}, " +
                        "cycleIdentityEntryCount=${input.cycleIdentityEntryCount}, " +
                        "rawFactEntryCount=${input.rawFactEntryCount}, " +
                        "cycleIdentityAlgorithmId=${input.cycleIdentityAlgorithmId}, " +
                        "cycleIdentityAlgorithmVersion=${input.cycleIdentityAlgorithmVersion}, " +
                        "cause=${e::class.qualifiedName}: ${e.message}",
            )
        }
    }

    @JvmStatic
    fun assembleImage(
        input: FrozenMetamodelImageAssemblyInput,
    ): FrozenMetamodelImage {
        return try {
            FrozenMetamodelImageAssembler.issue(
                input = input,
            )
        } catch (e: MetamodelAdapterAssemblyException) {
            throw e
        } catch (e: MetamodelException) {
            throw e
        } catch (e: RuntimeException) {
            throw MetamodelAdapterAssemblyException(
                "Frozen metamodel image assembly failed. " +
                        "blame=frozen-image-composition, " +
                        "imageId=${input.imageId.renderSummary()}, " +
                        "schemaVersion=${input.schemaVersion.renderSummary()}, " +
                        "typeReferenceCount=${input.typeReferenceCount}, " +
                        "shapeEntryCount=${input.shapeEntryCount}, " +
                        "cycleIdentityEntryCount=${input.cycleIdentityEntryCount}, " +
                        "rawFactEntryCount=${input.rawFactEntryCount}, " +
                        "cycleIdentityAlgorithmId=${input.cycleIdentityAlgorithmId}, " +
                        "cycleIdentityAlgorithmVersion=${input.cycleIdentityAlgorithmVersion}, " +
                        "cause=${e::class.qualifiedName}: ${e.message}",
            )
        }
    }
}
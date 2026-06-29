package realization.metamodel.frozen.provider

import stage.canonicalization.material.frozen.image.FrozenMetamodelImage
import stage.lowering.boundary.RawTypeFactsProvider
import stage.lowering.boundary.TypeCycleIdentityProvider
import stage.lowering.boundary.TypeShapeProvider

/**
 * Planning-facing provider bundle backed by a FrozenMetamodelImage.
 *
 * This object is the frozen-image counterpart of the reflection adapter bundle,
 * but it deliberately does not implement AutoCloseable.
 *
 * Reason:
 *
 * A FrozenMetamodelImage is immutable adapter-neutral semantic material.
 *
 * It does not own:
 *
 * - KType handles;
 * - KClass handles;
 * - KSP symbols;
 * - bytecode parser handles;
 * - source AST handles;
 * - mutable acquisition slots;
 * - backend registries;
 * - adapter-local sidecar state.
 *
 * Therefore this bundle has no runtime resource lifecycle to close.
 *
 * Hexagonal role:
 *
 * This bundle is an adapter-side composition result exposing only planning
 * outbound ports:
 *
 * - [typeShapeProvider]
 * - [typeCycleIdentityProvider]
 * - [rawTypeFactsProvider]
 *
 * Planning code must consume these ports, not FrozenMetamodelImage internals,
 * diagnostic headers, source adapter provenance, or frozen table
 * implementations.
 *
 * Image coherence law:
 *
 * All providers in this bundle are issued from the same FrozenMetamodelImage.
 *
 * This prevents accidental wiring such as:
 *
 * ```text
 * shapeProvider(imageA)
 * cycleIdentityProvider(imageB)
 * rawTypeFactsProvider(imageC)
 * ```
 *
 * Such mixed-image wiring would violate frozen ordinal locality because each
 * image owns its own TypeReferenceIndex and table ordinal layout.
 *
 * Provenance exclusion law:
 *
 * This bundle receives only FrozenMetamodelImage.
 *
 * It must not receive:
 *
 * - FrozenMetamodelImageEnvelope;
 * - FrozenMetamodelImageDiagnosticHeader;
 * - MetamodelSourceAdapterProvenance.
 *
 * Source adapter provenance is diagnostic/compatibility material, not planning
 * semantic material.
 *
 * Provider lookup law:
 *
 * The contained providers must use:
 *
 * ```text
 * TypeReference
 * -> image.typeIndex.ordinalOf(reference)
 * -> table.findAt(frozenOrdinal)
 * ```
 *
 * They must not reopen backend handles or repeat reference-based table lookup.
 */
class FrozenMetamodelProviderBundle private constructor(
    val typeShapeProvider: TypeShapeProvider,
    val typeCycleIdentityProvider: TypeCycleIdentityProvider,
    val rawTypeFactsProvider: RawTypeFactsProvider,
) {
    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
        ): FrozenMetamodelProviderBundle {
            return FrozenMetamodelProviderBundle(
                typeShapeProvider =
                    FrozenMetamodelTypeShapeProvider.issue(
                        image = image,
                    ),
                typeCycleIdentityProvider =
                    FrozenMetamodelTypeCycleIdentityProvider.issue(
                        image = image,
                    ),
                rawTypeFactsProvider =
                    FrozenMetamodelRawTypeFactsProvider.issue(
                        image = image,
                    ),
            )
        }
    }
}
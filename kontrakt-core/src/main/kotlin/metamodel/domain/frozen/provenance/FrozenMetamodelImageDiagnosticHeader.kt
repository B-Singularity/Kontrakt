package metamodel.domain.frozen.provenance

import metamodel.domain.frozen.image.FrozenMetamodelImageId

/**
 * Diagnostic header for one FrozenMetamodelImage.
 *
 * This object is diagnostic/bootstrap material.
 *
 * It is not:
 *
 * - planning-visible semantic input;
 * - type-expansion input;
 * - active-cycle identity material;
 * - PlanCacheKey material;
 * - route64 material;
 * - canonical IR material;
 * - L2 promotion material.
 *
 * Image binding law:
 *
 * The header carries [imageId] only to bind diagnostics to the exact frozen
 * image they describe.
 *
 * That image id is diagnostic/compatibility material. It must not be used as a
 * semantic type identity, L2 key, route key, or planning branch condition.
 *
 * Provenance isolation law:
 *
 * [sourceAdapterProvenance] may explain where the image came from, but planning
 * must not branch on reflection/KSP/bytecode/source/generated provenance.
 *
 * Serialization law:
 *
 * This header may be serialized only through explicitly diagnostic/reporting
 * surfaces. It must not be serialized as part of planning-visible frozen image
 * payload, L2 cache payload, PlanCacheKey material, or canonical IR material.
 *
 * Sanitization law:
 *
 * Any future diagnostic field added to this header must remain backend-neutral
 * and sanitized. It must not hold backend-native handles, local filesystem
 * paths, environment variables, classloader identities, resolver-local ids, or
 * registry keys that can recover backend handles unless a future diagnostic
 * ADR explicitly ratifies the field and its redaction policy.
 */
class FrozenMetamodelImageDiagnosticHeader private constructor(
    val imageId: FrozenMetamodelImageId,
    val sourceAdapterProvenance: MetamodelSourceAdapterProvenance,
) {
    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            sourceAdapterProvenance: MetamodelSourceAdapterProvenance,
        ): FrozenMetamodelImageDiagnosticHeader {
            return FrozenMetamodelImageDiagnosticHeader(
                imageId = imageId,
                sourceAdapterProvenance = sourceAdapterProvenance,
            )
        }
    }
}
package metamodel.domain.frozen.image

import metamodel.domain.exception.FrozenMetamodelIntegrityViolationException
import metamodel.domain.frozen.provenance.FrozenMetamodelImageDiagnosticHeader

/**
 * Adapter/bootstrap return object for one frozen metamodel image.
 *
 * This envelope deliberately exists outside planning-facing provider contracts.
 *
 * Correct boundary:
 *
 * ```text
 * acquisition / adapter / bootstrap
 * -> FrozenMetamodelImageEnvelope
 * -> bootstrap may inspect diagnosticHeader
 * -> planning providers receive image only
 * -> envelope reference is dropped
 * ```
 *
 * Incorrect boundary:
 *
 * ```text
 * planning provider receives FrozenMetamodelImageEnvelope
 * -> reads diagnosticHeader
 * -> branches on sourceAdapterProvenance
 * -> backend-dependent semantic behavior
 * ```
 *
 * Semantic isolation law:
 *
 * [image] is the only planning-visible semantic payload in this object.
 *
 * [diagnosticHeader] is intentionally [internal] so ordinary external consumers
 * cannot accidentally use diagnostic provenance as a planning decision axis.
 *
 * This is not the only enforcement mechanism. The stronger enforcement must be:
 *
 * - provider constructors accept FrozenMetamodelImage, not this envelope;
 * - architectural tests reject provider dependencies on this class;
 * - L2 promotion rejects this class;
 * - future module boundaries prevent planning modules from depending on
 *   diagnostic provenance packages.
 *
 * Image/header binding law:
 *
 * issue(...) validates:
 *
 * ```text
 * image.imageId == diagnosticHeader.imageId
 * ```
 *
 * A mismatched envelope is an integrity violation because it can attach
 * diagnostics from one acquisition to a different semantic image.
 *
 * Memory ownership law:
 *
 * The envelope is expected to be short-lived.
 *
 * Bootstrap code should extract [image], wire planning-facing providers with
 * that image only, and then release the envelope reference. This prevents
 * diagnostic provenance graphs from being retained for the whole planning
 * lifetime.
 *
 * Serialization law:
 *
 * This envelope must not be serialized as a planning artifact, L2 payload,
 * canonical IR payload, or persistent frozen-image semantic payload.
 *
 * If diagnostic reporting needs serialization, it must serialize an explicitly
 * diagnostic DTO with redaction/sanitization policy. It must not reuse this
 * envelope as a generic transport object.
 */
class FrozenMetamodelImageEnvelope private constructor(
    val image: FrozenMetamodelImage,
    internal val diagnosticHeader: FrozenMetamodelImageDiagnosticHeader,
) {
    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
            diagnosticHeader: FrozenMetamodelImageDiagnosticHeader,
        ): FrozenMetamodelImageEnvelope {
            if (diagnosticHeader.imageId != image.imageId) {
                throw FrozenMetamodelIntegrityViolationException(
                    imageId = image.imageId,
                    reason = "FrozenMetamodelImageEnvelope image/header mismatch: " +
                            "image.imageId=${image.imageId}, diagnosticHeader.imageId=${diagnosticHeader.imageId}",
                )
            }

            return FrozenMetamodelImageEnvelope(
                image = image,
                diagnosticHeader = diagnosticHeader,
            )
        }
    }
}
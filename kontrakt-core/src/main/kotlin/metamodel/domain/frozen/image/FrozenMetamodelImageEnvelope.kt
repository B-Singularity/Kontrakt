package metamodel.domain.frozen.image

import metamodel.domain.frozen.provenance.FrozenMetamodelImageDiagnosticHeader

/**
 * Adapter/bootstrap return object.
 *
 * This envelope may carry diagnostic provenance, but planning-facing providers
 * must receive only [image].
 *
 * Do not inject this envelope into planning ports.
 *
 * Correct:
 *
 * ```text
 * FrozenMetamodelImageEnvelope
 * -> adapter/bootstrap diagnostics may read diagnosticHeader
 * -> planning providers receive image only
 * ```
 *
 * Incorrect:
 *
 * ```text
 * planning provider receives envelope
 * -> branches on diagnosticHeader.sourceAdapterProvenance
 * ```
 */
class FrozenMetamodelImageEnvelope private constructor(
    val image: FrozenMetamodelImage,
    val diagnosticHeader: FrozenMetamodelImageDiagnosticHeader,
) {
    companion object {
        @JvmStatic
        fun issue(
            image: FrozenMetamodelImage,
            diagnosticHeader: FrozenMetamodelImageDiagnosticHeader,
        ): FrozenMetamodelImageEnvelope {
            return FrozenMetamodelImageEnvelope(
                image = image,
                diagnosticHeader = diagnosticHeader,
            )
        }
    }
}
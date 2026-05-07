package metamodel.domain.frozen.provenance

/**
 * Diagnostic header for a frozen metamodel image.
 *
 * This object is not semantic planning input.
 *
 * It must not be injected into planning-facing providers.
 */
class FrozenMetamodelImageDiagnosticHeader private constructor(
    val sourceAdapterProvenance: MetamodelSourceAdapterProvenance,
) {
    companion object {
        @JvmStatic
        fun issue(
            sourceAdapterProvenance: MetamodelSourceAdapterProvenance,
        ): FrozenMetamodelImageDiagnosticHeader {
            return FrozenMetamodelImageDiagnosticHeader(
                sourceAdapterProvenance = sourceAdapterProvenance,
            )
        }
    }
}
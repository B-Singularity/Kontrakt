package metamodel.domain.frozen.provenance

import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Diagnostic provenance for the adapter that produced a FrozenMetamodelImage.
 *
 * Provenance is compatibility and diagnostic material.
 *
 * It must not influence:
 * - TypeReference equality;
 * - planning traversal;
 * - active-cycle identity;
 * - plan cache key equality;
 * - L2 route selection.
 */
class MetamodelSourceAdapterProvenance private constructor(
    val sourceAdapterKind: MetamodelSourceAdapterKind,
    val sourceAdapterVersion: String,
) {
    override fun toString(): String {
        return "$sourceAdapterKind@$sourceAdapterVersion"
    }

    companion object {
        @JvmStatic
        fun issue(
            sourceAdapterKind: MetamodelSourceAdapterKind,
            sourceAdapterVersion: String,
        ): MetamodelSourceAdapterProvenance {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "MetamodelSourceAdapterProvenance.sourceAdapterVersion",
                value = sourceAdapterVersion,
                maxChars = MAX_SOURCE_ADAPTER_VERSION_CHARS,
            )

            return MetamodelSourceAdapterProvenance(
                sourceAdapterKind = sourceAdapterKind,
                sourceAdapterVersion = sourceAdapterVersion,
            )
        }

        private const val MAX_SOURCE_ADAPTER_VERSION_CHARS: Int = 128
    }
}
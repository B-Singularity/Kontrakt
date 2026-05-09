package metamodel.domain.frozen.provenance

import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Diagnostic provenance for the source adapter that produced a
 * FrozenMetamodelImage.
 *
 * This object is diagnostic and compatibility-reporting material.
 *
 * It is not:
 *
 * - planning-visible semantic input;
 * - TypeReference equality material;
 * - active-cycle identity material;
 * - type-expansion decision material;
 * - canonical IR material;
 * - PlanCacheKey material;
 * - route64 material;
 * - L2 promotion material.
 *
 * Provenance isolation law:
 *
 * A planning-facing provider must not receive this object.
 *
 * The allowed flow is:
 *
 * ```text
 * acquisition / adapter / bootstrap
 * -> MetamodelSourceAdapterProvenance
 * -> FrozenMetamodelImageDiagnosticHeader
 * -> FrozenMetamodelImageEnvelope
 * -> diagnostic tooling may inspect provenance
 * -> planning providers receive FrozenMetamodelImage only
 * ```
 *
 * The forbidden flow is:
 *
 * ```text
 * planning provider
 * -> reads MetamodelSourceAdapterProvenance
 * -> branches on REFLECTION / KSP / BYTECODE / SOURCE / GENERATED
 * -> backend-dependent semantic behavior
 * ```
 *
 * Equality law:
 *
 * Equality is structural over:
 *
 * - sourceAdapterKind;
 * - sourceAdapterVersion.
 *
 * This is intentionally allowed for diagnostic aggregation, compatibility
 * reports, and test assertions.
 *
 * Structural equality here must not be confused with semantic equivalence of
 * frozen images. Two images may have the same source adapter provenance and
 * still contain different semantic material. Conversely, two different source
 * adapters may produce semantically equivalent frozen images.
 *
 * Version law:
 *
 * [sourceAdapterVersion] is an exact diagnostic token.
 *
 * This class does not parse semantic versions and does not define compatibility
 * between tokens such as:
 *
 * ```text
 * 1.0
 * 1.0.0
 * ```
 *
 * Adapter-version compatibility, if needed later, must be handled by a separate
 * diagnostic/compatibility policy. It must not be interpreted by planning
 * providers and must not become a semantic branch condition.
 *
 * Hash law:
 *
 * hashCode currently uses Kotlin/JVM hashCode surfaces as a transitional
 * in-memory equality-collection companion, consistent with the current
 * metamodel VO family.
 *
 * This is intentionally not a canonical hash policy.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persistent image identity;
 * - route key;
 * - L1/L2 partition key;
 * - cross-runtime order hash;
 * - serialized order digest.
 *
 * Future hash law:
 *
 * The later BLAKE3 / metadata-hash refactoring may replace this transitional
 * hashCode strategy together with the other metamodel value objects. Do not
 * locally introduce a separate hash family in this VO.
 */
class MetamodelSourceAdapterProvenance private constructor(
    val sourceAdapterKind: MetamodelSourceAdapterKind,
    val sourceAdapterVersion: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "MetamodelSourceAdapterProvenance(" +
                "kind=$sourceAdapterKind, " +
                "version=$sourceAdapterVersion" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is MetamodelSourceAdapterProvenance) return false

        /*
         * Cheap negative filter for diagnostic maps/sets.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * not order material and is not a semantic identity authority.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return sourceAdapterKind == other.sourceAdapterKind &&
                sourceAdapterVersion == other.sourceAdapterVersion
    }

    override fun hashCode(): Int =
        precomputedHashCode

    override fun toString(): String =
        renderSummary()

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
                precomputedHashCode = computeHashCode(
                    sourceAdapterKind = sourceAdapterKind,
                    sourceAdapterVersion = sourceAdapterVersion,
                ),
            )
        }

        /**
         * Computes the transitional JVM hashCode companion.
         *
         * This deliberately stays aligned with the current metamodel VO family
         * until the later BLAKE3 / metadata-hash refactoring replaces hashCode
         * policy globally.
         *
         * The enum kind is mixed through a pinned local order order rather
         * than Enum.hashCode(), because Enum.hashCode() is not a semantic
         * order surface.
         *
         * The version token currently uses String.hashCode() as transitional
         * in-memory hash material. This must not become routing, persistence, L2,
         * or canonical digest material.
         */
        private fun computeHashCode(
            sourceAdapterKind: MetamodelSourceAdapterKind,
            sourceAdapterVersion: String,
        ): Int {
            var result = sourceAdapterKindProtocolOrder(sourceAdapterKind)
            result = 31 * result + sourceAdapterVersion.hashCode()
            return result
        }

        /**
         * Pinned local ordering for diagnostic provenance hashing.
         *
         * Do not replace this with enum.ordinal.
         *
         * enum.ordinal is source-order dependent and can drift if enum constants
         * are rearranged. This mapping is intentionally explicit so future
         * additions must make the ordering decision visible.
         */
        private fun sourceAdapterKindProtocolOrder(
            sourceAdapterKind: MetamodelSourceAdapterKind,
        ): Int =
            when (sourceAdapterKind) {
                MetamodelSourceAdapterKind.REFLECTION -> 1
                MetamodelSourceAdapterKind.KSP -> 2
                MetamodelSourceAdapterKind.BYTECODE -> 3
                MetamodelSourceAdapterKind.SOURCE -> 4
                MetamodelSourceAdapterKind.GENERATED -> 5
            }

        private const val MAX_SOURCE_ADAPTER_VERSION_CHARS: Int = 128
    }
}
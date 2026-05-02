package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.exception.MetamodelNormalizationViolationException
import metamodel.domain.port.outgoing.NormalizationEngine

/**
 * Canonical textual material for metamodel type identity.
 *
 * This VO is the ratified entry point from raw adapter/source/reflection/KSP
 * text material into the canonical metamodel identity boundary.
 *
 * Important rules:
 *
 * - It does not normalize.
 * - It does not repair.
 * - It does not call java.text.Normalizer.
 * - It does not call Character.*.
 * - It does not depend on reflection.
 * - It does not depend on KSP.
 * - It does not perform byte encoding.
 * - It does not compute policy fingerprints.
 * - It does not cache hashCode.
 * - It does not choose policy defaults.
 * - It does not eagerly allocate provenance strings.
 *
 * The NormalizationEngine is the adapter-owned Unicode inspection boundary.
 * It returns:
 *
 * - an immutable inspected snapshot;
 * - deterministic lexical facts for that exact snapshot.
 *
 * This VO must consume only the snapshot returned by the engine. It must never
 * build the canonical value from the original caller-owned CharSequence.
 *
 * TOCTOU law:
 *
 * The caller may pass a mutable CharSequence such as StringBuilder. Therefore
 * the domain must not trust the original input after inspection. The only text
 * admitted into this VO is CanonicalTypeTextInspectionResult.Accepted.snapshot.
 *
 * Equality law:
 *
 * Equality remains text-primary at this VO level. Policy, engine provenance, and
 * lexical profile are coherence/provenance facts, not direct equality axes here.
 *
 * Higher-level identity objects such as CanonicalTypeId include shape,
 * classifier law, and ratification fingerprint to prevent same-text/different-
 * shape collapse.
 *
 * Diagnostic law:
 *
 * Provenance strings are rendered lazily. The VO stores structured fields, not a
 * pre-joined diagnostic string. This prevents heap churn when many type texts
 * are ratified but only a small subset requires diagnostics.
 */
class CanonicalTypeText private constructor(
    val value: String,
    val normalizationEngineId: String,
    val normalizationEngineVersion: String,
    val unicodeProfileVersion: String,
    val goldenVectorSetId: String,
    val goldenVectorDigest: String,
    val inspectionPolicy: CanonicalTypeTextInspectionPolicy,
    val lexicalProfile: CanonicalTypeLexicalProfile,
) {
    override fun equals(other: Any?): Boolean = other is CanonicalTypeText && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    /**
     * Requires the other value to have the same ratification context.
     *
     * This is intentionally separate from equals(...).
     *
     * Use this when a caller needs to assert that two equal canonical texts were
     * accepted under the same engine, Unicode profile, golden-vector set, and
     * inspection policy.
     */
    fun requireSameInspectionContextAs(other: CanonicalTypeText) {
        if (normalizationEngineId != other.normalizationEngineId ||
            normalizationEngineVersion != other.normalizationEngineVersion ||
            unicodeProfileVersion != other.unicodeProfileVersion ||
            goldenVectorSetId != other.goldenVectorSetId ||
            goldenVectorDigest != other.goldenVectorDigest ||
            inspectionPolicy != other.inspectionPolicy
        ) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeText inspection context mismatch: " +
                    "value=${diagnosticSample(value)}, " +
                    "this=${renderRatificationProvenance()}, " +
                    "other=${other.renderRatificationProvenance()}",
            )
        }
    }

    /**
     * Diagnostic-only provenance.
     *
     * This string is built lazily to avoid permanent heap overhead.
     *
     * It is not canonical byte material.
     * It is not a cache key.
     * It is not equality material.
     */
    fun renderRatificationProvenance(): String =
        buildRatificationProvenance(
            normalizationEngineId = normalizationEngineId,
            normalizationEngineVersion = normalizationEngineVersion,
            unicodeProfileVersion = unicodeProfileVersion,
            goldenVectorSetId = goldenVectorSetId,
            goldenVectorDigest = goldenVectorDigest,
            inspectionPolicy = inspectionPolicy,
            lexicalProfile = lexicalProfile,
        )

    companion object {
        private const val MAX_PROVENANCE_TOKEN_CHARS: Int = 192
        private const val MAX_TOTAL_PROVENANCE_CHARS: Int = 4_096
        private const val MAX_DIAGNOSTIC_TEXT_SAMPLE_CHARS: Int = 128

        /**
         * Ratifies raw type text.
         *
         * The policy must be supplied by an already-pinned metamodel/runtime
         * policy boundary. This method deliberately has no default policy.
         *
         * Flow:
         *
         * 1. Validate engine provenance surface.
         * 2. Perform a cheap caller-side length precheck.
         * 3. Delegate Unicode/scalar/NFC/script/token inspection to
         *    NormalizationEngine.
         * 4. Consume only Accepted.snapshot.
         * 5. Assert accepted lexical profile against policy.
         * 6. Run low-cost ASCII protocol guards over the inspected snapshot.
         * 7. Issue CanonicalTypeText.
         */
        @JvmStatic
        fun ratify(
            rawValue: CharSequence,
            normalizationEngine: NormalizationEngine,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
        ): CanonicalTypeText {
            requireEngineProvenance(normalizationEngine)

            if (rawValue.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeText raw input must not be empty.",
                )
            }

            /*
             * This is only a cheap caller-side precheck.
             *
             * The NormalizationEngine remains responsible for the authoritative
             * early-exit guard, bounded snapshot capture, snapshot length recheck,
             * invalid surrogate fast-fail, exact code-point counting, and NFC
             * inspection.
             */
            rejectImpossibleLengthBeforeInspection(
                rawValue = rawValue,
                inspectionPolicy = inspectionPolicy,
            )

            val inspection =
                normalizationEngine.inspectCanonicalTypeText(
                    input = rawValue,
                    policy = inspectionPolicy,
                )

            val accepted =
                when (inspection) {
                    is CanonicalTypeTextInspectionResult.Accepted -> inspection

                    is CanonicalTypeTextInspectionResult.Rejected -> {
                        throw MetamodelNormalizationViolationException(
                            field = "CanonicalTypeText.value",
                            valueSample = diagnosticSample(rawValue),
                            engineId = normalizationEngine.engineId,
                            engineVersion = normalizationEngine.engineVersion,
                            reason = "${inspection.violationCode.protocolToken}: ${inspection.reason}",
                        )
                    }
                }

            val snapshot = accepted.snapshot
            val lexicalProfile = accepted.lexicalProfile

            requireAcceptedProfileWithinPolicy(
                snapshot = snapshot,
                lexicalProfile = lexicalProfile,
                inspectionPolicy = inspectionPolicy,
                normalizationEngine = normalizationEngine,
            )

            CanonicalTypeTextGuards.validateInspectedSnapshot(
                field = "CanonicalTypeText.value",
                snapshot = snapshot,
                allowNullableMarker = inspectionPolicy.allowNullableMarker,
                allowStarProjection = inspectionPolicy.allowStarProjection,
            )

            return CanonicalTypeText(
                value = snapshot,
                normalizationEngineId = normalizationEngine.engineId,
                normalizationEngineVersion = normalizationEngine.engineVersion,
                unicodeProfileVersion = normalizationEngine.unicodeProfileVersion,
                goldenVectorSetId = normalizationEngine.goldenVectorSetId,
                goldenVectorDigest = normalizationEngine.goldenVectorDigest,
                inspectionPolicy = inspectionPolicy,
                lexicalProfile = lexicalProfile,
            )
        }

        private fun rejectImpossibleLengthBeforeInspection(
            rawValue: CharSequence,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
        ) {
            if (rawValue.length > inspectionPolicy.maxUtf16CodeUnitsBeforeSnapshot) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeText exceeds pre-inspection UTF-16 length guard: " +
                        "utf16Units=${rawValue.length}, " +
                        "maxUtf16CodeUnitsBeforeSnapshot=${inspectionPolicy.maxUtf16CodeUnitsBeforeSnapshot}",
                )
            }
        }

        private fun requireAcceptedProfileWithinPolicy(
            snapshot: String,
            lexicalProfile: CanonicalTypeLexicalProfile,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
            normalizationEngine: NormalizationEngine,
        ) {
            if (snapshot.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: accepted snapshot must not be empty.",
                )
            }

            if (snapshot.length != lexicalProfile.utf16CodeUnitCount) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: snapshot length does not match lexical profile. " +
                        "engine=${normalizationEngine.engineId}@${normalizationEngine.engineVersion}, " +
                        "snapshotUtf16Units=${snapshot.length}, " +
                        "profileUtf16Units=${lexicalProfile.utf16CodeUnitCount}, " +
                        "value=${diagnosticSample(snapshot)}",
                )
            }

            try {
                lexicalProfile.requireWithinPolicy(inspectionPolicy)
            } catch (exception: MetamodelFactContractViolationException) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: accepted lexical profile violates inspection policy. " +
                        "engine=${normalizationEngine.engineId}@${normalizationEngine.engineVersion}, " +
                        "unicode=${normalizationEngine.unicodeProfileVersion}, " +
                        "goldenVectors=${normalizationEngine.goldenVectorSetId}, " +
                        "policy=${inspectionPolicy.deterministicPolicyToken}, " +
                        "value=${diagnosticSample(snapshot)}, " +
                        "reason=${exception.message}",
                )
            }
        }

        private fun requireEngineProvenance(normalizationEngine: NormalizationEngine) {
            requireProtocolComponent(
                field = "NormalizationEngine.engineId",
                value = normalizationEngine.engineId,
            )
            requireProtocolComponent(
                field = "NormalizationEngine.engineVersion",
                value = normalizationEngine.engineVersion,
            )
            requireProtocolComponent(
                field = "NormalizationEngine.unicodeProfileVersion",
                value = normalizationEngine.unicodeProfileVersion,
            )
            requireProtocolComponent(
                field = "NormalizationEngine.goldenVectorSetId",
                value = normalizationEngine.goldenVectorSetId,
            )
            requireProtocolComponent(
                field = "NormalizationEngine.goldenVectorDigest",
                value = normalizationEngine.goldenVectorDigest,
            )
        }

        private fun requireProtocolComponent(
            field: String,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "$field must not be empty.",
                )
            }

            if (value.length > MAX_PROVENANCE_TOKEN_CHARS) {
                throw MetamodelFactContractViolationException(
                    "$field exceeds maximum allowed provenance token length.",
                )
            }

            if (
                value.indexOf('|') >= 0 ||
                value.indexOf('\u0000') >= 0 ||
                value.indexOf('\n') >= 0 ||
                value.indexOf('\r') >= 0 ||
                value.indexOf('\t') >= 0
            ) {
                throw MetamodelFactContractViolationException(
                    "$field contains a reserved protocol/control character.",
                )
            }
        }

        private fun buildRatificationProvenance(
            normalizationEngineId: String,
            normalizationEngineVersion: String,
            unicodeProfileVersion: String,
            goldenVectorSetId: String,
            goldenVectorDigest: String,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
            lexicalProfile: CanonicalTypeLexicalProfile,
        ): String {
            val provenance =
                "CanonicalTypeText" +
                    "|engine=$normalizationEngineId" +
                    "|engineVersion=$normalizationEngineVersion" +
                    "|unicode=$unicodeProfileVersion" +
                    "|goldenVectorSet=$goldenVectorSetId" +
                    "|goldenVectorDigest=$goldenVectorDigest" +
                    "|policy=${inspectionPolicy.deterministicPolicyToken}" +
                    "|utf16Units=${lexicalProfile.utf16CodeUnitCount}" +
                    "|codePoints=${lexicalProfile.codePointCount}" +
                    "|identifierTokens=${lexicalProfile.identifierTokenCount}" +
                    "|longestIdentifier=${lexicalProfile.longestIdentifierTokenCodePoints}" +
                    "|delimiters=${lexicalProfile.totalDelimiterCodePoints}" +
                    "|nonIdentifierCodePoints=${lexicalProfile.nonIdentifierCodePointCount}" +
                    "|grossCombiningMarks=${lexicalProfile.grossCombiningMarkCount}" +
                    "|maxCombiningMarksPerIdentifier=${lexicalProfile.maxCombiningMarksPerIdentifierToken}" +
                    "|maxGraphemeClustersPerIdentifier=${lexicalProfile.maxGraphemeClustersPerIdentifierToken}"

            if (provenance.length > MAX_TOTAL_PROVENANCE_CHARS) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeText ratification provenance exceeds maximum render length.",
                )
            }

            return provenance
        }

        private fun diagnosticSample(value: CharSequence): String {
            if (value.length <= MAX_DIAGNOSTIC_TEXT_SAMPLE_CHARS) {
                return value.toString()
            }

            if (value is String) {
                return value.substring(0, MAX_DIAGNOSTIC_TEXT_SAMPLE_CHARS) + "...<truncated>"
            }

            val builder = StringBuilder(MAX_DIAGNOSTIC_TEXT_SAMPLE_CHARS + 14)

            for (index in 0 until MAX_DIAGNOSTIC_TEXT_SAMPLE_CHARS) {
                builder.append(value[index])
            }

            builder.append("...<truncated>")
            return builder.toString()
        }
    }
}

package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.exception.MetamodelNormalizationViolationException
import metamodel.port.outgoing.CanonicalTypeLexicalProfile
import metamodel.port.outgoing.CanonicalTypeTextInspectionPolicy
import metamodel.port.outgoing.CanonicalTypeTextInspectionResult
import metamodel.port.outgoing.NormalizationEngine

/**
 * Canonical textual material for metamodel type identity.
 *
 * This VO is the ratified entry point from raw adapter/source/reflection/KSP
 * strings into the canonical metamodel identity boundary.
 *
 * Important rules:
 *
 * - It does not normalize.
 * - It does not repair.
 * - It does not call java.text.Normalizer.
 * - It does not call Character.*.
 * - It does not issue "trusted" instances without inspection.
 * - It does not perform byte encoding.
 * - It does not cache hashCode.
 * - Equality remains value-primary.
 *
 * The NormalizationEngine inspection result is the expensive Unicode / script /
 * scalar / NFC proof. This VO performs only cheap protocol cross-checks against
 * ASCII grammar markers to detect faulty adapter inspection profiles.
 */
class CanonicalTypeText private constructor(
    val value: String,
    val normalizationEngineId: String,
    val normalizationEngineVersion: String,
    val unicodeProfileVersion: String,
    val inspectionPolicy: CanonicalTypeTextInspectionPolicy,
    val lexicalProfile: CanonicalTypeLexicalProfile,
    private val ratificationProvenance: String,
) {
    /**
     * Value-primary equality.
     *
     * Policy and engine provenance are not equality axes here. Mixing equal text
     * under incompatible policy/provenance must be rejected by the higher-level
     * TypeReferenceFactory / coherence registry, not hidden inside equals.
     */
    override fun equals(other: Any?): Boolean {
        return other is CanonicalTypeText && value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

    /**
     * Diagnostic-only provenance.
     *
     * Precomputed once to avoid repeatedly allocating diagnostic strings on
     * failure paths. This is not canonical byte material and must not be used as
     * equality or cache identity.
     */
    fun renderRatificationProvenance(): String {
        return ratificationProvenance
    }

    companion object {
        private const val MAX_UTF16_UNITS_PER_CODE_POINT: Int = 2

        /**
         * Ratifies raw type text.
         *
         * The cheap UTF-16 length precheck prevents obviously oversized input
         * from entering the normalization engine before policy bounds are even
         * considered. The engine still remains the authority for exact code-point
         * counting and Unicode/NFC inspection.
         */
        @JvmStatic
        fun ratify(
            rawValue: String,
            normalizationEngine: NormalizationEngine,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy =
                CanonicalTypeTextInspectionPolicy.strictTypeIdentityText(),
        ): CanonicalTypeText {
            requireEngineProvenance(normalizationEngine)

            if (rawValue.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeText must not be empty.",
                )
            }

            rejectImpossibleLengthBeforeInspection(
                rawValue = rawValue,
                inspectionPolicy = inspectionPolicy,
            )

            val inspection = normalizationEngine.inspectCanonicalTypeText(
                input = rawValue,
                policy = inspectionPolicy,
            )

            val lexicalProfile = when (inspection) {
                is CanonicalTypeTextInspectionResult.Accepted -> inspection.lexicalProfile
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

            requireAcceptedProfileWithinPolicy(
                rawValue = rawValue,
                lexicalProfile = lexicalProfile,
                inspectionPolicy = inspectionPolicy,
                normalizationEngine = normalizationEngine,
            )

            requireLexicalProfileCrossCheck(
                rawValue = rawValue,
                lexicalProfile = lexicalProfile,
            )

            return CanonicalTypeText(
                value = rawValue,
                normalizationEngineId = normalizationEngine.engineId,
                normalizationEngineVersion = normalizationEngine.engineVersion,
                unicodeProfileVersion = normalizationEngine.unicodeProfileVersion,
                inspectionPolicy = inspectionPolicy,
                lexicalProfile = lexicalProfile,
                ratificationProvenance = buildRatificationProvenance(
                    normalizationEngine = normalizationEngine,
                    inspectionPolicy = inspectionPolicy,
                    lexicalProfile = lexicalProfile,
                ),
            )
        }

        private fun rejectImpossibleLengthBeforeInspection(
            rawValue: String,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
        ) {
            val maxUtf16Units = inspectionPolicy.maxCodePoints.toLong() *
                    MAX_UTF16_UNITS_PER_CODE_POINT.toLong()

            if (rawValue.length.toLong() > maxUtf16Units) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeText exceeds pre-inspection UTF-16 length guard: " +
                            "utf16Units=${rawValue.length}, maxPossibleUtf16Units=$maxUtf16Units, " +
                            "policyMaxCodePoints=${inspectionPolicy.maxCodePoints}",
                )
            }
        }

        private fun requireAcceptedProfileWithinPolicy(
            rawValue: String,
            lexicalProfile: CanonicalTypeLexicalProfile,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
            normalizationEngine: NormalizationEngine,
        ) {
            if (!lexicalProfile.isNfc) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: accepted canonical type text is not marked NFC. " +
                            "engine=${normalizationEngine.engineId}@${normalizationEngine.engineVersion}, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.codePointCount > inspectionPolicy.maxCodePoints) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: accepted codePointCount exceeds policy. " +
                            "codePointCount=${lexicalProfile.codePointCount}, " +
                            "maxCodePoints=${inspectionPolicy.maxCodePoints}, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.longestIdentifierTokenCodePoints >
                inspectionPolicy.maxIdentifierTokenCodePoints
            ) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: accepted identifier token exceeds policy. " +
                            "longestIdentifierTokenCodePoints=${lexicalProfile.longestIdentifierTokenCodePoints}, " +
                            "maxIdentifierTokenCodePoints=${inspectionPolicy.maxIdentifierTokenCodePoints}, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.hasNullableMarker && !inspectionPolicy.allowNullableMarker) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: nullable marker accepted while policy forbids it. " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.hasStarProjection && !inspectionPolicy.allowStarProjection) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine contract violation: star projection accepted while policy forbids it. " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }
        }

        /**
         * Cheap ASCII-marker cross-check.
         *
         * This is not a full parser. It is a defensive consistency check between
         * adapter-issued lexical facts and obvious ASCII syntax markers. It does
         * not use Unicode classification and does not replace NormalizationEngine.
         */
        private fun requireLexicalProfileCrossCheck(
            rawValue: String,
            lexicalProfile: CanonicalTypeLexicalProfile,
        ) {
            val actualHasGenericDelimiters =
                rawValue.indexOf('<') >= 0 || rawValue.indexOf('>') >= 0
            val actualHasArraySuffix = rawValue.endsWith("[]")
            val actualHasNullableMarker = rawValue.indexOf('?') >= 0
            val actualHasStarProjection = rawValue.indexOf('*') >= 0

            if (lexicalProfile.hasGenericDelimiters != actualHasGenericDelimiters) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine lexical profile mismatch: hasGenericDelimiters. " +
                            "profile=${lexicalProfile.hasGenericDelimiters}, actual=$actualHasGenericDelimiters, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.hasArraySuffix != actualHasArraySuffix) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine lexical profile mismatch: hasArraySuffix. " +
                            "profile=${lexicalProfile.hasArraySuffix}, actual=$actualHasArraySuffix, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.hasNullableMarker != actualHasNullableMarker) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine lexical profile mismatch: hasNullableMarker. " +
                            "profile=${lexicalProfile.hasNullableMarker}, actual=$actualHasNullableMarker, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }

            if (lexicalProfile.hasStarProjection != actualHasStarProjection) {
                throw MetamodelFactContractViolationException(
                    "NormalizationEngine lexical profile mismatch: hasStarProjection. " +
                            "profile=${lexicalProfile.hasStarProjection}, actual=$actualHasStarProjection, " +
                            "value=${diagnosticSample(rawValue)}",
                )
            }
        }

        private fun requireEngineProvenance(
            normalizationEngine: NormalizationEngine,
        ) {
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

            if (value.contains('|')) {
                throw MetamodelFactContractViolationException(
                    "$field must not contain reserved delimiter '|': $value",
                )
            }
        }

        private fun buildRatificationProvenance(
            normalizationEngine: NormalizationEngine,
            inspectionPolicy: CanonicalTypeTextInspectionPolicy,
            lexicalProfile: CanonicalTypeLexicalProfile,
        ): String {
            return "CanonicalTypeText" +
                    "|engine=${normalizationEngine.engineId}" +
                    "|engineVersion=${normalizationEngine.engineVersion}" +
                    "|unicode=${normalizationEngine.unicodeProfileVersion}" +
                    "|policy=${inspectionPolicy.deterministicPolicyToken}" +
                    "|codePoints=${lexicalProfile.codePointCount}" +
                    "|identifierTokens=${lexicalProfile.identifierTokenCount}" +
                    "|longestIdentifier=${lexicalProfile.longestIdentifierTokenCodePoints}"
        }

        private fun diagnosticSample(value: String): String {
            return if (value.length <= 128) {
                value
            } else {
                value.substring(0, 128) + "...<truncated>"
            }
        }
    }
}
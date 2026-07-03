package realization.identity.derivation

import metamodel.domain.port.outgoing.PolicyFingerprintDeriver
import stage.canonicalization.contract.representative.CanonicalTextPolicyFingerprintSpec
import stage.canonicalization.material.representation.CanonicalTypeTextInspectionPolicy

/**
 * Domain service for issuing pinned canonical type-text inspection policies.
 *
 * This service is the lawful construction boundary for inspection policy
 * snapshots.
 *
 * It keeps policy issuance separate from:
 *
 * - NormalizationEngine;
 * - CanonicalTypeText;
 * - reflection adapters;
 * - KSP adapters;
 * - digest implementations;
 * - runtime profile selection.
 *
 * The caller chooses policy numbers at a stable governance boundary.
 * This service derives the order fingerprint through an outbound port and
 * returns an immutable policy value object.
 */
class CanonicalTypeTextInspectionPolicyIssuer(
    private val fingerprintDeriver: PolicyFingerprintDeriver,
) {
    fun issue(
        allowNullableMarker: Boolean,
        allowStarProjection: Boolean,
        maxCodePoints: Int,
        maxIdentifierTokenCodePoints: Int,
        maxDelimiterCodePoints: Int,
        maxNonIdentifierCodePointRatioBasisPoints: Int,
        maxGrossCombiningMarks: Int,
        maxCombiningMarksPerIdentifierToken: Int,
        maxGraphemeClustersPerIdentifierToken: Int,
        scriptPolicyToken: String,
        policyVersion: String,
    ): CanonicalTypeTextInspectionPolicy {
        val fields =
            listOf(
                "policyVersion" to policyVersion,
                "allowNullableMarker" to allowNullableMarker.toString(),
                "allowStarProjection" to allowStarProjection.toString(),
                "maxCodePoints" to maxCodePoints.toString(),
                "maxIdentifierTokenCodePoints" to maxIdentifierTokenCodePoints.toString(),
                "maxDelimiterCodePoints" to maxDelimiterCodePoints.toString(),
                "maxNonIdentifierCodePointRatioBasisPoints" to
                        maxNonIdentifierCodePointRatioBasisPoints.toString(),
                "maxGrossCombiningMarks" to maxGrossCombiningMarks.toString(),
                "maxCombiningMarksPerIdentifierToken" to
                        maxCombiningMarksPerIdentifierToken.toString(),
                "maxGraphemeClustersPerIdentifierToken" to
                        maxGraphemeClustersPerIdentifierToken.toString(),
                "scriptPolicyToken" to scriptPolicyToken,
            )

        val fingerprint =
            fingerprintDeriver.derive(
                purpose = CanonicalTextPolicyFingerprintSpec.PURPOSE,
                algorithmId = CanonicalTextPolicyFingerprintSpec.ALGORITHM_ID,
                algorithmVersion = CanonicalTextPolicyFingerprintSpec.FINGERPRINT_LAW_VERSION,
                encodingId = CanonicalTextPolicyFingerprintSpec.ENCODING_ID,
                fields = fields,
            )

        return CanonicalTypeTextInspectionPolicy.Companion.issueVerified(
            allowNullableMarker = allowNullableMarker,
            allowStarProjection = allowStarProjection,
            maxCodePoints = maxCodePoints,
            maxIdentifierTokenCodePoints = maxIdentifierTokenCodePoints,
            maxDelimiterCodePoints = maxDelimiterCodePoints,
            maxNonIdentifierCodePointRatioBasisPoints = maxNonIdentifierCodePointRatioBasisPoints,
            maxGrossCombiningMarks = maxGrossCombiningMarks,
            maxCombiningMarksPerIdentifierToken = maxCombiningMarksPerIdentifierToken,
            maxGraphemeClustersPerIdentifierToken = maxGraphemeClustersPerIdentifierToken,
            scriptPolicyToken = scriptPolicyToken,
            policyVersion = policyVersion,
            policyFingerprint = fingerprint,
        )
    }
}
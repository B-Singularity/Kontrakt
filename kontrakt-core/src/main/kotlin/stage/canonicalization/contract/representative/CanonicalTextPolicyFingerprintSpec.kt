package stage.canonicalization.contract.representative

/**
 * Protocol law for canonical type-text inspection policy fingerprints.
 *
 * This file defines governance-level fingerprint material.
 * It does NOT implement hashing.
 * It does NOT depend on java.security.MessageDigest.
 * It does NOT know reflection, KSP, ICU, JDK Unicode tables, or adapter details.
 *
 * Why this exists:
 *
 * - Policy identity must not be hidden inside ad hoc companion constants.
 * - Field order is order law.
 * - Encoding law is order law.
 * - Digest algorithm identity is order law.
 * - The actual digest primitive belongs to an adapter-side implementation of
 *   PolicyFingerprintDeriver.
 *
 * If any of the following changes, FINGERPRINT_LAW_VERSION must change:
 *
 * - field order
 * - field set
 * - field name spelling
 * - length-prefix material law
 * - encoding id
 * - digest algorithm id
 */
object CanonicalTextPolicyFingerprintSpec {
    const val PURPOSE: String =
        "canonical-type-text-inspection-policy"

    const val ALGORITHM_ID: String =
        "sha-256"

    /**
     * This version describes the fingerprint material law, not a JVM API.
     *
     * Do not encode implementation details such as "jdk-message-digest" here.
     * Those belong to the adapter implementation provenance.
     */
    const val FINGERPRINT_LAW_VERSION: String =
        "canonical-text-policy-sha256-length-prefixed-utf8-v1"

    const val ENCODING_ID: String =
        "utf-8"

    /**
     * Field order is order law.
     *
     * Do not reorder this list without changing FINGERPRINT_LAW_VERSION and
     * updating golden vectors.
     */
    val FIELD_ORDER: List<String> =
        listOf(
            "policyVersion",
            "allowNullableMarker",
            "allowStarProjection",
            "maxCodePoints",
            "maxIdentifierTokenCodePoints",
            "maxDelimiterCodePoints",
            "maxNonIdentifierCodePointRatioBasisPoints",
            "maxGrossCombiningMarks",
            "maxCombiningMarksPerIdentifierToken",
            "maxGraphemeClustersPerIdentifierToken",
            "scriptPolicyToken",
        )

    /**
     * Human-readable material law.
     *
     * The adapter-side deriver must encode every field as length-prefixed UTF-8
     * material. Delimiter-concatenation is forbidden because it allows ambiguous
     * material boundaries.
     */
    const val MATERIAL_LAW_DESCRIPTION: String =
        "length-prefixed ordered UTF-8 field material; no delimiter-based parsing"
}

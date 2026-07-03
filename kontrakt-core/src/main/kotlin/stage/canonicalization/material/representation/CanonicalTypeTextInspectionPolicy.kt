package stage.canonicalization.material.representation

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException

/**
 * Immutable canonical type-text inspection policy.
 *
 * This object contains already-issued policy values.
 *
 * It does not choose defaults.
 * It does not know runtime profile names.
 * It does not compute fingerprints.
 * It does not call MessageDigest.
 * It does not import reflection, KSP, ICU, or JDK Unicode classification APIs.
 *
 * Policy values must be issued at a stable runtime/metamodel policy boundary,
 * then pinned for the planning run or resolver scope that consumes them.
 *
 * This is inspection policy, not normalization policy.
 *
 * NFC-REJECT remains mandatory. The policy controls which already-NFC text
 * surfaces may become canonical type text.
 */
class CanonicalTypeTextInspectionPolicy private constructor(
    val allowNullableMarker: Boolean,
    val allowStarProjection: Boolean,
    val allowAsciiWhitespace: Boolean,
    val allowSourceVarianceTokens: Boolean,
    val maxCodePoints: Int,
    val maxIdentifierTokenCodePoints: Int,
    val maxIdentifierTokenCount: Int,
    val maxDelimiterCodePoints: Int,
    val maxGenericDepth: Int,
    val maxNonIdentifierCodePointRatioBasisPoints: Int,
    val maxGrossCombiningMarks: Int,
    val maxCombiningMarksPerIdentifierToken: Int,
    val maxGraphemeClustersPerIdentifierToken: Int,
    val scriptPolicyToken: String,
    val policyVersion: String,
    val policyFingerprint: PolicyFingerprint,
) {
    /**
     * Cheap first-line bound used before snapshot allocation and expensive
     * Unicode inspection.
     *
     * UTF-16 uses at most two code units per Unicode scalar value.
     */
    val maxUtf16CodeUnitsBeforeSnapshot: Int =
        safeMultiplyByTwo(maxCodePoints)

    /**
     * Fixed-size diagnostic token.
     *
     * Do not parse this string. It is a fingerprint display token, not policy
     * material.
     */
    val deterministicPolicyToken: String =
        policyFingerprint.renderProtocolToken()

    private val fingerprintFields: List<Pair<String, String>> =
        listOf(
            "policyVersion" to policyVersion,
            "allowNullableMarker" to allowNullableMarker.toString(),
            "allowStarProjection" to allowStarProjection.toString(),
            "allowAsciiWhitespace" to allowAsciiWhitespace.toString(),
            "allowSourceVarianceTokens" to allowSourceVarianceTokens.toString(),
            "maxCodePoints" to maxCodePoints.toString(),
            "maxIdentifierTokenCodePoints" to maxIdentifierTokenCodePoints.toString(),
            "maxIdentifierTokenCount" to maxIdentifierTokenCount.toString(),
            "maxDelimiterCodePoints" to maxDelimiterCodePoints.toString(),
            "maxGenericDepth" to maxGenericDepth.toString(),
            "maxNonIdentifierCodePointRatioBasisPoints" to
                    maxNonIdentifierCodePointRatioBasisPoints.toString(),
            "maxGrossCombiningMarks" to maxGrossCombiningMarks.toString(),
            "maxCombiningMarksPerIdentifierToken" to
                    maxCombiningMarksPerIdentifierToken.toString(),
            "maxGraphemeClustersPerIdentifierToken" to
                    maxGraphemeClustersPerIdentifierToken.toString(),
            "scriptPolicyToken" to scriptPolicyToken,
        )

    fun asFingerprintFields(): List<Pair<String, String>> {
        return fingerprintFields
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is CanonicalTypeTextInspectionPolicy) return false

        return allowNullableMarker == other.allowNullableMarker &&
                allowStarProjection == other.allowStarProjection &&
                allowAsciiWhitespace == other.allowAsciiWhitespace &&
                allowSourceVarianceTokens == other.allowSourceVarianceTokens &&
                maxCodePoints == other.maxCodePoints &&
                maxIdentifierTokenCodePoints == other.maxIdentifierTokenCodePoints &&
                maxIdentifierTokenCount == other.maxIdentifierTokenCount &&
                maxDelimiterCodePoints == other.maxDelimiterCodePoints &&
                maxGenericDepth == other.maxGenericDepth &&
                maxNonIdentifierCodePointRatioBasisPoints ==
                other.maxNonIdentifierCodePointRatioBasisPoints &&
                maxGrossCombiningMarks == other.maxGrossCombiningMarks &&
                maxCombiningMarksPerIdentifierToken ==
                other.maxCombiningMarksPerIdentifierToken &&
                maxGraphemeClustersPerIdentifierToken ==
                other.maxGraphemeClustersPerIdentifierToken &&
                scriptPolicyToken == other.scriptPolicyToken &&
                policyVersion == other.policyVersion &&
                policyFingerprint == other.policyFingerprint
    }

    override fun hashCode(): Int {
        var result = allowNullableMarker.hashCode()
        result = 31 * result + allowStarProjection.hashCode()
        result = 31 * result + allowAsciiWhitespace.hashCode()
        result = 31 * result + allowSourceVarianceTokens.hashCode()
        result = 31 * result + maxCodePoints
        result = 31 * result + maxIdentifierTokenCodePoints
        result = 31 * result + maxIdentifierTokenCount
        result = 31 * result + maxDelimiterCodePoints
        result = 31 * result + maxGenericDepth
        result = 31 * result + maxNonIdentifierCodePointRatioBasisPoints
        result = 31 * result + maxGrossCombiningMarks
        result = 31 * result + maxCombiningMarksPerIdentifierToken
        result = 31 * result + maxGraphemeClustersPerIdentifierToken
        result = 31 * result + scriptPolicyToken.hashCode()
        result = 31 * result + policyVersion.hashCode()
        result = 31 * result + policyFingerprint.hashCode()
        return result
    }

    override fun toString(): String {
        return deterministicPolicyToken
    }

    companion object {
        /**
         * Issues an already-fingerprinted inspection policy.
         *
         * The policy fingerprint is supplied by the stable policy boundary.
         * This class validates consistency, but does not compute a digest.
         */
        @JvmStatic
        fun issueVerified(
            allowNullableMarker: Boolean,
            allowStarProjection: Boolean,
            allowAsciiWhitespace: Boolean,
            allowSourceVarianceTokens: Boolean,
            maxCodePoints: Int,
            maxIdentifierTokenCodePoints: Int,
            maxIdentifierTokenCount: Int,
            maxDelimiterCodePoints: Int,
            maxGenericDepth: Int,
            maxNonIdentifierCodePointRatioBasisPoints: Int,
            maxGrossCombiningMarks: Int,
            maxCombiningMarksPerIdentifierToken: Int,
            maxGraphemeClustersPerIdentifierToken: Int,
            scriptPolicyToken: String,
            policyVersion: String,
            policyFingerprint: PolicyFingerprint,
        ): CanonicalTypeTextInspectionPolicy {
            policyFingerprint.requireCanonicalTextPolicySpec()

            requirePositive("maxCodePoints", maxCodePoints)
            requirePositive("maxIdentifierTokenCodePoints", maxIdentifierTokenCodePoints)
            requirePositive("maxIdentifierTokenCount", maxIdentifierTokenCount)
            requireNonNegative("maxDelimiterCodePoints", maxDelimiterCodePoints)
            requirePositive("maxGenericDepth", maxGenericDepth)
            requireRatioBasisPoints(
                field = "maxNonIdentifierCodePointRatioBasisPoints",
                value = maxNonIdentifierCodePointRatioBasisPoints,
            )
            requireNonNegative("maxGrossCombiningMarks", maxGrossCombiningMarks)
            requireNonNegative(
                field = "maxCombiningMarksPerIdentifierToken",
                value = maxCombiningMarksPerIdentifierToken,
            )
            requirePositive(
                field = "maxGraphemeClustersPerIdentifierToken",
                value = maxGraphemeClustersPerIdentifierToken,
            )
            requirePolicyComponent("scriptPolicyToken", scriptPolicyToken)
            requirePolicyComponent("policyVersion", policyVersion)

            if (maxIdentifierTokenCodePoints > maxCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxIdentifierTokenCodePoints must be <= maxCodePoints: " +
                            "maxIdentifierTokenCodePoints=$maxIdentifierTokenCodePoints, " +
                            "maxCodePoints=$maxCodePoints",
                )
            }

            if (maxIdentifierTokenCount > maxCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxIdentifierTokenCount must be <= maxCodePoints: " +
                            "maxIdentifierTokenCount=$maxIdentifierTokenCount, " +
                            "maxCodePoints=$maxCodePoints",
                )
            }

            if (maxDelimiterCodePoints > maxCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxDelimiterCodePoints must be <= maxCodePoints: " +
                            "maxDelimiterCodePoints=$maxDelimiterCodePoints, " +
                            "maxCodePoints=$maxCodePoints",
                )
            }

            if (maxGenericDepth > maxCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxGenericDepth must be <= maxCodePoints: " +
                            "maxGenericDepth=$maxGenericDepth, maxCodePoints=$maxCodePoints",
                )
            }

            if (maxGrossCombiningMarks > maxCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxGrossCombiningMarks must be <= maxCodePoints: " +
                            "maxGrossCombiningMarks=$maxGrossCombiningMarks, " +
                            "maxCodePoints=$maxCodePoints",
                )
            }

            if (maxCombiningMarksPerIdentifierToken > maxGrossCombiningMarks) {
                throw MetamodelFactContractViolationException(
                    "maxCombiningMarksPerIdentifierToken must be <= maxGrossCombiningMarks: " +
                            "maxCombiningMarksPerIdentifierToken=$maxCombiningMarksPerIdentifierToken, " +
                            "maxGrossCombiningMarks=$maxGrossCombiningMarks",
                )
            }

            if (maxGraphemeClustersPerIdentifierToken > maxIdentifierTokenCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxGraphemeClustersPerIdentifierToken must be <= maxIdentifierTokenCodePoints: " +
                            "maxGraphemeClustersPerIdentifierToken=$maxGraphemeClustersPerIdentifierToken, " +
                            "maxIdentifierTokenCodePoints=$maxIdentifierTokenCodePoints",
                )
            }

            return CanonicalTypeTextInspectionPolicy(
                allowNullableMarker = allowNullableMarker,
                allowStarProjection = allowStarProjection,
                allowAsciiWhitespace = allowAsciiWhitespace,
                allowSourceVarianceTokens = allowSourceVarianceTokens,
                maxCodePoints = maxCodePoints,
                maxIdentifierTokenCodePoints = maxIdentifierTokenCodePoints,
                maxIdentifierTokenCount = maxIdentifierTokenCount,
                maxDelimiterCodePoints = maxDelimiterCodePoints,
                maxGenericDepth = maxGenericDepth,
                maxNonIdentifierCodePointRatioBasisPoints =
                    maxNonIdentifierCodePointRatioBasisPoints,
                maxGrossCombiningMarks = maxGrossCombiningMarks,
                maxCombiningMarksPerIdentifierToken =
                    maxCombiningMarksPerIdentifierToken,
                maxGraphemeClustersPerIdentifierToken =
                    maxGraphemeClustersPerIdentifierToken,
                scriptPolicyToken = scriptPolicyToken,
                policyVersion = policyVersion,
                policyFingerprint = policyFingerprint,
            )
        }

        private fun safeMultiplyByTwo(
            value: Int,
        ): Int {
            if (value > Int.MAX_VALUE / 2) {
                throw MetamodelFactContractViolationException(
                    "maxCodePoints too large to derive UTF-16 early-exit guard: $value",
                )
            }

            return value * 2
        }

        private fun requirePositive(
            field: String,
            value: Int,
        ) {
            if (value <= 0) {
                throw MetamodelFactContractViolationException(
                    "$field must be > 0: $value",
                )
            }
        }

        private fun requireNonNegative(
            field: String,
            value: Int,
        ) {
            if (value < 0) {
                throw MetamodelFactContractViolationException(
                    "$field must be >= 0: $value",
                )
            }
        }

        private fun requireRatioBasisPoints(
            field: String,
            value: Int,
        ) {
            if (value < 0 || value > 10_000) {
                throw MetamodelFactContractViolationException(
                    "$field must be within [0, 10000]: $value",
                )
            }
        }

        private fun requirePolicyComponent(
            field: String,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "$field must not be empty.",
                )
            }

            var index = 0
            while (index < value.length) {
                val c = value[index]
                val code = c.code

                val isControl =
                    code in 0x0000..0x001F ||
                            code in 0x007F..0x009F

                if (isControl || c == '|') {
                    throw MetamodelFactContractViolationException(
                        "$field must not contain order/control material: index=$index",
                    )
                }

                index += 1
            }
        }
    }
}
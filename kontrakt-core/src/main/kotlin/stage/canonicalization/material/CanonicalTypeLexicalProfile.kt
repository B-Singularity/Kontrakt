package stage.canonicalization.material

import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Deterministic lexical facts discovered during canonical type-text inspection.
 *
 * This is not a full type parser.
 * This is not a type classifier.
 * This is not a reflection/KSP artifact.
 *
 * It is the adapter-issued inspection proof for one immutable snapshot returned
 * by NormalizationEngine.
 */
class CanonicalTypeLexicalProfile private constructor(
    val isNfc: Boolean,
    val utf16CodeUnitCount: Int,
    val codePointCount: Int,
    val identifierTokenCount: Int,
    val longestIdentifierTokenCodePoints: Int,
    val totalDelimiterCodePoints: Int,
    val nonIdentifierCodePointCount: Int,
    val grossCombiningMarkCount: Int,
    val maxCombiningMarksPerIdentifierToken: Int,
    val maxGraphemeClustersPerIdentifierToken: Int,
    val genericDepth: Int,
    val hasGenericDelimiters: Boolean,
    val hasArraySuffix: Boolean,
    val hasNullableMarker: Boolean,
    val hasStarProjection: Boolean,
    val hasAsciiWhitespace: Boolean,
    val hasSourceVarianceToken: Boolean,
) {
    fun requireWithinPolicy(
        policy: CanonicalTypeTextInspectionPolicy,
    ) {
        if (!isNfc) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile must be NFC for accepted type text.",
            )
        }

        if (utf16CodeUnitCount > policy.maxUtf16CodeUnitsBeforeSnapshot) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.utf16CodeUnitCount exceeds policy: " +
                        "utf16CodeUnitCount=$utf16CodeUnitCount, " +
                        "maxUtf16CodeUnitsBeforeSnapshot=${policy.maxUtf16CodeUnitsBeforeSnapshot}",
            )
        }

        if (codePointCount > policy.maxCodePoints) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.codePointCount exceeds policy: " +
                        "codePointCount=$codePointCount, maxCodePoints=${policy.maxCodePoints}",
            )
        }

        if (identifierTokenCount > policy.maxIdentifierTokenCount) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.identifierTokenCount exceeds policy: " +
                        "identifierTokenCount=$identifierTokenCount, " +
                        "maxIdentifierTokenCount=${policy.maxIdentifierTokenCount}",
            )
        }

        if (longestIdentifierTokenCodePoints > policy.maxIdentifierTokenCodePoints) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.longestIdentifierTokenCodePoints exceeds policy: " +
                        "longestIdentifierTokenCodePoints=$longestIdentifierTokenCodePoints, " +
                        "maxIdentifierTokenCodePoints=${policy.maxIdentifierTokenCodePoints}",
            )
        }

        if (totalDelimiterCodePoints > policy.maxDelimiterCodePoints) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.totalDelimiterCodePoints exceeds policy: " +
                        "totalDelimiterCodePoints=$totalDelimiterCodePoints, " +
                        "maxDelimiterCodePoints=${policy.maxDelimiterCodePoints}",
            )
        }

        if (genericDepth > policy.maxGenericDepth) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.genericDepth exceeds policy: " +
                        "genericDepth=$genericDepth, maxGenericDepth=${policy.maxGenericDepth}",
            )
        }

        if (grossCombiningMarkCount > policy.maxGrossCombiningMarks) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.grossCombiningMarkCount exceeds policy: " +
                        "grossCombiningMarkCount=$grossCombiningMarkCount, " +
                        "maxGrossCombiningMarks=${policy.maxGrossCombiningMarks}",
            )
        }

        if (
            maxCombiningMarksPerIdentifierToken >
            policy.maxCombiningMarksPerIdentifierToken
        ) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.maxCombiningMarksPerIdentifierToken exceeds policy: " +
                        "maxCombiningMarksPerIdentifierToken=$maxCombiningMarksPerIdentifierToken, " +
                        "maxAllowed=${policy.maxCombiningMarksPerIdentifierToken}",
            )
        }

        if (
            maxGraphemeClustersPerIdentifierToken >
            policy.maxGraphemeClustersPerIdentifierToken
        ) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile.maxGraphemeClustersPerIdentifierToken exceeds policy: " +
                        "maxGraphemeClustersPerIdentifierToken=$maxGraphemeClustersPerIdentifierToken, " +
                        "maxAllowed=${policy.maxGraphemeClustersPerIdentifierToken}",
            )
        }

        val ratioBasisPoints =
            ((nonIdentifierCodePointCount.toLong() * 10_000L) / codePointCount.toLong()).toInt()

        if (ratioBasisPoints > policy.maxNonIdentifierCodePointRatioBasisPoints) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile non-identifier density exceeds policy: " +
                        "ratioBasisPoints=$ratioBasisPoints, " +
                        "maxRatioBasisPoints=${policy.maxNonIdentifierCodePointRatioBasisPoints}",
            )
        }

        if (hasNullableMarker && !policy.allowNullableMarker) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile reports nullable marker while policy forbids it.",
            )
        }

        if (hasStarProjection && !policy.allowStarProjection) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile reports star projection while policy forbids it.",
            )
        }

        if (hasAsciiWhitespace && !policy.allowAsciiWhitespace) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile reports ASCII whitespace while policy forbids it.",
            )
        }

        if (hasSourceVarianceToken && !policy.allowSourceVarianceTokens) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeLexicalProfile reports source variance token while policy forbids it.",
            )
        }
    }

    companion object {
        @JvmStatic
        fun accepted(
            isNfc: Boolean,
            utf16CodeUnitCount: Int,
            codePointCount: Int,
            identifierTokenCount: Int,
            longestIdentifierTokenCodePoints: Int,
            totalDelimiterCodePoints: Int,
            nonIdentifierCodePointCount: Int,
            grossCombiningMarkCount: Int,
            maxCombiningMarksPerIdentifierToken: Int,
            maxGraphemeClustersPerIdentifierToken: Int,
            genericDepth: Int,
            hasGenericDelimiters: Boolean,
            hasArraySuffix: Boolean,
            hasNullableMarker: Boolean,
            hasStarProjection: Boolean,
            hasAsciiWhitespace: Boolean,
            hasSourceVarianceToken: Boolean,
        ): CanonicalTypeLexicalProfile {
            if (!isNfc) {
                throw MetamodelFactContractViolationException(
                    "Accepted CanonicalTypeLexicalProfile must be NFC.",
                )
            }

            if (utf16CodeUnitCount <= 0) {
                throw MetamodelFactContractViolationException(
                    "utf16CodeUnitCount must be > 0: $utf16CodeUnitCount",
                )
            }

            if (codePointCount <= 0) {
                throw MetamodelFactContractViolationException(
                    "codePointCount must be > 0: $codePointCount",
                )
            }

            if (identifierTokenCount <= 0) {
                throw MetamodelFactContractViolationException(
                    "identifierTokenCount must be > 0 for accepted type text: $identifierTokenCount",
                )
            }

            if (longestIdentifierTokenCodePoints <= 0) {
                throw MetamodelFactContractViolationException(
                    "longestIdentifierTokenCodePoints must be > 0 for accepted type text: " +
                            longestIdentifierTokenCodePoints,
                )
            }

            if (longestIdentifierTokenCodePoints > codePointCount) {
                throw MetamodelFactContractViolationException(
                    "longestIdentifierTokenCodePoints must be <= codePointCount: " +
                            "longest=$longestIdentifierTokenCodePoints, codePointCount=$codePointCount",
                )
            }

            if (totalDelimiterCodePoints < 0) {
                throw MetamodelFactContractViolationException(
                    "totalDelimiterCodePoints must be >= 0: $totalDelimiterCodePoints",
                )
            }

            if (nonIdentifierCodePointCount < 0) {
                throw MetamodelFactContractViolationException(
                    "nonIdentifierCodePointCount must be >= 0: $nonIdentifierCodePointCount",
                )
            }

            if (grossCombiningMarkCount < 0) {
                throw MetamodelFactContractViolationException(
                    "grossCombiningMarkCount must be >= 0: $grossCombiningMarkCount",
                )
            }

            if (maxCombiningMarksPerIdentifierToken < 0) {
                throw MetamodelFactContractViolationException(
                    "maxCombiningMarksPerIdentifierToken must be >= 0: " +
                            maxCombiningMarksPerIdentifierToken,
                )
            }

            if (maxGraphemeClustersPerIdentifierToken <= 0) {
                throw MetamodelFactContractViolationException(
                    "maxGraphemeClustersPerIdentifierToken must be > 0: " +
                            maxGraphemeClustersPerIdentifierToken,
                )
            }

            if (genericDepth < 0) {
                throw MetamodelFactContractViolationException(
                    "genericDepth must be >= 0: $genericDepth",
                )
            }

            if (totalDelimiterCodePoints > nonIdentifierCodePointCount) {
                throw MetamodelFactContractViolationException(
                    "totalDelimiterCodePoints must be <= nonIdentifierCodePointCount: " +
                            "totalDelimiterCodePoints=$totalDelimiterCodePoints, " +
                            "nonIdentifierCodePointCount=$nonIdentifierCodePointCount",
                )
            }

            if (nonIdentifierCodePointCount > codePointCount) {
                throw MetamodelFactContractViolationException(
                    "nonIdentifierCodePointCount must be <= codePointCount: " +
                            "nonIdentifierCodePointCount=$nonIdentifierCodePointCount, " +
                            "codePointCount=$codePointCount",
                )
            }

            if (grossCombiningMarkCount > nonIdentifierCodePointCount) {
                throw MetamodelFactContractViolationException(
                    "grossCombiningMarkCount must be <= nonIdentifierCodePointCount: " +
                            "grossCombiningMarkCount=$grossCombiningMarkCount, " +
                            "nonIdentifierCodePointCount=$nonIdentifierCodePointCount",
                )
            }

            if (
                grossCombiningMarkCount > 0 &&
                maxCombiningMarksPerIdentifierToken > grossCombiningMarkCount
            ) {
                throw MetamodelFactContractViolationException(
                    "maxCombiningMarksPerIdentifierToken cannot exceed grossCombiningMarkCount " +
                            "when grossCombiningMarkCount > 0.",
                )
            }

            if (maxGraphemeClustersPerIdentifierToken > longestIdentifierTokenCodePoints) {
                throw MetamodelFactContractViolationException(
                    "maxGraphemeClustersPerIdentifierToken must be <= longestIdentifierTokenCodePoints.",
                )
            }

            return CanonicalTypeLexicalProfile(
                isNfc = true,
                utf16CodeUnitCount = utf16CodeUnitCount,
                codePointCount = codePointCount,
                identifierTokenCount = identifierTokenCount,
                longestIdentifierTokenCodePoints = longestIdentifierTokenCodePoints,
                totalDelimiterCodePoints = totalDelimiterCodePoints,
                nonIdentifierCodePointCount = nonIdentifierCodePointCount,
                grossCombiningMarkCount = grossCombiningMarkCount,
                maxCombiningMarksPerIdentifierToken = maxCombiningMarksPerIdentifierToken,
                maxGraphemeClustersPerIdentifierToken = maxGraphemeClustersPerIdentifierToken,
                genericDepth = genericDepth,
                hasGenericDelimiters = hasGenericDelimiters,
                hasArraySuffix = hasArraySuffix,
                hasNullableMarker = hasNullableMarker,
                hasStarProjection = hasStarProjection,
                hasAsciiWhitespace = hasAsciiWhitespace,
                hasSourceVarianceToken = hasSourceVarianceToken,
            )
        }
    }
}
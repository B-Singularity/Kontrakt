package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

/**
 * Deterministic proof token for type-shape ratification.
 *
 * This value is not:
 *
 * - hashCode caching;
 * - interning;
 * - cache identity;
 * - session identity;
 * - diagnostic text;
 * - or runtime handle identity.
 *
 * It is a compact proof token produced under a pinned ratification law.
 *
 * Security law:
 *
 * The fingerprint must be derived from at least:
 *
 * - CanonicalTypeText.value;
 * - TypeShapeSummary protocol material;
 * - classifierId;
 * - classifierVersion;
 * - ratification law version.
 *
 * The fingerprint must not contain:
 *
 * - session nonce;
 * - wall-clock time;
 * - random salt;
 * - adapter object identity;
 * - ClassLoader identity.
 *
 * If scope/session isolation is required, carry that outside this value object.
 *
 * Encoding law:
 *
 * The fingerprint value is accepted only under an explicit FingerprintTokenEncoding.
 * Do not infer the encoding from the token shape.
 *
 * Preferred encodings:
 *
 * - LOWER_HEX for simple digest interoperability;
 * - BASE64_URL_NO_PADDING for compact protocol-safe tokens.
 *
 * Standard Base64 is supported only when explicitly declared, because '+', '/',
 * and '=' are less convenient for logs, URLs, and filesystem-safe protocol
 * material. The value is still redacted from diagnostics.
 *
 * Algorithm id law:
 *
 * algorithmId is canonicalized to lowercase ASCII protocol form at issue time.
 * For example, "SHA-256" and "sha-256" become the same algorithm id.
 *
 * algorithmVersion is not silently normalized because it represents a protocol
 * law version. It must already be supplied as a clean protocol token.
 */
class TypeShapeRatificationFingerprint private constructor(
    val algorithmId: String,
    val algorithmVersion: String,
    val valueEncoding: FingerprintTokenEncoding,
    val value: String,
) {
    fun redacted(): String =
        "TypeShapeRatificationFingerprint(" +
            "algorithm=$algorithmId@$algorithmVersion, " +
            "encoding=${valueEncoding.protocolToken}, " +
            "value=<redacted>" +
            ")"

    fun requireAlgorithm(
        expectedAlgorithmId: String,
        expectedAlgorithmVersion: String,
    ) {
        val expectedCanonicalAlgorithmId = canonicalizeAlgorithmId(expectedAlgorithmId)

        if (
            algorithmId != expectedCanonicalAlgorithmId ||
            algorithmVersion != expectedAlgorithmVersion
        ) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatificationFingerprint algorithm mismatch: " +
                    "expected=$expectedCanonicalAlgorithmId@$expectedAlgorithmVersion, " +
                    "actual=$algorithmId@$algorithmVersion",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeRatificationFingerprint) return false

        return algorithmId == other.algorithmId &&
            algorithmVersion == other.algorithmVersion &&
            valueEncoding == other.valueEncoding &&
            value == other.value
    }

    override fun hashCode(): Int {
        var result = algorithmId.hashCode()
        result = 31 * result + algorithmVersion.hashCode()
        result = 31 * result + valueEncoding.protocolOrder
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String = redacted()

    companion object {
        private const val MAX_FINGERPRINT_TOKEN_CHARS: Int = 512

        @JvmStatic
        fun issue(
            algorithmId: String,
            algorithmVersion: String,
            valueEncoding: FingerprintTokenEncoding,
            value: String,
        ): TypeShapeRatificationFingerprint {
            /*
             * Validate raw algorithmId before canonicalization.
             *
             * This keeps the construction order explicit:
             *
             * 1. reject protocol/control contamination;
             * 2. canonicalize ASCII algorithm spelling;
             * 3. validate the canonical token surface;
             * 4. validate algorithmVersion;
             * 5. validate fingerprint value under declared encoding.
             */
            requireProtocolComponent(
                field = "algorithmId",
                value = algorithmId,
            )

            val canonicalAlgorithmId = canonicalizeAlgorithmId(algorithmId)

            requireProtocolComponent(
                field = "algorithmId",
                value = canonicalAlgorithmId,
            )
            requireProtocolComponent(
                field = "algorithmVersion",
                value = algorithmVersion,
            )
            requireFingerprintValue(
                encoding = valueEncoding,
                value = value,
            )

            return TypeShapeRatificationFingerprint(
                algorithmId = canonicalAlgorithmId,
                algorithmVersion = algorithmVersion,
                valueEncoding = valueEncoding,
                value = value,
            )
        }

        private fun canonicalizeAlgorithmId(value: String): String {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "algorithmId must not be empty.",
                )
            }

            var requiresLowercaseCopy = false

            /*
             * First pass:
             *
             * - validate the allowed ASCII protocol surface;
             * - detect whether a lowercase copy is actually required.
             *
             * This avoids allocating StringBuilder for the common already-canonical
             * path such as "sha-256" or "blake3".
             */
            for (index in value.indices) {
                val c = value[index]

                when (c) {
                    in 'A'..'Z' -> {
                        requiresLowercaseCopy = true
                    }

                    in 'a'..'z',
                    in '0'..'9',
                    '-',
                    '_',
                    '.',
                    -> {
                        // Already canonical-safe ASCII protocol material.
                    }

                    else -> {
                        throw MetamodelFactContractViolationException(
                            "algorithmId contains a non-canonical protocol character.",
                        )
                    }
                }
            }

            if (!requiresLowercaseCopy) {
                return value
            }

            val builder = StringBuilder(value.length)

            for (index in value.indices) {
                val c = value[index]
                val lowered =
                    if (c in 'A'..'Z') {
                        (c.code + 32).toChar()
                    } else {
                        c
                    }

                builder.append(lowered)
            }

            return builder.toString()
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

        private fun requireFingerprintValue(
            encoding: FingerprintTokenEncoding,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeRatificationFingerprint.value must not be empty.",
                )
            }

            if (value.indexOf('|') >= 0) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeRatificationFingerprint.value contains reserved protocol delimiter.",
                )
            }

            if (value.length > MAX_FINGERPRINT_TOKEN_CHARS) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeRatificationFingerprint.value exceeds the maximum token length.",
                )
            }

            when (encoding) {
                FingerprintTokenEncoding.LOWER_HEX -> {
                    requireLowerHex(value)
                }

                FingerprintTokenEncoding.BASE64_URL_NO_PADDING -> {
                    requireBase64UrlNoPadding(value)
                }

                FingerprintTokenEncoding.BASE64_STANDARD_PADDED -> {
                    requireStandardBase64PaddedSurface(value)
                }
            }
        }

        private fun requireLowerHex(value: String) {
            if (value.length % 2 != 0) {
                throw MetamodelFactContractViolationException(
                    "LOWER_HEX fingerprint value must have even length.",
                )
            }

            for (index in value.indices) {
                val c = value[index]
                val ok = c in '0'..'9' || c in 'a'..'f'

                if (!ok) {
                    throw MetamodelFactContractViolationException(
                        "LOWER_HEX fingerprint value contains a non-lowercase-hex character.",
                    )
                }
            }
        }

        private fun requireBase64UrlNoPadding(value: String) {
            if (value.indexOf('=') >= 0) {
                throw MetamodelFactContractViolationException(
                    "BASE64_URL_NO_PADDING fingerprint value must not contain padding.",
                )
            }

            for (index in value.indices) {
                val c = value[index]
                val ok =
                    c in 'A'..'Z' ||
                        c in 'a'..'z' ||
                        c in '0'..'9' ||
                        c == '-' ||
                        c == '_'

                if (!ok) {
                    throw MetamodelFactContractViolationException(
                        "BASE64_URL_NO_PADDING fingerprint value contains an invalid character.",
                    )
                }
            }
        }

        /**
         * ASCII-surface validation for standard padded Base64.
         *
         * This method intentionally does not perform byte-level Base64 decoding or
         * canonical padding-bit validation. That belongs to the adapter-side
         * TypeShapeRatificationVerifier / fingerprint deriver implementation, where
         * the digest primitive and encoding implementation are already outside the
         * metamodel domain core.
         *
         * Domain responsibility here:
         *
         * - reject invalid transport characters;
         * - reject malformed padding placement;
         * - reject impossible padded length shape.
         *
         * Adapter/verifier responsibility:
         *
         * - decode if the selected algorithm law requires it;
         * - verify canonical Base64 pad bits;
         * - verify digest/HMAC byte length;
         * - verify the fingerprint is bound to the ratification tuple.
         */
        private fun requireStandardBase64PaddedSurface(value: String) {
            if (value.length % 4 != 0) {
                throw MetamodelFactContractViolationException(
                    "BASE64_STANDARD_PADDED fingerprint value must have length divisible by 4.",
                )
            }

            var paddingStarted = false
            var paddingCount = 0

            for (index in value.indices) {
                val c = value[index]

                if (c == '=') {
                    paddingStarted = true
                    paddingCount += 1
                    continue
                }

                if (paddingStarted) {
                    throw MetamodelFactContractViolationException(
                        "BASE64_STANDARD_PADDED fingerprint value contains non-padding data after padding.",
                    )
                }

                val ok =
                    c in 'A'..'Z' ||
                        c in 'a'..'z' ||
                        c in '0'..'9' ||
                        c == '+' ||
                        c == '/'

                if (!ok) {
                    throw MetamodelFactContractViolationException(
                        "BASE64_STANDARD_PADDED fingerprint value contains an invalid character.",
                    )
                }
            }

            if (paddingCount > 2) {
                throw MetamodelFactContractViolationException(
                    "BASE64_STANDARD_PADDED fingerprint value contains invalid padding.",
                )
            }
        }
    }
}

/**
 * Explicit encoding vocabulary for fingerprint token values.
 *
 * Do not infer this from token shape.
 * Do not use enum ordinal.
 */
enum class FingerprintTokenEncoding(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    /**
     * Lowercase hexadecimal token.
     *
     * Preferred when human diffability and digest-tool interoperability matter.
     */
    LOWER_HEX(
        protocolOrder = 10,
        protocolToken = "lower_hex",
    ),

    /**
     * URL-safe Base64 without padding.
     *
     * Preferred compact encoding for protocol-safe token transport.
     */
    BASE64_URL_NO_PADDING(
        protocolOrder = 20,
        protocolToken = "base64_url_no_padding",
    ),

    /**
     * Standard padded Base64.
     *
     * Supported only for explicit interop. Less preferred because '+', '/', and
     * '=' are awkward in URLs, paths, shell snippets, and some log pipelines.
     */
    BASE64_STANDARD_PADDED(
        protocolOrder = 30,
        protocolToken = "base64_standard_padded",
    ),
}

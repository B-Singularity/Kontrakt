package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

/**
 * Result returned by the pinned NormalizationEngine inspection.
 */
sealed interface CanonicalTypeTextInspectionResult {
    /**
     * Accepted inspection proof.
     *
     * snapshot is the immutable text that was actually inspected.
     *
     * Domain code MUST consume this snapshot and MUST NOT go back to the
     * original CharSequence.
     */
    class Accepted(
        val snapshot: String,
        val lexicalProfile: CanonicalTypeLexicalProfile,
    ) : CanonicalTypeTextInspectionResult {
        init {
            if (snapshot.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "Accepted CanonicalTypeTextInspectionResult.snapshot must not be empty.",
                )
            }

            if (snapshot.length != lexicalProfile.utf16CodeUnitCount) {
                throw MetamodelFactContractViolationException(
                    "Accepted snapshot length must match lexicalProfile.utf16CodeUnitCount: " +
                        "snapshot.length=${snapshot.length}, " +
                        "profile.utf16CodeUnitCount=${lexicalProfile.utf16CodeUnitCount}",
                )
            }

            if (!lexicalProfile.isNfc) {
                throw MetamodelFactContractViolationException(
                    "Accepted CanonicalTypeTextInspectionResult must carry an NFC lexical profile.",
                )
            }
        }
    }

    /**
     * Rejected inspection result.
     *
     * violationCode is intentionally machine-readable. Human diagnostics may use
     * reason, but policy logic should branch on violationCode.
     */
    class Rejected(
        val violationCode: CanonicalTypeTextViolationCode,
        val reason: String,
    ) : CanonicalTypeTextInspectionResult {
        init {
            if (reason.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeTextInspectionResult.Rejected.reason must not be empty.",
                )
            }
        }
    }
}

/**
 * Machine-readable canonical type-text rejection code.
 *
 * Keep this vocabulary stable. Add new codes explicitly; never overload UNKNOWN
 * for known policy failures.
 */
enum class CanonicalTypeTextViolationCode(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    EMPTY_INPUT(10, "empty_input"),
    LENGTH_LIMIT_EXCEEDED(20, "length_limit_exceeded"),
    IDENTIFIER_TOKEN_LIMIT_EXCEEDED(30, "identifier_token_limit_exceeded"),
    DELIMITER_LIMIT_EXCEEDED(40, "delimiter_limit_exceeded"),
    NON_IDENTIFIER_DENSITY_EXCEEDED(50, "non_identifier_density_exceeded"),
    GROSS_COMBINING_MARK_LIMIT_EXCEEDED(60, "gross_combining_mark_limit_exceeded"),
    COMBINING_MARK_LIMIT_EXCEEDED(70, "combining_mark_limit_exceeded"),
    GRAPHEME_CLUSTER_LIMIT_EXCEEDED(80, "grapheme_cluster_limit_exceeded"),
    NON_NFC(90, "non_nfc"),
    INVALID_SURROGATE(100, "invalid_surrogate"),
    INVALID_SCALAR(110, "invalid_scalar"),
    FORBIDDEN_UNICODE_CATEGORY(120, "forbidden_unicode_category"),
    FORBIDDEN_WHITESPACE(130, "forbidden_whitespace"),
    FORBIDDEN_SCRIPT(140, "forbidden_script"),
    FORBIDDEN_INVISIBLE_OR_BIDI(150, "forbidden_invisible_or_bidi"),
    RESERVED_DELIMITER(160, "reserved_delimiter"),
    JVM_DESCRIPTOR_SYNTAX(170, "jvm_descriptor_syntax"),
    JVM_INTERNAL_NAME_SYNTAX(180, "jvm_internal_name_syntax"),
    JVM_BINARY_NAME_SYNTAX(190, "jvm_binary_name_syntax"),
    RAW_NULLABLE_MARKER(200, "raw_nullable_marker"),
    RAW_STAR_PROJECTION(210, "raw_star_projection"),
    SOURCE_VARIANCE_TOKEN(220, "source_variance_token"),
    MALFORMED_GENERIC_SYNTAX(230, "malformed_generic_syntax"),
    POLICY_CONTRACT_VIOLATION(240, "policy_contract_violation"),
    UNKNOWN(10_000, "unknown"),
}

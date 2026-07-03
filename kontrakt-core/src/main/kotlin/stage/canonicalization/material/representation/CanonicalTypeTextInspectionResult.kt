package stage.canonicalization.material.representation

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.contract.representative.MetamodelProtocolTextGuards

/**
 * Result of pinned canonical type-text inspection.
 *
 * This result is returned only by [metamodel.port.outgoing.NormalizationEngine].
 *
 * It is the metamodel-domain boundary result for converting high-entropy
 * adapter/source/reflection/KSP text into either:
 *
 * - an immutable, inspected, NFC-confirmed snapshot; or
 * - a machine-readable rejection code with bounded diagnostic text.
 *
 * This is not:
 *
 * - a type parser result;
 * - a type descriptor;
 * - a reflection/KSP handle;
 * - a canonical IR node;
 * - a canonical byte-encoding result;
 * - a source-language AST result;
 * - or a human-facing error object.
 *
 * Authority law:
 *
 * [Accepted] means the engine has inspected one exact immutable [String]
 * snapshot and derived [CanonicalTypeLexicalProfile] from that same snapshot.
 *
 * Domain code that receives [Accepted] MUST consume [Accepted.snapshot].
 * It MUST NOT return to the original raw [CharSequence] supplied to the engine.
 *
 * [Rejected] means the engine refused to ratify the input under the supplied
 * [CanonicalTypeTextInspectionPolicy].
 *
 * Rejection classification law:
 *
 * [Rejected.violationCode] is the stable machine-readable classification.
 * [Rejected.reason] is diagnostic prose only.
 *
 * Domain logic MUST branch on [Rejected.violationCode], not on [Rejected.reason].
 *
 * Diagnostic safety law:
 *
 * [Rejected.reason] is bounded and control-character-free because it may appear
 * in logs, traces, test reports, or failure summaries.
 *
 * It must not become a log-injection surface.
 *
 * Layering law:
 *
 * This type must not import ICU4J, JDK normalization APIs, reflection, KSP,
 * bytecode libraries, or adapter-local handles.
 */
sealed interface CanonicalTypeTextInspectionResult {
    /**
     * Successful canonical type-text inspection proof.
     *
     * [snapshot] is the exact immutable string inspected by the engine.
     *
     * [lexicalProfile] contains lexical facts derived from [snapshot].
     *
     * Invariants:
     *
     * - [snapshot] is non-empty;
     * - [snapshot.length] matches [lexicalProfile.utf16CodeUnitCount];
     * - [lexicalProfile.isNfc] is true;
     * - profile-policy validation is performed later by
     *   `CanonicalTypeText.ratify(...)` through
     *   `lexicalProfile.requireWithinPolicy(policy)`.
     *
     * This object does not itself know the policy used for inspection.
     *
     * Reason:
     *
     * The result proves engine inspection output shape.
     * Policy cross-checking remains the responsibility of the ratification
     * boundary, where policy and engine provenance are both visible.
     */
    class Accepted private constructor(
        val snapshot: String,
        val lexicalProfile: CanonicalTypeLexicalProfile,
    ) : CanonicalTypeTextInspectionResult {
        companion object {
            /**
             * Issues an accepted inspection result.
             *
             * This factory verifies result-shape coherence only.
             *
             * It intentionally does not:
             *
             * - call Unicode normalization;
             * - call ICU4J;
             * - call JDK character classification;
             * - validate source-language type syntax;
             * - validate policy-specific limits.
             *
             * Policy-specific limits are checked by
             * [CanonicalTypeLexicalProfile.requireWithinPolicy].
             */
            @JvmStatic
            fun issue(
                snapshot: String,
                lexicalProfile: CanonicalTypeLexicalProfile,
            ): Accepted {
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

                return Accepted(
                    snapshot = snapshot,
                    lexicalProfile = lexicalProfile,
                )
            }
        }
    }

    /**
     * Failed canonical type-text inspection result.
     *
     * This object represents a controlled, fail-closed refusal by the pinned
     * normalization/type-text inspection boundary.
     *
     * [violationCode] is the stable order-facing classification.
     *
     * [reason] is bounded diagnostic prose. It exists to improve failure reports,
     * not to define semantics.
     *
     * Security law:
     *
     * [reason] must be:
     *
     * - non-empty;
     * - length-bounded;
     * - free of C0/C1 control characters;
     * - safe for single-line diagnostic rendering.
     *
     * This blocks newline, tab, NUL, escape, and equivalent control-character
     * injection into logs or structured traces.
     *
     * The reason is not required to be ASCII-only because it may contain
     * human-readable diagnostic prose. However, control material remains
     * forbidden.
     */
    class Rejected private constructor(
        val violationCode: CanonicalTypeTextViolationCode,
        val reason: String,
    ) : CanonicalTypeTextInspectionResult {
        companion object {
            /**
             * Conservative diagnostic cap.
             *
             * This is intentionally small because rejection reasons may be copied
             * into logs, traces, test output, or IDE diagnostics.
             *
             * The full offending input must never be echoed here. If detailed
             * evidence is needed, use a separately budgeted safe diagnostic
             * sample utility.
             */
            private const val MAX_REJECTION_REASON_CHARS: Int = 512

            /**
             * Issues a rejected inspection result.
             *
             * The factory validates only the rejection diagnostic surface.
             *
             * It does not inspect raw input and does not normalize anything.
             */
            @JvmStatic
            fun issue(
                violationCode: CanonicalTypeTextViolationCode,
                reason: String,
            ): Rejected {
                MetamodelProtocolTextGuards.requireBoundedDiagnosticText(
                    field = "CanonicalTypeTextInspectionResult.Rejected.reason",
                    value = reason,
                    maxChars = MAX_REJECTION_REASON_CHARS,
                )

                return Rejected(
                    violationCode = violationCode,
                    reason = reason,
                )
            }
        }
    }
}

/**
 * Machine-readable canonical type-text rejection code.
 *
 * This vocabulary is part of the metamodel inspection order.
 *
 * Do not branch on diagnostic reason strings.
 * Branch on this enum.
 *
 * Evolution law:
 *
 * - Existing order tokens must not be renamed.
 * - Existing order orders must not be reused for different meanings.
 * - New known violations should receive explicit codes.
 * - [UNKNOWN] is reserved for truly unclassified adapter/engine failures and
 *   must not be used as a shortcut for known policy failures.
 *
 * Ordering law:
 *
 * [protocolOrder] gives deterministic diagnostic ordering when multiple
 * violations are accumulated before returning a final rejection.
 *
 * It is not an execution priority unless a specific engine implementation
 * explicitly documents that behavior.
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
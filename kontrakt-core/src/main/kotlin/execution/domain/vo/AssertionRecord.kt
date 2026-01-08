package execution.domain.vo

import execution.domain.AssertionStatus

/**
 * [Value Object] Assertion Record
 *
 * Represents the normalized outcome (Verdict) of a single verification step.
 * Includes both the result status and the source coordinates for deep-linking.
 */
data class AssertionRecord(
    val status: AssertionStatus,
    val rule: AssertionRule,
    val message: String,
    val expected: Any? = null,
    val actual: Any? = null,


    // [ADR-020] Metadata for Deep Linking
    // Replaces (file, line, method) with a strict Value Object.
    val location: SourceLocation = SourceLocation.NotCaptured,
) {
    val ruleName: String get() = rule.key
}
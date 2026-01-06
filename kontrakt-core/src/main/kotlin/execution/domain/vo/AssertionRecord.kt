package execution.domain.vo

import execution.domain.AssertionStatus

/**
 * [Value Object] Assertion Record
 *
 * Represents the normalized outcome (Verdict) of a single verification step.
 * It links the result (Status) with the applied standard (Rule).
 */
data class AssertionRecord(
    val status: AssertionStatus,

    /**
     * The specific rule or standard that was applied.
     * Replaces the legacy String-based 'ruleName'.
     */
    val rule: AssertionRule,

    val message: String,
    val expected: Any?,
    val actual: Any?
) {
    /**
     * Convenience property for legacy support and simple logging.
     * Delegates to the rule's key.
     */
    val ruleName: String
        get() = rule.key
}

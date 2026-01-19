package execution.domain.vo.result

import execution.domain.vo.verification.AssertionRecord

/**
 * [Value Object] The comprehensive result of a test execution.
 *
 * It encapsulates both the verification outcomes ([records]) and the context
 * of the execution ([arguments]). This explicit return type eliminates the need
 * for side-effect-based tracing for the happy path, while preserving data integrity.
 *
 * @property records The list of assertion records generated during execution.
 * @property arguments The map of arguments used for the test, keyed by parameter name.
 */
data class ExecutionResult(
    val records: List<AssertionRecord>,
    val arguments: Map<String, Any?> = emptyMap(),
    val seed: Long,
)

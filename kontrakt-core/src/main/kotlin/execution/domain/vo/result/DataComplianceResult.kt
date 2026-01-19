package execution.domain.vo.result

import execution.domain.vo.verification.AssertionRecord

/**
 * [Value Object] Result of a Data Compliance check.
 *
 * Encapsulates the assertion records and the generated arguments used for fuzzing.
 * This ensures traceability for data compliance failures, aligning with the
 * framework's auditability standards.
 *
 * @property records The assertion outcomes (e.g., equals/hashCode checks).
 * @property capturedArgs The arguments generated for fuzzing the data object.
 */
data class DataComplianceResult(
    val records: List<AssertionRecord>,
    val capturedArgs: Map<String, Any?>,
)

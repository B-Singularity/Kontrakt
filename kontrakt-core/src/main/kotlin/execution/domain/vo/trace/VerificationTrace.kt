package execution.domain.vo.trace

import execution.domain.vo.verification.AssertionRecord
import execution.domain.vo.verification.AssertionStatus

data class VerificationTrace(
    val records: List<AssertionRecord>,
    override val timestamp: Long,
) : TraceEvent {
    override val phase: TracePhase = TracePhase.VERIFICATION
    override val eventType: String = "VERIFICATION"

    override val details: Map<String, Any?> =
        mapOf(
            "total" to records.size,
            "passed" to records.count { it.status == AssertionStatus.PASSED },
            "failed" to records.count { it.status == AssertionStatus.FAILED },
            "results" to records, // Sanitizer will handle serialization safety
        )

    override val isCritical: Boolean = records.any { it.status == AssertionStatus.FAILED }
}

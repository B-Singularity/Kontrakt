package execution.domain.vo.trace

import execution.domain.AssertionStatus
import infrastructure.json.escapeJson

data class VerificationTrace(
    val rule: String,
    val status: AssertionStatus,
    val detail: String,

    override val timestamp: Long
) : TraceEvent {
    override val phase = TracePhase.VERIFICATION

    override fun toNdjson(): String {
        return """{"timestamp":$timestamp,"phase":"$phase","rule":"${rule.escapeJson()}","status":"$status","detail":"${detail.escapeJson()}"}"""
    }
}

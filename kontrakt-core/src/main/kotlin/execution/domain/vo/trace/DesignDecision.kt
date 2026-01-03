package execution.domain.vo.trace

import infrastructure.json.escapeJson

data class DesignDecision(
    val subject: String,
    val strategy: String,
    val generatedValue: String,
    override val timestamp: Long
) : TraceEvent {
    override val phase = TracePhase.DESIGN
    override fun toNdjson(): String =
        """{"ts":$timestamp,"ph":"$phase","sub":"${subject.escapeJson()}","st":"${strategy.escapeJson()}","val":"${generatedValue.escapeJson()}"}"""
}

package statemachine.transition.diagnostics

import ir.EdgeModel
import stage.diagnostic.evidence.TraceEvent
import stage.diagnostic.evidence.TracePhase

/**
 * [Trace Event] Records a decision made to truncate a recursive cycle.
 */
data class TruncationRecord(
    val path: String,
    val edgeKind: EdgeModel,
    val edgeName: String?,
    val typeId: String,
    val rule: TruncationRule,
    val suppressedConstraints: List<String>,
    override val timestamp: Long,
    override val phase: TracePhase,
) : TraceEvent {
    override val eventType = "CYCLE_TRUNCATION"
    override val details: Map<String, Any?> =
        mapOf(
            "path" to path,
            "edge" to "$edgeKind($edgeName)",
            "target" to typeId,
            "rule" to rule.name,
            "suppressed" to suppressedConstraints,
        )
}

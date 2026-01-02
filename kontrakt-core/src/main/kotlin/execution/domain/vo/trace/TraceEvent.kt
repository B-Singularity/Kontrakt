package execution.domain.vo.trace

sealed interface TraceEvent {
    val phase: TracePhase
    val timestamp: Long
}


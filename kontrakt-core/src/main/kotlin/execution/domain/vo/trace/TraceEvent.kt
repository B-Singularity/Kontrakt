package execution.domain.vo.trace

/**
 * [Domain] Trace Event Interface
 * Represents an atomic audit event captured during test execution.
 * * Marked as [sealed] because the test lifecycle phases are finite and known.
 * This allows exhaustive pattern matching in the reporting layer without 'else' branches.
 */
sealed interface TraceEvent {
    val timestamp: Long
    val phase: TracePhase

    /**
     * Serializes the event into a single-line NDJSON string.
     * This method avoids heavy reflection-based libraries for performance and safety.
     */
    fun toNdjson(): String
}

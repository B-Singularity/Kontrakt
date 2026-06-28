package stage.diagnostic.evidence

/**
 * [Domain Event] Represents an atomic event captured during test execution.
 * Pure data object with no dependencies on serialization libraries.
 */
interface TraceEvent {
    val timestamp: Long
    val phase: TracePhase
    val eventType: String

    /**
     * Contextual data for the event.
     * Adapters (JSON, HTML) will serialize this map.
     */
    val details: Map<String, Any?>

    /**
     * Indicates if this event is critical and should trigger an immediate flush.
     * Defaults to false.
     */
    val isCritical: Boolean
        get() = false
}

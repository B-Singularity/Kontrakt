package execution.port.outgoing

import execution.domain.vo.trace.TraceEvent

/**
 * [SPI] Scenario Trace
 *
 * Defines the contract for a storage component that collects telemetry data
 * (events, arguments, logs) during the execution of a test scenario.
 *
 * **Thread Safety:**
 * Implementations must ensure thread safety as this component may be accessed
 * concurrently by the Executor (writing) and Interceptors (reading/snapshotting),
 * especially in async test environments.
 *
 * **Immutability:**
 * The exposed properties ([events], [generatedArguments]) should return
 * immutable snapshots or read-only views to prevent concurrent modification exceptions.
 */
interface ScenarioTrace {
    /**
     * A unique identifier for the current test run.
     * Used for correlating logs or naming output files.
     */
    val runId: String

    /**
     * A chronological list of all trace events (GIVEN/WHEN/THEN phases).
     * Includes decisions, execution logs, and verification results.
     * Returns a snapshot of the current events to ensure iteration safety.
     */
    val events: List<TraceEvent>

    /**
     * A read-only view of all generated arguments accumulated so far.
     * Key: Parameter Name, Value: Generated Argument.
     * Returns a snapshot to ensure iteration safety.
     */
    val generatedArguments: Map<String, Any?>

    /**
     * Appends a new event to the trace.
     *
     * **Performance Contract:**
     * This method should be **non-blocking** or minimally blocking to avoid
     * impacting the throughput of high-frequency generative tests.
     * Implementations should prefer lock-free structures (e.g., ConcurrentLinkedQueue).
     *
     * @param event The trace event to record.
     */
    fun add(event: TraceEvent)

    /**
     * Adds generated arguments to the cumulative record.
     * This serves as the "Black Box" data in case of execution crashes.
     *
     * Existing keys will be overwritten, but new keys are appended (cumulative behavior).
     *
     * @param args A map of arguments keyed by their parameter names.
     */
    fun addGeneratedArguments(args: Map<String, Any?>)
}

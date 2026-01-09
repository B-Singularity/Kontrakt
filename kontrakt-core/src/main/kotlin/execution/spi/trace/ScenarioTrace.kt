package execution.spi.trace

import execution.domain.vo.trace.TraceEvent

/**
 * [SPI] Scenario Trace
 *
 * Defines the contract for a storage component that collects telemetry data
 * (decisions, arguments, logs) during the execution of a test scenario.
 *
 */
interface ScenarioTrace {
    /**
     * A unique identifier for the current test run.
     * Used for correlating logs or naming output files.
     */
    val runId: String

    /**
     * A mutable list to store decision events (GIVEN phase).
     * Implementations can decide how to back this list (Memory, File wrapper, etc.).
     */
    val decisions: MutableList<TraceEvent>

    /**
     * A mutable list to store generated arguments (WHEN phase).
     */
    val generatedArguments: MutableList<Any?>

    /**
     * Clears the stored traces.
     * Used to reset the state when reusing the trace object (e.g., in object pooling).
     */
    fun clear()
}

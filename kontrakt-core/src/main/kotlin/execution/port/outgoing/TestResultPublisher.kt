package execution.port.outgoing

import execution.domain.vo.result.TestResultEvent

/**
 * [Port] Test Result Publisher
 *
 * The output port for the Reporting Architecture.
 * Implementations are responsible for handling events (Displaying, Writing, Sending).
 *
 * Implements [AutoCloseable] to support resource cleanup (Flushing buffers, closing files)
 * at the end of the test lifecycle.
 */
interface TestResultPublisher : AutoCloseable {
    /**
     * Publishes a test result event to the configured channel.
     *
     * @param event The lightweight event object containing the Claim Check (journal path).
     */
    fun publish(event: TestResultEvent)

    /**
     * Default implementation to strictly enforce backward compatibility
     * for reporters that don't hold resources (like Console).
     */
    override fun close() {
        // No-op by default
    }
}

package execution.port.outgoing

import execution.domain.vo.TestResultEvent

/**
 * [Port] Test Result Publisher (Output Port)
 *
 * Serves as the dedicated exit point for the Execution Engine (Core) to transmit
 * test result events (Claim Checks) to the Reporting System or an asynchronous messaging channel.
 *
 * * **Pattern**: Implements the [Fire-and-Forget] pattern to avoid blocking the execution flow.
 */
fun interface TestResultPublisher {
    /**
     * Publishes a test result event to the configured channel.
     *
     * @param event The lightweight event object containing the Claim Check (journal path).
     */
    fun publish(event: TestResultEvent)
}

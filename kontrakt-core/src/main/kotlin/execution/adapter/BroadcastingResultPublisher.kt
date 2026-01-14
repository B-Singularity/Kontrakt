package execution.adapter

import exception.KontraktInternalException
import execution.domain.vo.TestResultEvent
import execution.port.outgoing.TestResultPublisher
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * [Infrastructure] Broadcasting Publisher
 *
 * Acts as a router adapter that fans out the output of the Execution domain
 * to multiple downstream domains (e.g., Report, Notification).
 *
 * Features:
 * 1. **Fault Tolerance:** Isolates failures in individual publishers using a "best-effort" strategy.
 * 2. **Observability:** Provides a hook (`onPublishFailure`) to monitor infrastructure health.
 * 3. **Composite Pattern:** Decouples the engine from specific reporting implementations.
 */
class BroadcastingResultPublisher(
    private val publishers: List<TestResultPublisher>,
    private val onPublishFailure: (String, Throwable) -> Unit = { _, _ -> }
) : TestResultPublisher {

    private val logger = KotlinLogging.logger {}

    init {
        if (publishers.isEmpty()) {
            logger.warn { "BroadcastingResultPublisher initialized with NO subscribers. Test results will be lost." }
        }
    }

    override fun publish(event: TestResultEvent) {
        publishers.forEach { publisher ->
            executeSafely(publisher, "publish") {
                it.publish(event)
            }
        }
    }

    /**
     * [Lifecycle] Propagate Close
     * Safely closes all underlying publishers. One failure must not stop others.
     */
    override fun close() {
        publishers.forEach { publisher ->
            executeSafely(publisher, "close") {
                it.close()
            }
        }
    }

    /**
     * Executes an action on a publisher safely, ensuring isolation.
     */
    private fun executeSafely(
        publisher: TestResultPublisher,
        actionName: String,
        block: (TestResultPublisher) -> Unit
    ) {
        try {
            block(publisher)
        } catch (t: Throwable) {
            handleFailure(publisher, actionName, t)
        }
    }

    private fun handleFailure(publisher: TestResultPublisher, action: String, cause: Throwable) {
        val publisherName = publisher::class.simpleName ?: "UnknownPublisher"

        // 1. Semantic Exception wrapping
        val internalError = KontraktInternalException(
            message = "Reporting failed during '$action' in adapter: $publisherName",
            cause = cause
        )

        // 2. Structured Logging
        logger.error(internalError) { internalError.message }

        // 3. Emergency Fallback (Direct to Stderr)
        System.err.println("[Kontrakt Critical] Failed to $action result via $publisherName: ${cause.message}")

        // 4. Notify Observer
        onPublishFailure(publisherName, internalError)
    }

}

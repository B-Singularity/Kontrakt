package reporting.adapter.outgoing.file

import execution.domain.vo.TestResultEvent
import execution.port.outgoing.TestResultPublisher
import reporting.adapter.config.ReportingDirectives
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * [Adapter] JSON File Reporter
 *
 * Collects all test events and dumps them into a single JSON file at the end of execution.
 * Useful for CI/CD integration or external analysis tools.
 */
class JsonReporter(
    private val config: ReportingDirectives
) : TestResultPublisher {

    private val events = ConcurrentLinkedQueue<TestResultEvent>()

    override fun publish(event: TestResultEvent) {
        events.add(event)
    }

    /**
     * [Lifecycle] Close & Flush
     * Writes the collected events to the disk when the engine shuts down.
     */
    override fun close() {
        if (events.isEmpty()) return // No events, no file needed

        val reportFile = config.baseReportDir.resolve("test-results.json").toFile()
        reportFile.parentFile.mkdirs()

        // Manual JSON construction
        val jsonContent = buildString {
            append("[")
            val iterator = events.iterator()
            while (iterator.hasNext()) {
                val event = iterator.next()
                append(eventToJson(event))
                if (iterator.hasNext()) append(",")
            }
            append("]")
        }

        reportFile.writeText(jsonContent)
    }

    private fun eventToJson(event: TestResultEvent): String {
        // Simple manual JSON serialization
        return """
            {
              "testName": "${event.testName}",
              "status": "${event.status::class.simpleName}",
              "duration": ${event.durationMs},
              "seed": ${event.seed}
            }
        """.trimIndent()
    }
}
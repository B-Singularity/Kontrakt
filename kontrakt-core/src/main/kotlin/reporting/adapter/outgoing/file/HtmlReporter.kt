package reporting.adapter.outgoing.file

import execution.domain.vo.TestResultEvent
import execution.port.outgoing.TestResultPublisher
import reporting.adapter.config.ReportingDirectives
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * [Adapter] HTML File Reporter
 *
 * Generates a human-readable HTML dashboard.
 */
class HtmlReporter(
    private val config: ReportingDirectives,
) : TestResultPublisher {
    private val events = ConcurrentLinkedQueue<TestResultEvent>()

    override fun publish(event: TestResultEvent) {
        events.add(event)
    }

    override fun close() {
        if (events.isEmpty()) return

        val reportFile = config.baseReportDir.resolve("index.html").toFile()
        reportFile.parentFile.mkdirs()

        val htmlContent =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Kontrakt Test Report</title>
                <style>
                    body { font-family: sans-serif; padding: 20px; }
                    .passed { color: green; }
                    .failed { color: red; }
                    table { width: 100%; border-collapse: collapse; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                </style>
            </head>
            <body>
                <h1>Test Execution Report</h1>
                <table>
                    <tr>
                        <th>Test Name</th>
                        <th>Status</th>
                        <th>Duration (ms)</th>
                    </tr>
                    ${events.joinToString("\n") { row(it) }}
                </table>
            </body>
            </html>
            """.trimIndent()

        reportFile.writeText(htmlContent)
    }

    private fun row(event: TestResultEvent): String {
        val style = if (event.status::class.simpleName == "Passed") "passed" else "failed"
        return """
            <tr>
                <td>${event.testName}</td>
                <td class="$style">${event.status::class.simpleName}</td>
                <td>${event.durationMs}</td>
            </tr>
        """
    }
}

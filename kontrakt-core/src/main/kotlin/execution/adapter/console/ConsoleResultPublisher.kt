package execution.adapter.console

import execution.domain.TestStatus
import execution.domain.vo.TestResultEvent
import execution.port.outgoing.TestResultPublisher

/**
 * [Adapter] Console Output Publisher
 *
 * Provides real-time feedback to the developer via the standard output (terminal).
 * This component acts as the "Live Monitor" of the test execution.
 *
 * ## Features
 * - **Rich Output**: Uses ANSI colors for readability.
 * - **CI/CD Friendly**: Automatically disables colors if `NO_COLOR` environment variable is set.
 * - **Thread-Safe**: Uses synchronization to prevent interleaved output during parallel execution.
 */
class ConsoleResultPublisher : TestResultPublisher {
    // [Refactor] Theme strategy based on environment
    private val theme = if (System.getenv("NO_COLOR").isNullOrEmpty()) AnsiTheme else NoColorTheme

    override fun publish(event: TestResultEvent) {
        synchronized(this) {
            printResult(event)
        }
    }

    private fun printResult(event: TestResultEvent) {
        val icon = theme.getIcon(event.status)
        val statusText = theme.getStatusText(event.status)
        val runIdShort = event.runId.take(8)

        // Summary Line: [12345678] ✔ PASSED (45ms)
        println("[${theme.bold(runIdShort)}] $icon $statusText (${event.durationMs}ms)")

        // Detail Lines (Error Messages)
        when (val status = event.status) {
            is TestStatus.AssertionFailed -> {
                println("    ${theme.red("└─ Reason:")} ${status.message}")
                println("    ${theme.red("   Expected:")} ${status.expected}")
                println("    ${theme.red("   Actual:")}   ${status.actual}")
            }

            is TestStatus.ExecutionError -> {
                val causeName = status.cause::class.simpleName ?: "UnknownException"
                println("    ${theme.red("└─ Exception:")} $causeName: ${status.cause.message}")
            }

            else -> {}
        }

        // Log Path Notification
        if (event.journalPath.isNotEmpty()) {
            println("    ${theme.yellow("└─ Log Saved:")} file://${event.journalPath}")
        }
    }

    // --- Internal Theme System ---

    private interface ConsoleTheme {
        fun getIcon(status: TestStatus): String

        fun getStatusText(status: TestStatus): String

        fun red(text: String): String

        fun yellow(text: String): String

        fun bold(text: String): String
    }

    private object AnsiTheme : ConsoleTheme {
        private const val RESET = "\u001B[0m"
        private const val RED = "\u001B[31m"
        private const val GREEN = "\u001B[32m"
        private const val YELLOW = "\u001B[33m"
        private const val BOLD = "\u001B[1m"

        override fun getIcon(status: TestStatus) =
            when (status) {
                is TestStatus.Passed -> "$GREEN✔$RESET"
                is TestStatus.AssertionFailed -> "$RED✘$RESET"
                is TestStatus.ExecutionError -> "$RED💥$RESET"
                is TestStatus.Aborted -> "$YELLOW⚠$RESET"
                is TestStatus.Disabled -> "$YELLOW○$RESET"
            }

        override fun getStatusText(status: TestStatus) =
            when (status) {
                is TestStatus.Passed -> "${GREEN}PASSED$RESET"
                is TestStatus.AssertionFailed -> "${RED}FAILED$RESET"
                is TestStatus.ExecutionError -> "${RED}ERROR$RESET"
                is TestStatus.Aborted -> "${YELLOW}SKIPPED$RESET"
                is TestStatus.Disabled -> "${YELLOW}DISABLED$RESET"
            }

        override fun red(text: String) = "$RED$text$RESET"

        override fun yellow(text: String) = "$YELLOW$text$RESET"

        override fun bold(text: String) = "$BOLD$text$RESET"
    }

    private object NoColorTheme : ConsoleTheme {
        override fun getIcon(status: TestStatus) =
            when (status) {
                is TestStatus.Passed -> "[PASS]"
                is TestStatus.AssertionFailed -> "[FAIL]"
                is TestStatus.ExecutionError -> "[ERR ]"
                is TestStatus.Aborted -> "[SKIP]"
                is TestStatus.Disabled -> "[OFF ]"
            }

        override fun getStatusText(status: TestStatus) =
            when (status) {
                is TestStatus.Passed -> "PASSED"
                is TestStatus.AssertionFailed -> "FAILED"
                is TestStatus.ExecutionError -> "ERROR"
                is TestStatus.Aborted -> "SKIPPED"
                is TestStatus.Disabled -> "DISABLED"
            }

        override fun red(text: String) = text

        override fun yellow(text: String) = text

        override fun bold(text: String) = text
    }
}

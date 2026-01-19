package reporting.adapter.outgoing.console

import execution.domain.vo.result.TestStatus

/**
 * [Style] Console Theme Strategy
 * Abstraction for ANSI color codes and icons to support both Local and CI environments.
 */
interface ConsoleTheme {
    fun red(text: String): String

    fun green(text: String): String

    fun yellow(text: String): String

    fun bold(text: String): String

    fun line(): String

    fun icon(status: TestStatus): String
}

/**
 * Theme for terminals that support ANSI escape codes.
 */
object AnsiTheme : ConsoleTheme {
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BOLD = "\u001B[1m"
    private const val GRAY = "\u001B[90m"

    override fun red(text: String) = "$RED$text$RESET"

    override fun green(text: String) = "$GREEN$text$RESET"

    override fun yellow(text: String) = "$YELLOW$text$RESET"

    override fun bold(text: String) = "$BOLD$text$RESET"

    override fun line() = "$GRAY──────────────────────────────────────────────────────$RESET"

    override fun icon(status: TestStatus) =
        when (status) {
            is TestStatus.AssertionFailed, is TestStatus.ExecutionError -> "✖"
            else -> "✔"
        }
}

/**
 * Theme for environments that do not support colors (e.g., simple CI logs).
 */
object NoColorTheme : ConsoleTheme {
    override fun red(text: String) = text

    override fun green(text: String) = text

    override fun yellow(text: String) = text

    override fun bold(text: String) = text

    override fun line() = "------------------------------------------------------"

    override fun icon(status: TestStatus) = if (status is TestStatus.Passed) "[PASS]" else "[FAIL]"
}

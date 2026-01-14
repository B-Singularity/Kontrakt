package reporting.adapter.outgoing.console

import execution.domain.TestStatus
import execution.domain.vo.AuditDepth
import execution.domain.vo.AuditPolicy
import execution.domain.vo.LogRetention
import execution.domain.vo.TestResultEvent

/**
 * [View] Console Layout Strategy
 * Responsible for formatting the output structure (e.g., Summaries, Failure Cards).
 */
interface ConsoleLayout {
    fun printProgress(status: TestStatus)
    fun printSummary(total: Int, passed: Int, failed: Int, ignored: Int, duration: Long)
    fun printFailures(failures: List<Pair<TestResultEvent, TestStatus>>)
}

/**
 * Standard Layout: Silent Progress -> Summary Table -> Detailed Failure Cards.
 * It adapts its output based on the injected [AuditPolicy].
 */
class StandardConsoleLayout(
    private val theme: ConsoleTheme,
    private val policy: AuditPolicy
) : ConsoleLayout {

    override fun printProgress(status: TestStatus) {
        // [Policy Check] Only print progress dots if retention is ALWAYS (Verbose).
        // Otherwise, keep the console silent.
        if (policy.retention == LogRetention.ALWAYS && status is TestStatus.Passed) {
            print(".")
        }
    }

    override fun printSummary(total: Int, passed: Int, failed: Int, ignored: Int, duration: Long) {
        val colorFunc = if (failed > 0) theme::red else theme::green

        val icon = if (failed > 0) {
            theme.icon(TestStatus.AssertionFailed("", "", ""))
        } else {
            theme.icon(TestStatus.Passed)
        }

        println(
            """
            ${theme.line()}
            $icon ${colorFunc("TEST RESULTS")}
            ${theme.line()}
            
            Total:    $total
            Passed:   ${theme.green(passed.toString())}
            Failed:   ${theme.red(failed.toString())}
            Ignored:  ${theme.yellow(ignored.toString())}
            
            Duration: ${duration}ms
            ${theme.line()}
        """.trimIndent()
        )
    }

    override fun printFailures(failures: List<Pair<TestResultEvent, TestStatus>>) {
        if (failures.isEmpty()) {
            println("\n${theme.green("✨ All tests passed successfully!")}\n")
            return
        }

        println("\n${theme.bold("▼ FAILURE DETAILS (${failures.size})")}")

        failures.forEach { (event, status) ->
            when (status) {
                is TestStatus.AssertionFailed -> printFailureCard(event, status)
                is TestStatus.ExecutionError -> printErrorCard(event, status)
                else -> {}
            }
        }
        println("\n")
    }

    private fun printFailureCard(event: TestResultEvent, status: TestStatus.AssertionFailed) {
        val location = findUserLocation(status.cause)

        // [Policy Check] Show trace badge if depth is EXPLAINABLE
        val traceBadge = if (policy.depth == AuditDepth.EXPLAINABLE) " ${theme.yellow("[TRACE]")}" else ""

        // [Policy Check] Hide log path if retention is NONE
        val logInfo = if (policy.retention != LogRetention.NONE) {
            "\n(ℹ️ Log: file://${event.journalPath})"
        } else {
            ""
        }

        println(
            """
            
            ${theme.red("[❌ FAILED]")}$traceBadge ${theme.bold(event.testName)}
            🔍 ${theme.bold("Reason:")} ${status.message}
               ${theme.red("Expected:")} ${status.expected}
               ${theme.red("Actual:")}   ${status.actual}
            📍 ${theme.bold("Location:")}
               $location
            🌱 ${theme.bold("Reproduction:")} --seed ${event.seed} $logInfo
        """.trimIndent()
        )
    }

    private fun printErrorCard(event: TestResultEvent, status: TestStatus.ExecutionError) {
        val location = findUserLocation(status.cause)
        val exName = status.cause::class.simpleName ?: "UnknownException"

        val traceBadge = if (policy.depth == AuditDepth.EXPLAINABLE) " ${theme.yellow("[TRACE]")}" else ""

        val logInfo = if (policy.retention != LogRetention.NONE) {
            "\n(ℹ️ Log: file://${event.journalPath})"
        } else {
            ""
        }

        println(
            """
            
            ${theme.red("[💥 ERROR]")}$traceBadge ${theme.bold(event.testName)}
            ⚠️ ${theme.bold("Exception:")} $exName
               ${theme.yellow("Message:")}   ${status.cause.message}
            📍 ${theme.bold("Location:")}
               $location
            
            🌱 ${theme.bold("Reproduction:")} --seed ${event.seed} $logInfo
        """.trimIndent()
        )
    }

    // [Smart Filtering] Filters out framework internals to show user code
    private fun findUserLocation(throwable: Throwable?): String {
        if (throwable == null) return "Unknown Source"
        return throwable.stackTrace
            .asSequence()
            .filterNot { it.className.startsWith("execution.") }
            .filterNot { it.className.startsWith("reporting.") }
            .filterNot { it.className.startsWith("java.") }
            .filterNot { it.className.startsWith("jdk.") }
            .filterNot { it.className.startsWith("org.junit.") }
            .take(3)
            .joinToString("\n   ") { "at $it" }
            .ifEmpty { "   (No user code found in stack trace)" }
    }
}
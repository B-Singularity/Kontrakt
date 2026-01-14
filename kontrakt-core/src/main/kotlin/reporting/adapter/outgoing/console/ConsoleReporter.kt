package reporting.adapter.outgoing.console

import execution.domain.TestStatus
import execution.domain.vo.TestResultEvent
import execution.port.outgoing.TestResultPublisher
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

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
class ConsoleReporter(
    private val layout: ConsoleLayout,
) : TestResultPublisher {
    private val passedCount = AtomicInteger(0)
    private val failedCount = AtomicInteger(0)
    private val abortedCount = AtomicInteger(0)
    private val startTime = System.currentTimeMillis()

    // Buffer for failure events to be displayed at the end
    private val failureQueue = ConcurrentLinkedQueue<Pair<TestResultEvent, TestStatus>>()

    override fun publish(event: TestResultEvent) {
        when (val status = event.status) {
            is TestStatus.Passed -> {
                passedCount.incrementAndGet()
                layout.printProgress(status)
            }

            is TestStatus.AssertionFailed -> {
                failedCount.incrementAndGet()
                failureQueue.add(event to status)
            }

            is TestStatus.ExecutionError -> {
                failedCount.incrementAndGet()
                failureQueue.add(event to status)
            }

            is TestStatus.Aborted -> abortedCount.incrementAndGet()
            else -> {}
        }
    }

    /**
     * Triggers the rendering of the final report.
     * Should be called by the engine when execution is complete.
     */
    fun printFinalReport() {
        val total = passedCount.get() + failedCount.get() + abortedCount.get()
        val duration = System.currentTimeMillis() - startTime

        // 1. Print Summary Table
        layout.printSummary(
            total = total,
            passed = passedCount.get(),
            failed = failedCount.get(),
            ignored = abortedCount.get(),
            duration = duration,
        )

        // 2. Print Failure Details
        layout.printFailures(failureQueue.toList())
    }
}

package execution.adapter.junit

import execution.adapter.reporting.BroadcastingResultPublisher
import execution.adapter.trace.WorkerTraceSinkPool
import execution.domain.vo.config.AuditPolicy
import execution.port.outgoing.TestResultPublisher
import reporting.adapter.outgoing.console.AnsiTheme
import reporting.adapter.outgoing.console.ConsoleReporter
import reporting.adapter.outgoing.console.ConsoleTheme
import reporting.adapter.outgoing.console.NoColorTheme
import reporting.adapter.outgoing.console.StandardConsoleLayout
import java.nio.file.Path

/**
 * [Default Implementation] Production-grade Infrastructure Factory.
 *
 * This singleton object acts as the default composition root for the JUnit Adapter.
 * It implements both [ReportingInfrastructureFactory] and [TracingInfrastructureFactory],
 * providing concrete instances of [BroadcastingResultPublisher] and [WorkerTraceSinkPool].
 */
object DefaultInfrastructureFactory : ReportingInfrastructureFactory, TracingInfrastructureFactory {

    override fun createConsoleReporter(auditPolicy: AuditPolicy): ConsoleReporter {
        val noColorEnv = System.getenv("NO_COLOR")

        val theme = resolveTheme(noColorEnv)

        val layout = StandardConsoleLayout(theme, auditPolicy)
        return ConsoleReporter(layout)
    }

    override fun createResultPublisher(
        publishers: List<TestResultPublisher>,
        onPublishFailure: (String, Throwable) -> Unit
    ): TestResultPublisher = BroadcastingResultPublisher(publishers, onPublishFailure)

    override fun createTraceSinkPool(path: Path): WorkerTraceSinkPool = WorkerTraceSinkPool(path)

    /**
     * Determines the [ConsoleTheme] based on the environment variable.
     *
     * [Pure Function] This method is separated for testability.
     * It allows testing the branching logic without mocking System.getenv().
     *
     * @return [ConsoleTheme] (Abstraction) instead of concrete implementation.
     */
    internal fun resolveTheme(noColorEnv: String?): ConsoleTheme {
        return if (noColorEnv.isNullOrEmpty()) AnsiTheme else NoColorTheme
    }
}
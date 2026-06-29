package adapter.junit

import adapter.console.ConsoleReporter
import governance.policy.AuditPolicy
import realization.reporting.TestResultPublisher

/**
 * [Testing Seam] Factory interface for Reporting Infrastructure.
 *
 * Segregated to allow the [KontraktTestEngine] to depend only on reporting capabilities
 * where needed, adhering to the Interface Segregation Principle (ISP).
 */
interface ReportingInfrastructureFactory {
    fun createConsoleReporter(auditPolicy: AuditPolicy): ConsoleReporter

    /**
     * Creates a composite [TestResultPublisher] that broadcasts results to all configured consumers.
     *
     * @param publishers The list of individual publishers (e.g., Console, JSON, HTML).
     * @param onPublishFailure A safe callback to handle exceptions during reporting without crashing the engine.
     */
    fun createResultPublisher(
        publishers: List<TestResultPublisher>,
        onPublishFailure: (String, Throwable) -> Unit,
    ): TestResultPublisher
}

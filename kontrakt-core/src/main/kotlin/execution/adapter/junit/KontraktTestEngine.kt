package execution.adapter.junit

import discovery.adapter.ClassGraphScannerAdapter
import discovery.api.Contract
import discovery.domain.service.TestDiscovererImpl
import discovery.domain.vo.DiscoveryPolicy
import discovery.domain.vo.ScanScope
import exception.KontraktInternalException
import execution.adapter.BroadcastingResultPublisher
import execution.adapter.MockitoEngineAdapter
import execution.adapter.UserControlOptions
import execution.adapter.state.ThreadLocalScenarioControl
import execution.adapter.toExecutionPolicy
import execution.adapter.toReportingDirectives
import execution.adapter.trace.WorkerTraceSinkPool
import execution.api.DefaultRuntimeFactory
import execution.api.KontraktRuntimeFactory
import execution.domain.TestStatus
import execution.domain.factory.ExecutionEnvironmentFactory
import execution.domain.vo.TestResult
import execution.port.outgoing.TestResultPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.PackageSelector
import reporting.adapter.config.ReportFormat
import reporting.adapter.outgoing.console.AnsiTheme
import reporting.adapter.outgoing.console.ConsoleReporter
import reporting.adapter.outgoing.console.NoColorTheme
import reporting.adapter.outgoing.console.StandardConsoleLayout
import reporting.adapter.outgoing.file.HtmlReporter
import reporting.adapter.outgoing.file.JsonReporter
import java.nio.file.Path
import java.time.Clock

/**
 * [Adapter] JUnit Platform Engine Implementation.
 *
 * This acts as the **Composition Root** of the framework.
 * It is responsible for bridging the JUnit Platform Lifecycle with the Kontrakt Domain.
 *
 * **Architectural Role:**
 * - Instantiates concrete [Infrastructure] components (Clock, Mockito, FileSystem).
 * - Injects them into the [Domain] via [DefaultRuntimeFactory].
 * - Delegates execution to the configured Runtime.
 */
class KontraktTestEngine : TestEngine {
    private val logger = KotlinLogging.logger {}
    private lateinit var runtimeFactory: KontraktRuntimeFactory

    override fun getId(): String = "kontrakt-engine"

    override fun discover(
        request: EngineDiscoveryRequest,
        uniqueId: UniqueId,
    ): TestDescriptor {
        val engineDescriptor = KontraktTestDescriptor(uniqueId, "kontrakt")
        val scanScope = resolveScanScope(request)
        val discoveryPolicy = DiscoveryPolicy(scope = scanScope)
        val scanner = ClassGraphScannerAdapter()
        val discoverer = TestDiscovererImpl(scanner)

        val specs =
            runBlocking {
                discoverer
                    .discover(
                        policy = discoveryPolicy,
                        contractMarker = Contract::class
                    )
                    .onFailure { logger.error(it) { "Discovery failed" } }
                    .getOrDefault(emptyList())
            }

        specs.forEach { spec ->
            val specDescriptor =
                KontraktTestDescriptor(
                    uniqueId.append("spec", spec.target.fullyQualifiedName),
                    spec.target.displayName,
                    spec,
                )
            engineDescriptor.addChild(specDescriptor)
        }

        return engineDescriptor
    }

    override fun execute(request: ExecutionRequest) {
        val engineDescriptor = request.rootTestDescriptor
        val listener = request.engineExecutionListener

        // 1. [Infrastructure] Setup basic components
        val clock = Clock.systemDefaultZone()
        val mockingEngine = MockitoEngineAdapter()
        val scenarioControl = ThreadLocalScenarioControl()
        val traceSinkPool = WorkerTraceSinkPool(Path.of("build/kontrakt"))

        // 2. [Boundary] Load User Input
        val userOptions = UserControlOptions(
            traceMode = System.getProperty("kontrakt.trace")?.toBoolean() ?: false,
            seed = System.getProperty("kontrakt.seed")?.toLongOrNull(),
        )

        // 3. [Translation] DTO -> Domain Policy
        val executionPolicy = userOptions.toExecutionPolicy()
        val reportingDirectives = userOptions.toReportingDirectives()

        // 4. [Wiring] Assemble Reporters
        val publishers = mutableListOf<TestResultPublisher>()

        // 1. Console Reporter
        val theme = if (System.getenv("NO_COLOR").isNullOrEmpty()) AnsiTheme else NoColorTheme
        val layout = StandardConsoleLayout(theme, executionPolicy.auditing)
        val consoleReporter = ConsoleReporter(layout)
        publishers.add(consoleReporter)

        // 2. File Reporters
        if (ReportFormat.JSON in reportingDirectives.formats) {
            publishers.add(JsonReporter(reportingDirectives))
        }
        if (ReportFormat.HTML in reportingDirectives.formats) {
            publishers.add(HtmlReporter(reportingDirectives))
        }

        val resultPublisher = BroadcastingResultPublisher(
            publishers = publishers,
            onPublishFailure = { name, error ->
                System.err.println("[Kontrakt Engine] Reporter '$name' failed: ${error.message}")
            }
        )

        try {
            // 5. [Runtime] Create Runtime Factory
            runtimeFactory = DefaultRuntimeFactory(
                mockingEngine = mockingEngine,
                scenarioControl = scenarioControl,
                traceSinkPool = traceSinkPool,
                resultPublisher = resultPublisher,
                clock = clock,
                executionPolicy = executionPolicy
            )

            listener.executionStarted(engineDescriptor)

            engineDescriptor.children.forEach { descriptor ->
                if (descriptor is KontraktTestDescriptor && descriptor.spec != null) {
                    executeSpec(descriptor, listener)
                }
            }

            listener.executionFinished(engineDescriptor, TestExecutionResult.successful())

            // 6. [Finalize] Print Console Summary
            consoleReporter.printFinalReport()

        } finally {
            traceSinkPool.close()
        }
    }

    private fun executeSpec(
        descriptor: KontraktTestDescriptor,
        listener: EngineExecutionListener,
    ) {
        listener.executionStarted(descriptor)

        val envFactory = ExecutionEnvironmentFactory(Clock.systemDefaultZone())
        val environment = envFactory.create()

        ThreadLocalScenarioControl.bind(environment)

        try {
            val scenarioExecutor = runtimeFactory.createExecutor()

            val execution = runtimeFactory.createExecution(descriptor.spec!!, scenarioExecutor)

            val result = execution.execute()

            reportResult(descriptor, result, listener)
        } catch (e: Exception) {
            logger.error(e) { "💥 Framework CRASH: ${descriptor.displayName}" }
            listener.executionFinished(descriptor, TestExecutionResult.failed(e))
        } finally {
            ThreadLocalScenarioControl.clear()
        }
    }

    private fun reportResult(descriptor: TestDescriptor, result: TestResult, listener: EngineExecutionListener) {
        when (val status = result.finalStatus) {
            is TestStatus.Passed -> listener.executionFinished(descriptor, TestExecutionResult.successful())
            is TestStatus.AssertionFailed -> listener.executionFinished(
                descriptor,
                TestExecutionResult.failed(AssertionError(status.message))
            )

            is TestStatus.ExecutionError -> listener.executionFinished(
                descriptor,
                TestExecutionResult.failed(status.cause)
            )

            else -> listener.executionFinished(
                descriptor,
                TestExecutionResult.failed(KontraktInternalException("Unknown status"))
            )
        }
    }

    private fun resolveScanScope(request: EngineDiscoveryRequest): ScanScope {
        val classSelector = request.getSelectorsByType(ClassSelector::class.java)
        if (classSelector.isNotEmpty()) {
            return ScanScope.Classes(classSelector.map { it.className }.toSet())
        }
        val packageSelectors = request.getSelectorsByType(PackageSelector::class.java)
        if (packageSelectors.isNotEmpty()) {
            return ScanScope.Packages(packageSelectors.map { it.packageName }.toSet())
        }
        return ScanScope.All
    }
}

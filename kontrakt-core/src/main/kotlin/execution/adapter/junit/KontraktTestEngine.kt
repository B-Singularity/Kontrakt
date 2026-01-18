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
import execution.domain.vo.ExecutionPolicy
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
 * This class acts as the **Composition Root** of the Kontrakt framework.
 * It serves as the bridge between the JUnit Platform Lifecycle and the Kontrakt Domain logic.
 *
 * ### Architectural Role
 * 1. **Lifecycle Management:** Orchestrates the Discovery, Setup, Execution, and Reporting phases.
 * 2. **Dependency Injection:** Instantiates infrastructure components (Clock, FileSystem, MockingEngine)
 * and injects them into the Domain layer via [DefaultRuntimeFactory].
 * 3. **Consistency Guardian:** Defines the **Master Seed** for the entire test run to guarantee reproducibility.
 *
 * ### Seed Management Strategy (ADR)
 * To ensure deterministic execution across all components (Environment, Fuzzing, Mocking),
 * the **Random Seed** is determined ONCE at the beginning of the [execute] method.
 * This seed is baked into the [ExecutionPolicy] and propagated down to every child component.
 */
class KontraktTestEngine : TestEngine {
    private val logger = KotlinLogging.logger {}
    private lateinit var runtimeFactory: KontraktRuntimeFactory

    override fun getId(): String = "kontrakt-engine"

    /**
     * Phase 1: Discovery
     * Scans the classpath to find classes marked with `@Contract` or implementing [Contract].
     */
    override fun discover(
        request: EngineDiscoveryRequest,
        uniqueId: UniqueId,
    ): TestDescriptor {
        val engineDescriptor = KontraktTestDescriptor(uniqueId, "kontrakt")
        val scanScope = resolveScanScope(request)
        val discoveryPolicy = DiscoveryPolicy(scope = scanScope)

        // Infrastructure: Use ClassGraph for efficient scanning
        val scanner = ClassGraphScannerAdapter()
        val discoverer = TestDiscovererImpl(scanner)

        val specs =
            runBlocking {
                discoverer
                    .discover(
                        policy = discoveryPolicy,
                        contractMarker = Contract::class,
                    ).onFailure { logger.error(it) { "Discovery failed" } }
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

    /**
     * Phase 2: Execution & Orchestration
     *
     * This is the entry point for the actual test run. It initializes the infrastructure,
     * determines the global execution policy, and iterates through discovered tests.
     */
    override fun execute(request: ExecutionRequest) {
        val engineDescriptor = request.rootTestDescriptor
        val listener = request.engineExecutionListener

        // 1. [Infrastructure] Instantiate Infrastructure Adapters
        val clock = Clock.systemDefaultZone()
        val mockingEngine = MockitoEngineAdapter()
        val scenarioControl = ThreadLocalScenarioControl()
        val traceSinkPool = WorkerTraceSinkPool(Path.of("build/kontrakt"))

        // 2. [Boundary] Load User Configuration (System Properties)
        val userOptions =
            UserControlOptions(
                traceMode = System.getProperty("kontrakt.trace")?.toBoolean() ?: false,
                seed = System.getProperty("kontrakt.seed")?.toLongOrNull(),
            )

        // 3. [Policy] Establish the "Source of Truth" for Determinism
        // ADR: Determine the Master Seed HERE. If the user didn't provide one, generate it.
        // This ensures consistent seeding across Environment, Generators, and Logs.
        val masterSeed = userOptions.seed ?: System.currentTimeMillis()

        // Bake the master seed into the ExecutionPolicy.
        // Now, policy.determinism.seed is GUARANTEED to be non-null for downstream consumers.
        val basePolicy = userOptions.toExecutionPolicy()
        val executionPolicy =
            basePolicy.copy(
                determinism = basePolicy.determinism.copy(seed = masterSeed),
            )

        val reportingDirectives = userOptions.toReportingDirectives()

        // 4. [Wiring] Assemble Reporters
        val publishers = mutableListOf<TestResultPublisher>()

        // 4-1. Console Reporter setup
        val theme = if (System.getenv("NO_COLOR").isNullOrEmpty()) AnsiTheme else NoColorTheme
        val layout = StandardConsoleLayout(theme, executionPolicy.auditing)
        val consoleReporter = ConsoleReporter(layout)
        publishers.add(consoleReporter)

        // 4-2. File Reporters setup
        if (ReportFormat.JSON in reportingDirectives.formats) {
            publishers.add(JsonReporter(reportingDirectives))
        }
        if (ReportFormat.HTML in reportingDirectives.formats) {
            publishers.add(HtmlReporter(reportingDirectives))
        }

        val resultPublisher =
            BroadcastingResultPublisher(
                publishers = publishers,
                onPublishFailure = { name, error ->
                    System.err.println("[Kontrakt Engine] Reporter '$name' failed: ${error.message}")
                },
            )

        try {
            // 5. [Runtime] Initialize the Runtime Factory
            // Inject the finalized ExecutionPolicy (with Master Seed) and Infrastructure components.
            runtimeFactory =
                DefaultRuntimeFactory(
                    mockingEngine = mockingEngine,
                    scenarioControl = scenarioControl,
                    traceSinkPool = traceSinkPool,
                    resultPublisher = resultPublisher,
                    clock = clock,
                    executionPolicy = executionPolicy,
                )

            // Notify JUnit: Execution Started
            listener.executionStarted(engineDescriptor)

            // Execute each discovered spec
            engineDescriptor.children.forEach { descriptor ->
                if (descriptor is KontraktTestDescriptor && descriptor.spec != null) {
                    // Pass the Global Execution Policy to ensure consistency
                    executeSpec(descriptor, listener, executionPolicy)
                }
            }

            // Notify JUnit: Execution Finished
            listener.executionFinished(engineDescriptor, TestExecutionResult.successful())

            // 6. [Finalize] Print Executive Summary to Console
            consoleReporter.printFinalReport()
        } finally {
            // [Cleanup] Ensure resources are released (e.g., flush logs, close file handles)
            traceSinkPool.close()
        }
    }

    /**
     * Phase 3: Spec Execution
     *
     * Sets up the isolation environment (ThreadLocal) and triggers the execution pipeline
     * for a single test specification.
     *
     * @param policy The global execution policy containing the Master Seed.
     */
    private fun executeSpec(
        descriptor: KontraktTestDescriptor,
        listener: EngineExecutionListener,
        policy: ExecutionPolicy,
    ) {
        listener.executionStarted(descriptor)

        val envFactory = ExecutionEnvironmentFactory(Clock.systemDefaultZone())

        // [Determinism] Create the Execution Environment using the Master Seed.
        // This ensures that Random instances inside the test use the exact seed defined in the Policy.
        // The '!!' assertion is safe here because we baked the seed in step 3 of execute().
        val environment = envFactory.create(seed = policy.determinism.seed!!)

        // [Isolation] Bind the environment to the current thread.
        ThreadLocalScenarioControl.bind(environment)

        try {
            // Create Executor and Execution Aggregate
            val scenarioExecutor = runtimeFactory.createExecutor()
            val execution = runtimeFactory.createExecution(descriptor.spec!!, scenarioExecutor)

            // Trigger the Interceptor Chain
            val result = execution.execute()

            // Report results back to JUnit
            reportResult(descriptor, result, listener)
        } catch (e: Exception) {
            logger.error(e) { "💥 Framework CRASH: ${descriptor.displayName}" }
            listener.executionFinished(descriptor, TestExecutionResult.failed(e))
        } finally {
            // [Cleanup] Always clear ThreadLocal to prevent pollution across tests
            ThreadLocalScenarioControl.clear()
        }
    }

    private fun reportResult(
        descriptor: TestDescriptor,
        result: TestResult,
        listener: EngineExecutionListener,
    ) {
        when (val status = result.finalStatus) {
            is TestStatus.Passed -> listener.executionFinished(descriptor, TestExecutionResult.successful())
            is TestStatus.AssertionFailed ->
                listener.executionFinished(
                    descriptor,
                    TestExecutionResult.failed(AssertionError(status.message)),
                )

            is TestStatus.ExecutionError ->
                listener.executionFinished(
                    descriptor,
                    TestExecutionResult.failed(status.cause),
                )

            else ->
                listener.executionFinished(
                    descriptor,
                    TestExecutionResult.failed(KontraktInternalException("Unknown status")),
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

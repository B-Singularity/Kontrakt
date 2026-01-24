package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import discovery.domain.service.TestDiscovererImpl
import discovery.domain.vo.DiscoveredTestTarget
import discovery.domain.vo.ScanScope
import execution.adapter.runtime.DefaultRuntimeFactory
import execution.adapter.state.ThreadLocalScenarioControl
import execution.adapter.trace.WorkerTraceSinkPool
import execution.domain.aggregate.TestExecution
import execution.domain.factory.ExecutionEnvironmentFactory
import execution.domain.vo.config.AuditPolicy
import execution.domain.vo.context.ExecutionEnvironment
import execution.domain.vo.result.TestResult
import execution.domain.vo.result.TestStatus
import execution.port.incoming.TestScenarioExecutor
import execution.port.outgoing.TestResultPublisher
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.PackageSelector
import reporting.adapter.outgoing.console.ConsoleReporter
import reporting.adapter.outgoing.file.HtmlReporter
import reporting.adapter.outgoing.file.JsonReporter
import java.time.Duration

/**
 * Unit tests for [KontraktTestEngine].
 *
 * Verifies orchestration logic, configuration branches, and error handling.
 * Achieves 100% Branch Coverage by testing enabled/disabled states of reporters.
 */
@Execution(ExecutionMode.SAME_THREAD)
class KontraktTestEngineTest {

    private val mockReportingFactory = mockk<ReportingInfrastructureFactory>(relaxed = true)
    private val mockTracingFactory = mockk<TracingInfrastructureFactory>(relaxed = true)

    // Default engine instance for standard tests
    private val engine = KontraktTestEngine(mockReportingFactory, mockTracingFactory)

    @BeforeEach
    fun setup() {
        clearSystemProperties()
        // Default stubbing
        every { mockReportingFactory.createConsoleReporter(any()) } returns mockk(relaxed = true)
        every { mockTracingFactory.createTraceSinkPool(any()) } returns mockk(relaxed = true)
    }

    @AfterEach
    fun teardown() {
        unmockkAll()
        clearAllMocks()
        clearSystemProperties()
    }

    private fun clearSystemProperties() {
        System.clearProperty("kontrakt.trace")
        System.clearProperty("kontrakt.seed")
        System.clearProperty("kontrakt.reports.json")
        System.clearProperty("kontrakt.reports.html")
    }

    @Test
    fun `getId - returns correct engine ID`() {
        assertThat(engine.id).isEqualTo("kontrakt-engine")
    }

    // =================================================================================================================
    // Reporter Branch Coverage (The Core Fix)
    // =================================================================================================================

    @Test
    fun `execute - wires console and file reporters by default`() {
        // [Branch: True] Tests the default path where JSON/HTML are enabled (ADR-015)
        clearSystemProperties()

        val request = mockk<ExecutionRequest>(relaxed = true)
        every { request.rootTestDescriptor } returns mockk(relaxed = true)

        mockkConstructor(DefaultRuntimeFactory::class)
        mockkConstructor(JsonReporter::class)
        mockkConstructor(HtmlReporter::class)

        // Capture publishers
        val publishersSlot = slot<List<TestResultPublisher>>()
        every {
            mockReportingFactory.createResultPublisher(capture(publishersSlot), any())
        } returns mockk(relaxed = true)

        engine.execute(request)

        val publishers = publishersSlot.captured
        assertThat(publishers).hasSize(3)
        // Verify all 3 are present
        assertThat(publishers).anyMatch { it is ConsoleReporter }
        assertThat(publishers).anyMatch { it is JsonReporter }
        assertThat(publishers).anyMatch { it is HtmlReporter }

        unmockkConstructor(DefaultRuntimeFactory::class)
        unmockkConstructor(JsonReporter::class)
        unmockkConstructor(HtmlReporter::class)
    }

    @Test
    fun `execute - skips file reporters when explicitly disabled`() {
        // [Branch: False] Tests the path where 'if (JSON in formats)' returns false.

        // Given: Explicitly disable file reports via System Properties
        System.setProperty("kontrakt.reports.json", "false")
        System.setProperty("kontrakt.reports.html", "false")

        try {
            val freshEngine = KontraktTestEngine(mockReportingFactory, mockTracingFactory)

            val request = mockk<ExecutionRequest>(relaxed = true)
            every { request.rootTestDescriptor } returns mockk(relaxed = true)

            mockkConstructor(DefaultRuntimeFactory::class)

            // Mock ConsoleReporter (Default Reporter)
            val mockConsoleReporter = mockk<ConsoleReporter>(relaxed = true)
            every { mockReportingFactory.createConsoleReporter(any()) } returns mockConsoleReporter

            // Capture the list of publishers passed to ResultPublisher
            val publishersSlot = slot<List<TestResultPublisher>>()
            every {
                mockReportingFactory.createResultPublisher(capture(publishersSlot), any())
            } returns mockk(relaxed = true)

            // When
            freshEngine.execute(request)

            // Then
            val publishers = publishersSlot.captured

            // 1. Verify size is 1 (Only Console)
            assertThat(publishers).hasSize(1)

            // 2. Verify it is indeed the ConsoleReporter
            assertThat(publishers[0]).isSameAs(mockConsoleReporter)

            // 3. Explicitly ensure File Reporters are NOT present
            assertThat(publishers.map { it::class.simpleName })
                .doesNotContain("JsonReporter", "HtmlReporter")

        } finally {
            System.clearProperty("kontrakt.reports.json")
            System.clearProperty("kontrakt.reports.html")

            unmockkConstructor(DefaultRuntimeFactory::class)
        }
    }

    @Test
    fun `execute - invokes failure callback to cover logger`() {
        // [Branch: Logger] Forces execution of the lambda passed to createResultPublisher
        val request = mockk<ExecutionRequest>(relaxed = true)
        every { request.rootTestDescriptor } returns mockk(relaxed = true) { every { children } returns emptySet() }

        mockkConstructor(DefaultRuntimeFactory::class)

        // Capture the lambda
        val lambdaSlot = slot<(String, Throwable) -> Unit>()
        every {
            mockReportingFactory.createResultPublisher(any(), capture(lambdaSlot))
        } returns mockk(relaxed = true)

        engine.execute(request)

        // Force-invoke the captured lambda to trigger the logger logic
        assertDoesNotThrow {
            lambdaSlot.captured("TestReporter", RuntimeException("Simulated Failure for Coverage"))
        }

        unmockkConstructor(DefaultRuntimeFactory::class)
    }

    // =================================================================================================================
    // Other Tests (Discovery, Trace Mode, etc.) - Kept strictly as is
    // =================================================================================================================

    @Test
    fun `discover - handles failure in discovery process`() {
        val uniqueId = UniqueId.forEngine(engine.id)
        val request = mockk<EngineDiscoveryRequest>(relaxed = true)

        mockkConstructor(TestDiscovererImpl::class)
        coEvery { anyConstructed<TestDiscovererImpl>().discover(any(), any()) } returns Result.failure(
            RuntimeException("Discovery Failed")
        )

        val descriptor = engine.discover(request, uniqueId)
        assertThat(descriptor.children).isEmpty()
    }

    @Test
    fun `discover - handles successful discovery`() {
        val uniqueId = UniqueId.forEngine(engine.id)
        val request = mockk<EngineDiscoveryRequest>(relaxed = true)

        mockkConstructor(TestDiscovererImpl::class)
        val mockSpec = mockk<TestSpecification>(relaxed = true) {
            every { target } returns DiscoveredTestTarget.create(String::class, "DisplayName", "pkg.Class").getOrThrow()
        }
        coEvery { anyConstructed<TestDiscovererImpl>().discover(any(), any()) } returns Result.success(listOf(mockSpec))

        val descriptor = engine.discover(request, uniqueId)
        assertThat(descriptor.children).hasSize(1)
    }

    @Test
    fun `execute - runs full execution pipeline for specs`() {
        val request = mockk<ExecutionRequest>(relaxed = true)
        val rootDescriptor = mockk<TestDescriptor>()
        val listener = mockk<EngineExecutionListener>(relaxed = true)
        val mockSpec = mockk<TestSpecification>(relaxed = true)
        val childDescriptor = KontraktTestDescriptor(UniqueId.forEngine("test"), "Child", mockSpec)

        every { request.rootTestDescriptor } returns rootDescriptor
        every { request.engineExecutionListener } returns listener
        every { rootDescriptor.children } returns mutableSetOf(childDescriptor)

        mockkConstructor(DefaultRuntimeFactory::class)
        mockkConstructor(ExecutionEnvironmentFactory::class)
        mockkObject(ThreadLocalScenarioControl)

        val mockTracePool = mockk<WorkerTraceSinkPool>(relaxed = true)
        every { mockTracingFactory.createTraceSinkPool(any()) } returns mockTracePool

        val mockExecutor = mockk<TestScenarioExecutor>()
        val mockExecution = mockk<TestExecution>()
        val mockResult = TestResult(mockk(), TestStatus.Passed, Duration.ZERO, emptyList())
        val mockEnv = mockk<ExecutionEnvironment>()

        every { anyConstructed<ExecutionEnvironmentFactory>().create(any()) } returns mockEnv
        every { anyConstructed<DefaultRuntimeFactory>().createExecutor() } returns mockExecutor
        every { anyConstructed<DefaultRuntimeFactory>().createExecution(mockSpec, mockExecutor) } returns mockExecution
        every { mockExecution.execute() } returns mockResult
        every { ThreadLocalScenarioControl.bind(mockEnv) } just Runs
        every { ThreadLocalScenarioControl.clear() } just Runs

        engine.execute(request)

        verifyOrder {
            mockTracingFactory.createTraceSinkPool(any())
            mockReportingFactory.createResultPublisher(any(), any())
            mockExecution.execute()
            listener.executionFinished(childDescriptor, match { it.status == TestExecutionResult.Status.SUCCESSFUL })
            mockTracePool.close()
        }
    }

    @Test
    fun `execute - enables trace mode via system property`() {
        System.setProperty("kontrakt.trace", "true")
        // Use a fresh engine to ensure property pickup
        val freshEngine = KontraktTestEngine(mockReportingFactory, mockTracingFactory)

        val request = mockk<ExecutionRequest>(relaxed = true)
        val rootDescriptor = mockk<TestDescriptor>()
        val child = KontraktTestDescriptor(UniqueId.forEngine("t"), "c", mockk(relaxed = true))

        every { request.rootTestDescriptor } returns rootDescriptor
        every { request.engineExecutionListener } returns mockk(relaxed = true)
        every { rootDescriptor.children } returns mutableSetOf(child)

        mockkConstructor(DefaultRuntimeFactory::class)
        mockkConstructor(ExecutionEnvironmentFactory::class)
        mockkObject(ThreadLocalScenarioControl)

        every { anyConstructed<ExecutionEnvironmentFactory>().create(any()) } returns mockk()
        every { anyConstructed<DefaultRuntimeFactory>().createExecutor() } returns mockk()
        every { anyConstructed<DefaultRuntimeFactory>().createExecution(any(), any()) } returns mockk(relaxed = true)
        every { ThreadLocalScenarioControl.bind(any()) } just Runs
        every { ThreadLocalScenarioControl.clear() } just Runs

        freshEngine.execute(request)

        val auditPolicySlot = slot<AuditPolicy>()
        verify {
            mockReportingFactory.createConsoleReporter(capture(auditPolicySlot))
        }

        assertThat(auditPolicySlot.captured).isNotEqualTo(AuditPolicy.DEFAULT)
    }

    @Test
    fun `execute - propagates system property seed to execution environment`() {
        val expectedSeed = 12345L
        System.setProperty("kontrakt.seed", expectedSeed.toString())
        val freshEngine = KontraktTestEngine(mockReportingFactory, mockTracingFactory)

        val request = mockk<ExecutionRequest>(relaxed = true)
        val rootDescriptor = mockk<TestDescriptor>()
        every { request.rootTestDescriptor } returns rootDescriptor
        every { rootDescriptor.children } returns mutableSetOf(
            KontraktTestDescriptor(UniqueId.forEngine("t"), "c", mockk(relaxed = true))
        )

        mockkConstructor(ExecutionEnvironmentFactory::class)
        mockkConstructor(DefaultRuntimeFactory::class)
        mockkObject(ThreadLocalScenarioControl)

        every { anyConstructed<DefaultRuntimeFactory>().createExecutor() } returns mockk()
        every { anyConstructed<DefaultRuntimeFactory>().createExecution(any(), any()) } returns mockk(relaxed = true)
        every { anyConstructed<ExecutionEnvironmentFactory>().create(any()) } returns mockk()
        every { ThreadLocalScenarioControl.bind(any()) } just Runs
        every { ThreadLocalScenarioControl.clear() } just Runs

        freshEngine.execute(request)

        verify { anyConstructed<ExecutionEnvironmentFactory>().create(seed = expectedSeed) }
    }

    @Test
    fun `execute - ensures trace pool is closed even if runtime initialization fails`() {
        val request = mockk<ExecutionRequest>(relaxed = true)
        every { request.rootTestDescriptor } returns mockk(relaxed = true) { every { children } returns emptySet() }

        mockkConstructor(DefaultRuntimeFactory::class)

        val mockTracePool = mockk<WorkerTraceSinkPool>(relaxed = true)
        every { mockTracingFactory.createTraceSinkPool(any()) } returns mockTracePool

        every { anyConstructed<DefaultRuntimeFactory>().createExecutor() } throws RuntimeException("Runtime Error")

        assertDoesNotThrow { engine.execute(request) }

        verify(exactly = 1) { mockTracePool.close() }
    }

    @Test
    fun `execute - handles spec execution failure gracefully`() {
        val request = mockk<ExecutionRequest>(relaxed = true)
        val rootDescriptor = mockk<TestDescriptor>()
        val listener = mockk<EngineExecutionListener>(relaxed = true)
        val childDescriptor = KontraktTestDescriptor(UniqueId.forEngine("test"), "Child", mockk(relaxed = true))

        every { request.rootTestDescriptor } returns rootDescriptor
        every { request.engineExecutionListener } returns listener
        every { rootDescriptor.children } returns mutableSetOf(childDescriptor)

        mockkConstructor(DefaultRuntimeFactory::class)
        mockkConstructor(ExecutionEnvironmentFactory::class)
        mockkObject(ThreadLocalScenarioControl)

        every { anyConstructed<ExecutionEnvironmentFactory>().create(any()) } returns mockk()

        val mockExecution = mockk<TestExecution>()
        every { anyConstructed<DefaultRuntimeFactory>().createExecutor() } returns mockk()
        every { anyConstructed<DefaultRuntimeFactory>().createExecution(any(), any()) } returns mockExecution
        every { ThreadLocalScenarioControl.bind(any()) } just Runs
        every { ThreadLocalScenarioControl.clear() } just Runs

        val crashException = RuntimeException("Spec Failed")
        every { mockExecution.execute() } throws crashException

        engine.execute(request)

        verify {
            listener.executionFinished(childDescriptor, match {
                it.status == TestExecutionResult.Status.FAILED && it.throwable.get() == crashException
            })
            ThreadLocalScenarioControl.clear()
        }
    }

    @Test
    fun `resolveScanScope - prioritizes ClassSelector`() {
        val method = engine.javaClass.getDeclaredMethod("resolveScanScope", EngineDiscoveryRequest::class.java)
        method.isAccessible = true

        val request = mockk<EngineDiscoveryRequest>()
        val classSelector = mockk<ClassSelector> { every { className } returns "MyClass" }
        every { request.getSelectorsByType(ClassSelector::class.java) } returns listOf(classSelector)

        val scope = method.invoke(engine, request) as ScanScope
        assertThat(scope).isInstanceOf(ScanScope.Classes::class.java)
    }

    @Test
    fun `resolveScanScope - falls back to PackageSelector`() {
        val method = engine.javaClass.getDeclaredMethod("resolveScanScope", EngineDiscoveryRequest::class.java)
        method.isAccessible = true

        val request = mockk<EngineDiscoveryRequest>()
        every { request.getSelectorsByType(ClassSelector::class.java) } returns emptyList()
        val pkgSelector = mockk<PackageSelector> { every { packageName } returns "my.pkg" }
        every { request.getSelectorsByType(PackageSelector::class.java) } returns listOf(pkgSelector)

        val scope = method.invoke(engine, request) as ScanScope
        assertThat(scope).isInstanceOf(ScanScope.Packages::class.java)
    }

    @Test
    fun `resolveScanScope - defaults to ScanScope_All`() {
        val method = engine.javaClass.getDeclaredMethod("resolveScanScope", EngineDiscoveryRequest::class.java)
        method.isAccessible = true

        val request = mockk<EngineDiscoveryRequest>()
        every { request.getSelectorsByType(ClassSelector::class.java) } returns emptyList()
        every { request.getSelectorsByType(PackageSelector::class.java) } returns emptyList()

        val scope = method.invoke(engine, request) as ScanScope
        assertThat(scope).isEqualTo(ScanScope.All)
    }

    @Test
    fun `reportResult - reports successful execution`() {
        val descriptor = mockk<TestDescriptor>()
        val listener = mockk<EngineExecutionListener>(relaxed = true)
        val result = TestResult(mockk(), TestStatus.Passed, Duration.ZERO, emptyList())

        invokeReportResult(descriptor, result, listener)
        verify { listener.executionFinished(descriptor, match { it.status == TestExecutionResult.Status.SUCCESSFUL }) }
    }

    @Test
    fun `reportResult - reports assertion failure`() {
        val descriptor = mockk<TestDescriptor>()
        val listener = mockk<EngineExecutionListener>(relaxed = true)
        val failureMessage = "Expected 1 but got 2"
        val status = TestStatus.AssertionFailed(failureMessage, 1, 2)
        val result = TestResult(mockk(), status, Duration.ZERO, emptyList())

        invokeReportResult(descriptor, result, listener)
        verify {
            listener.executionFinished(descriptor, match {
                it.status == TestExecutionResult.Status.FAILED &&
                        it.throwable.get() is AssertionError &&
                        it.throwable.get().message == failureMessage
            })
        }
    }

    @Test
    fun `reportResult - reports execution error`() {
        val descriptor = mockk<TestDescriptor>()
        val listener = mockk<EngineExecutionListener>(relaxed = true)
        val exception = RuntimeException("Boom")
        val status = TestStatus.ExecutionError(exception)
        val result = TestResult(mockk(), status, Duration.ZERO, emptyList())

        invokeReportResult(descriptor, result, listener)
        verify {
            listener.executionFinished(descriptor, match {
                it.status == TestExecutionResult.Status.FAILED && it.throwable.get() == exception
            })
        }
    }

    private fun invokeReportResult(descriptor: TestDescriptor, result: TestResult, listener: EngineExecutionListener) {
        val method = engine.javaClass.getDeclaredMethod(
            "reportResult",
            TestDescriptor::class.java,
            TestResult::class.java,
            EngineExecutionListener::class.java
        )
        method.isAccessible = true
        method.invoke(engine, descriptor, result, listener)
    }

    @Test
    fun `execute - ignores invalid or empty descriptors`() {
        // [Branch Coverage]
        // Covers the 'else' branch of: if (descriptor is KontraktTestDescriptor && descriptor.spec != null)

        // Given
        val request = mockk<ExecutionRequest>(relaxed = true)
        val rootDescriptor = mockk<TestDescriptor>()
        val listener = mockk<EngineExecutionListener>(relaxed = true)

        // 1. Valid Descriptor (Real Object)
        val validSpec = mockk<TestSpecification>(relaxed = true)
        val validDescriptor = KontraktTestDescriptor(
            UniqueId.forEngine("valid"),
            "Valid",
            validSpec
        )

        // 2. Invalid Type Descriptor (Mock Object)
        val invalidTypeDescriptor = mockk<TestDescriptor>()

        // 3. Empty Spec Descriptor (Real Object with null spec)
        val emptySpecDescriptor = KontraktTestDescriptor(
            UniqueId.forEngine("empty"),
            "Empty",
            null
        )

        // Setup hierarchy
        every { request.rootTestDescriptor } returns rootDescriptor
        every { request.engineExecutionListener } returns listener
        every { rootDescriptor.children } returns mutableSetOf(
            validDescriptor,
            invalidTypeDescriptor,
            emptySpecDescriptor
        )

        // Mocks for runtime
        mockkConstructor(DefaultRuntimeFactory::class)
        mockkConstructor(ExecutionEnvironmentFactory::class)
        mockkObject(ThreadLocalScenarioControl)

        every { anyConstructed<ExecutionEnvironmentFactory>().create(any()) } returns mockk()
        every { anyConstructed<DefaultRuntimeFactory>().createExecutor() } returns mockk()
        every { anyConstructed<DefaultRuntimeFactory>().createExecution(any(), any()) } returns mockk(relaxed = true)
        every { ThreadLocalScenarioControl.bind(any()) } just Runs
        every { ThreadLocalScenarioControl.clear() } just Runs

        // When
        engine.execute(request)

        // Then
        // 1. Verify Valid Descriptor was executed
        // [Best Practice] Use 'match' to avoid direct object comparison issues (stdObjectAnswer error)
        verify(exactly = 1) {
            listener.executionStarted(match {
                it is KontraktTestDescriptor && it.spec != null
            })
        }


        unmockkConstructor(DefaultRuntimeFactory::class)
        unmockkConstructor(ExecutionEnvironmentFactory::class)
        unmockkObject(ThreadLocalScenarioControl)
    }
}
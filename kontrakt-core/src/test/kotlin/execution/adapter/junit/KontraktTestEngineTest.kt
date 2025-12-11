package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveredTestTarget
import execution.api.TestScenarioExecutor
import execution.domain.TestStatus
import execution.domain.aggregate.TestExecution
import execution.domain.vo.TestResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.platform.engine.ConfigurationParameters
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.discovery.PackageSelector
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import kotlin.test.Test

class KontraktTestEngineTest {

    private val mockFactory: KontraktRuntimeFactory = mock()
    private val mockExecutor: TestScenarioExecutor = mock()
    private val mockExecution: TestExecution = mock()
    private val listener: EngineExecutionListener = mock()
    private val discoveryRequest: EngineDiscoveryRequest = mock()

    private val engine = KontraktTestEngine(mockFactory)

    @Test
    fun `discover - should handle ClassSelector correctly`() {
        val selector = DiscoverySelectors.selectClass("com.example.MyTest")
        whenever(discoveryRequest.getSelectorsByType(ClassSelector::class.java)).thenReturn(listOf(selector))
        whenever(discoveryRequest.getSelectorsByType(PackageSelector::class.java)).thenReturn(emptyList())

        val descriptor = engine.discover(discoveryRequest, UniqueId.forEngine("kontrakt-engine"))

        assertEquals("kontrakt-engine", descriptor.uniqueId.engineId.get())
        assertEquals("kontrakt", descriptor.displayName)
    }

    @Test
    fun `discover - should handle PackageSelector correctly`() {

        val selector = DiscoverySelectors.selectPackage("com.example")

        whenever(discoveryRequest.getSelectorsByType(ClassSelector::class.java)).thenReturn(emptyList())
        whenever(discoveryRequest.getSelectorsByType(PackageSelector::class.java)).thenReturn(listOf(selector))

        val descriptor = engine.discover(discoveryRequest, UniqueId.forEngine("kontrakt-engine"))

        assertEquals("kontrakt", descriptor.displayName)
    }

    @Test
    fun `discover - should fallback to All scan if no selectors provided`() {

        whenever(discoveryRequest.getSelectorsByType(ClassSelector::class.java)).thenReturn(emptyList())
        whenever(discoveryRequest.getSelectorsByType(PackageSelector::class.java)).thenReturn(emptyList())

        val descriptor = engine.discover(discoveryRequest, UniqueId.forEngine("kontrakt-engine"))

        assertEquals("kontrakt", descriptor.displayName)
    }

    @Test
    fun `execute - should report SUCCESS when TestStatus is Passed`() {

        val (request, childDescriptor) = setupExecutionEnvironment()

        val successResult = createTestResult(TestStatus.Passed)

        whenever(mockFactory.createExecutor()).thenReturn(mockExecutor)
        whenever(mockFactory.createExecution(any(), any())).thenReturn(mockExecution)
        whenever(mockExecution.execute()).thenReturn(successResult)

        engine.execute(request)

        verify(listener).executionStarted(childDescriptor)
        verify(listener).executionFinished(childDescriptor, TestExecutionResult.successful())
    }

    @Test
    fun `execute - should report FAILED with AssertionError when TestStatus is AssertionFailed`() {

        val (request, childDescriptor) = setupExecutionEnvironment()

        val failedResult = createTestResult(
            TestStatus.AssertionFailed("Value mismatch", "10", "20")
        )

        whenever(mockFactory.createExecutor()).thenReturn(mockExecutor)
        whenever(mockFactory.createExecution(any(), any())).thenReturn(mockExecution)
        whenever(mockExecution.execute()).thenReturn(failedResult)

        engine.execute(request)

        val captor = argumentCaptor<TestExecutionResult>()
        verify(listener).executionFinished(org.mockito.kotlin.eq(childDescriptor), captor.capture())

        val result = captor.firstValue
        assertEquals(TestExecutionResult.Status.FAILED, result.status)
        assertTrue(result.throwable.get() is AssertionError, "Should wrap in AssertionError")
        assertTrue(result.throwable.get().message!!.contains("Value mismatch"))
    }

    @Test
    fun `execute - should report FAILED with Original Exception when TestStatus is ExecutionError`() {

        val (request, childDescriptor) = setupExecutionEnvironment()

        val originalError = IllegalStateException("Logic Error")
        val errorResult = createTestResult(TestStatus.ExecutionError(originalError))

        whenever(mockFactory.createExecutor()).thenReturn(mockExecutor)
        whenever(mockFactory.createExecution(any(), any())).thenReturn(mockExecution)
        whenever(mockExecution.execute()).thenReturn(errorResult)

        engine.execute(request)

        val captor = argumentCaptor<TestExecutionResult>()
        verify(listener).executionFinished(org.mockito.kotlin.eq(childDescriptor), captor.capture())

        assertEquals(TestExecutionResult.Status.FAILED, captor.firstValue.status)
        assertEquals(originalError, captor.firstValue.throwable.get(), "Should report the original cause")
    }

    @Test
    fun `execute - should catch Framework Crash and report as FAILED`() {

        val (request, childDescriptor) = setupExecutionEnvironment()

        whenever(mockFactory.createExecutor()).doThrow(RuntimeException("Factory Explosion"))

        engine.execute(request)
        
        val captor = argumentCaptor<TestExecutionResult>()
        verify(listener).executionFinished(org.mockito.kotlin.eq(childDescriptor), captor.capture())

        assertEquals(TestExecutionResult.Status.FAILED, captor.firstValue.status)
        assertTrue(captor.firstValue.throwable.get().message!!.contains("Factory Explosion"))
    }


    private fun setupExecutionEnvironment(): Pair<ExecutionRequest, KontraktTestDescriptor> {
        val rootDescriptor = KontraktTestDescriptor(UniqueId.forEngine(engine.id), "Root")

        val target = DiscoveredTestTarget.create(String::class, "Target", "Target").getOrThrow()
        val mockSpec = mock<TestSpecification> { on { this.target } doReturn target }

        val childDescriptor = KontraktTestDescriptor(rootDescriptor.uniqueId.append("spec", "1"), "Child", mockSpec)
        rootDescriptor.addChild(childDescriptor)

        val configParams = mock<ConfigurationParameters>()

        return Pair(ExecutionRequest(rootDescriptor, listener, configParams), childDescriptor)
    }

    private fun createTestResult(status: TestStatus): TestResult {
        return TestResult(
            target = DiscoveredTestTarget.create(String::class, "T", "T").getOrThrow(),
            finalStatus = status,
            duration = Duration.ZERO,
            assertionRecords = emptyList()
        )
    }
}
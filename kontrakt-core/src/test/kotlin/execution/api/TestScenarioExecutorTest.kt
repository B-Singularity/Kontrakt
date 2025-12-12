package execution.api

import discovery.api.Positive
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveredTestTarget
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.spi.MockingEngine
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class TestScenarioExecutorTest {

    protected abstract val executor: TestScenarioExecutor
    
    interface TestContract {
        fun validMethod(input: String): String

        @Positive
        fun violationMethod(): Int

        fun errorMethod()

        fun complexParams(a: Int, b: String): String
    }

    class TestImplementation : TestContract {
        override fun validMethod(input: String): String = "Echo: $input"
        override fun violationMethod(): Int = -1
        override fun errorMethod() {
            throw IllegalStateException("Boom!")
        }

        override fun complexParams(a: Int, b: String): String = "$a-$b"
    }

    interface GhostContract {
        fun ghostMethod()
    }

    class GhostImplementation


    @Test
    fun `executeScenarios - should execute valid method and return PASSED`() {

        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("validMethod") }!!
        assertEquals(AssertionStatus.PASSED, record.status)
        assertEquals("Success", record.actual)
    }

    @Test
    fun `executeScenarios - should inject generated arguments for multiple parameters`() {

        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("complexParams") }!!
        assertEquals(AssertionStatus.PASSED, record.status)
    }

    @Test
    fun `executeScenarios - should capture ContractViolationException and return FAILED`() {

        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("violationMethod") }!!
        assertEquals(AssertionStatus.FAILED, record.status)
        assertTrue(record.message.contains("Contract Violation"), "Should report violation")
        assertEquals("Violation", record.actual)
    }

    @Test
    fun `executeScenarios - should capture RuntimeException and return FAILED`() {

        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("errorMethod") }!!
        assertEquals(AssertionStatus.FAILED, record.status)
        assertTrue(record.message.contains("threw an exception"), "Should report exception")
        assertEquals("IllegalStateException", record.actual)
    }

    @Test
    fun `executeScenarios - should return empty list if implementation does not implement interface`() {

        val context = setupContext(GhostImplementation::class, GhostImplementation())

        val results = executor.executeScenarios(context)

        assertTrue(results.isEmpty(), "Should return empty list when no interface is found")
    }


    protected fun setupContext(targetClass: kotlin.reflect.KClass<*>, implInstance: Any): EphemeralTestContext {
        val mockContext = mock<EphemeralTestContext>()
        val mockSpec = mock<TestSpecification>()
        val mockTarget = mock<DiscoveredTestTarget>()
        val mockEngine = mock<MockingEngine>()

        whenever(mockEngine.createMock<Any>(any())).thenAnswer { "DummyArg" }

        whenever(mockContext.getTestTarget()).thenReturn(implInstance)
        whenever(mockContext.specification).thenReturn(mockSpec)
        whenever(mockContext.mockingEngine).thenReturn(mockEngine)

        whenever(mockSpec.target).thenReturn(mockTarget)

        whenever(mockTarget.kClass).doReturn(targetClass)

        return mockContext
    }
}
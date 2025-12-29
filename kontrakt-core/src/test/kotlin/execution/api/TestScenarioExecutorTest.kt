package execution.api

import discovery.api.Positive
import discovery.domain.aggregate.TestSpecification
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.spi.MockingEngine
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

interface TestContract {
    fun validMethod(input: String): String

    @Positive
    fun violationMethod(): Int

    fun errorMethod()

    fun complexParams(a: Int, b: String): String
}

open class TestImplementation : TestContract {
    override fun validMethod(input: String): String = "Echo: $input"
    override fun violationMethod(): Int = -1
    override fun errorMethod(): Unit = throw IllegalStateException("Boom!")
    override fun complexParams(a: Int, b: String): String = "$a-$b"
}

interface GhostContract {
    fun ghostMethod()
}

class GhostImplementation

abstract class TestScenarioExecutorTest {
    protected abstract val executor: TestScenarioExecutor

    @Test
    fun `executeScenarios - should execute valid method and return PASSED`() {
        val context = setupContext(TestImplementation::class, TestImplementation())
        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("validMethod") }
        assertNotNull(record, "Scenario 'validMethod' was not executed. Results: ${results.map { it.message }}")

        assertEquals(AssertionStatus.PASSED, record.status)
        // Actual implementation returns "Success" string on pass, not the return value
        assertEquals("Success", record.actual)
    }

    @Test
    fun `executeScenarios - should inject generated arguments for multiple parameters`() {
        val context = setupContext(TestImplementation::class, TestImplementation())
        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("complexParams") }
        assertNotNull(record, "Scenario 'complexParams' was not executed.")

        assertEquals(AssertionStatus.PASSED, record.status)
        // Actual implementation returns "Success" string on pass
        assertEquals("Success", record.actual)
    }

    @Test
    fun `executeScenarios - should capture ContractViolationException and return FAILED`() {
        val context = setupContext(TestImplementation::class, TestImplementation())
        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("violationMethod") }
        assertNotNull(record, "Scenario 'violationMethod' was not executed.")

        assertEquals(AssertionStatus.FAILED, record.status)
        // Actual implementation returns "Violation" string on contract failure
        assertEquals("Violation", record.actual)
        assertTrue(record.message.contains("Contract Violation") || record.message.contains("Constraint"))
    }

    @Test
    fun `executeScenarios - should capture RuntimeException and return FAILED`() {
        val context = setupContext(TestImplementation::class, TestImplementation())
        val results = executor.executeScenarios(context)

        val record = results.find { it.message.contains("errorMethod") }
        assertNotNull(record, "Scenario 'errorMethod' was not executed.")

        assertEquals(AssertionStatus.FAILED, record.status)
        // Actual implementation returns the exception simple class name
        assertEquals("IllegalStateException", record.actual)
        assertTrue(record.message.contains("Boom!") || record.message.contains("threw an exception"))
    }

    @Test
    fun `executeScenarios - should return empty list if implementation does not implement interface`() {
        val context = setupContext(GhostImplementation::class, GhostImplementation())
        val results = executor.executeScenarios(context)

        assertTrue(results.isEmpty(), "Should return empty list for GhostImplementation")
    }

    protected fun setupContext(
        targetClass: KClass<*>,
        implInstance: Any,
    ): EphemeralTestContext {
        val mockContext = mock<EphemeralTestContext>()
        val mockSpec = mock<TestSpecification>()
        val mockEngine = mock<MockingEngine>()

        // Configure ContractAuto mode (Required for the loop to run)
        val modes = if (implInstance is TestContract) {
            setOf(TestSpecification.TestMode.ContractAuto(TestContract::class))
        } else {
            emptySet()
        }
        whenever(mockSpec.modes).thenReturn(modes)
        whenever(mockSpec.seed).thenReturn(12345L)

        // Mocking Engine configuration
        whenever(mockEngine.createMock<Any>(any<KClass<Any>>())).thenAnswer { invocation ->
            val type = invocation.arguments[0] as KClass<*>
            when (type) {
                Int::class -> 100
                String::class -> "TestString"
                else -> "Unknown"
            }
        }

        // Link context components
        whenever(mockContext.getTestTarget()).thenReturn(implInstance)
        whenever(mockContext.specification).thenReturn(mockSpec)
        whenever(mockContext.mockingEngine).thenReturn(mockEngine)

        return mockContext
    }
}
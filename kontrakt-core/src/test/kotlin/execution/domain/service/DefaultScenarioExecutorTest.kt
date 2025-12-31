package execution.domain.service

import exception.ContractViolationException
import execution.api.TestImplementation
import execution.api.TestScenarioExecutor
import execution.api.TestScenarioExecutorTest
import execution.domain.AssertionStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KParameter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultScenarioExecutorTest : TestScenarioExecutorTest() {
    override val executor: TestScenarioExecutor = DefaultScenarioExecutor()

    @Test
    fun `should delegate validation to the injected ContractValidator`() {
        val mockValidator = mock<ContractValidator>()
        val sut =
            DefaultScenarioExecutor(
                validatorFactory = { _ -> mockValidator },
            )
        val context = setupContext(TestImplementation::class, TestImplementation())

        sut.executeScenarios(context)

        verify(mockValidator, atLeastOnce()).validate(any<KAnnotatedElement>(), any())
    }

    @Test
    fun `should delegate argument generation to the injected FixtureGenerator`() {
        val mockGenerator = mock<FixtureGenerator>()

        // Mock must accept 2 arguments: generate(param, context)
        doAnswer { invocation ->
            val param = invocation.arguments[0] as KParameter
            when (param.type.classifier) {
                Int::class -> 123
                String::class -> "MockedString"
                else -> null
            }
        }.whenever(mockGenerator).generate(any<KParameter>(), any())

        val sut =
            DefaultScenarioExecutor(
                fixtureFactory = { _, _ -> mockGenerator },
            )
        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = sut.executeScenarios(context)

        val record = results.find { it.message.contains("complexParams") }
        assertNotNull(record, "Scenario 'complexParams' was not executed.")

        assertEquals(AssertionStatus.PASSED, record.status)
    }

    @Test
    fun `should use the injected Clock for time consistency`() {
        val fixedClock = Clock.fixed(Instant.parse("2099-01-01T00:00:00Z"), ZoneId.of("UTC"))
        val sut = DefaultScenarioExecutor(clock = fixedClock)
        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = sut.executeScenarios(context)

        assertTrue(results.isNotEmpty(), "Results should not be empty")
    }

    @Test
    fun `when Validator throws exception via Mock, it should be handled correctly`() {
        val mockValidator = mock<ContractValidator>()
        val exceptionMessage = "Simulated Validation Error"

        doThrow(ContractViolationException(exceptionMessage))
            .whenever(mockValidator)
            .validate(any(), any())

        val sut =
            DefaultScenarioExecutor(
                validatorFactory = { _ -> mockValidator },
            )
        val context = setupContext(TestImplementation::class, TestImplementation())

        val results = sut.executeScenarios(context)

        val record = results.find { it.message.contains("validMethod") }
        assertNotNull(record, "Scenario 'validMethod' was not executed.")

        assertEquals(AssertionStatus.FAILED, record.status)
        assertTrue(record.message.contains(exceptionMessage))
    }
}

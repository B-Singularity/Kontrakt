package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import execution.api.TestScenarioExecutor
import execution.domain.aggregate.TestExecution
import execution.domain.service.DefaultScenarioExecutor
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class DefaultRuntimeFactoryTest {

    private val factory = DefaultRuntimeFactory()

    @Test
    fun `createExecution should successfully create instances for various input combinations`() {

        data class TestCase(
            val spec: TestSpecification,
            val executor: TestScenarioExecutor,
            val description: String
        )

        val mockSpecA = mock<TestSpecification>()
        val mockExecutorA = mock<TestScenarioExecutor>()

        val mockSpecB = mock<TestSpecification>()
        val mockExecutorB = mock<TestScenarioExecutor>()

        val testCases = listOf(
            TestCase(mockSpecA, mockExecutorA, "Case 1: First set of mocks"),
            TestCase(mockSpecB, mockExecutorB, "Case 2: Second set of mocks (distinct instances)"),
            TestCase(mockSpecA, mockExecutorB, "Case 3: Mixed instances")
        )

        testCases.forEachIndexed { index, testCase ->
            val result = factory.createExecution(testCase.spec, testCase.executor)

            assertNotNull(
                result,
                "Failed at [#$index] ${testCase.description}: Result should not be null"
            )
            assertIs<TestExecution>(
                result,
                "Failed at [#$index] ${testCase.description}: Should return TestExecution type"
            )
        }
    }

    @Test
    fun `createExecutor should return DefaultScenarioExecutor`() {

        val executor = factory.createExecutor()

        assertNotNull(executor)
        assertIs<DefaultScenarioExecutor>(executor)
    }
}
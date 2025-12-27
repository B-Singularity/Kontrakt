package execution.domain.aggregate

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveredTestTarget
import exception.KontraktInternalException
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.service.TestInstanceFactory
import execution.domain.vo.AssertionRecord
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestExecutionTest {
    private val mockSpec: TestSpecification = mock()
    private val mockFactory: TestInstanceFactory = mock()
    private val mockExecutor: TestScenarioExecutor = mock()
    private val mockContext: EphemeralTestContext = mock()
    private val mockTarget: DiscoveredTestTarget = mock()

    private fun createSUT(): TestExecution {
        whenever(mockSpec.target).thenReturn(mockTarget)
        whenever(mockTarget.displayName).thenReturn("TestTarget")
        return TestExecution(mockSpec, mockFactory, mockExecutor)
    }

    @Test
    fun `execute - should return Passed status when all assertions pass`() {
        val sut = createSUT()
        val passedRecord = AssertionRecord(AssertionStatus.PASSED, "Good", "A", "A")

        whenever(mockFactory.create(mockSpec)).thenReturn(mockContext)
        whenever(mockExecutor.executeScenarios(mockContext)).thenReturn(listOf(passedRecord))

        val result = sut.execute()

        assertIs<TestStatus.Passed>(result.finalStatus)
        assertEquals(1, result.assertionRecords.size)
        assertEquals(mockTarget, result.target)
    }

    @Test
    fun `execute - should return Passed status even if record list is empty`() {
        val sut = createSUT()

        whenever(mockFactory.create(mockSpec)).thenReturn(mockContext)
        whenever(mockExecutor.executeScenarios(mockContext)).thenReturn(emptyList())

        val result = sut.execute()

        assertIs<TestStatus.Passed>(result.finalStatus)
    }

    @Test
    fun `execute - should return AssertionFailed status when at least one assertion fails`() {
        val sut = createSUT()
        val passedRecord = AssertionRecord(AssertionStatus.PASSED, "Good", "A", "A")
        val failedRecord =
            AssertionRecord(
                status = AssertionStatus.FAILED,
                message = "Something went wrong",
                expected = "ExpectedValue",
                actual = "ActualValue",
            )

        whenever(mockFactory.create(mockSpec)).thenReturn(mockContext)
        whenever(mockExecutor.executeScenarios(mockContext)).thenReturn(listOf(passedRecord, failedRecord))

        val result = sut.execute()

        val status = result.finalStatus
        assertIs<TestStatus.AssertionFailed>(status)

        assertEquals("Something went wrong", status.message)
        assertEquals("ExpectedValue", status.expected)
        assertEquals("ActualValue", status.actual)
    }

    @Test
    fun `execute - should return ExecutionError when factory throws exception`() {
        val sut = createSUT()
        val runtimeException = RuntimeException("Instantiation Failed")

        whenever(mockFactory.create(any())).thenThrow(runtimeException)

        val result = sut.execute()

        val status = result.finalStatus
        assertIs<TestStatus.ExecutionError>(status)
        assertEquals(runtimeException, status.cause)
    }

    @Test
    fun `execute - should return ExecutionError when executor throws exception`() {
        val sut = createSUT()
        val runtimeException = IllegalStateException("Executor Crashed")

        whenever(mockFactory.create(any())).thenReturn(mockContext)
        whenever(mockExecutor.executeScenarios(any())).thenThrow(runtimeException)

        val result = sut.execute()

        val status = result.finalStatus
        assertIs<TestStatus.ExecutionError>(status)
        assertEquals(runtimeException, status.cause)
    }

    @Test
    fun `execute - should throw KontraktInternalException if executed more than once`() {
        val sut = createSUT()

        whenever(mockFactory.create(any())).thenReturn(mockContext)
        whenever(mockExecutor.executeScenarios(any())).thenReturn(emptyList())

        sut.execute()

        val exception =
            assertFailsWith<KontraktInternalException> {
                sut.execute()
            }
        assertTrue(exception.message!!.contains("cannot be reused"), "Should fail on double execution")
    }
}

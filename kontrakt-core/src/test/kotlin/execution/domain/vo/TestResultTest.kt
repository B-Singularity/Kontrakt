package execution.domain.vo

import discovery.domain.vo.DiscoveredTestTarget
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestResultTest {

    private val dummyTarget = DiscoveredTestTarget.create(
        kClass = String::class,
        displayName = "Dummy Target",
        fullyQualifiedName = "java.lang.String"
    ).getOrThrow()

    private val ASSERTION_PASS = AssertionStatus.PASSED
    private val ASSERTION_FAIL = AssertionStatus.FAILED

    @Test
    fun `should create result with Passed status`() {
        val duration = Duration.ofMillis(100)

        val record = AssertionRecord(ASSERTION_PASS, "All good", true, true)

        val result = TestResult(
            target = dummyTarget,
            finalStatus = TestStatus.Passed,
            duration = duration,
            assertionRecords = listOf(record)
        )

        assertEquals(TestStatus.Passed, result.finalStatus)
        assertEquals(1, result.assertionRecords.size)
        assertEquals(duration, result.duration)
    }

    @Test
    fun `should create result with AssertionFailed status`() {
        val duration = Duration.ofMillis(200)

        val failedStatus = TestStatus.AssertionFailed(
            message = "Value mismatch",
            expected = 10,
            actual = 5
        )

        val record = AssertionRecord(ASSERTION_FAIL, "Value mismatch", 10, 5)

        val result = TestResult(
            target = dummyTarget,
            finalStatus = failedStatus,
            duration = duration,
            assertionRecords = listOf(record)
        )

        assertTrue(result.finalStatus is TestStatus.AssertionFailed)
        assertEquals("Value mismatch", (result.finalStatus as TestStatus.AssertionFailed).message)
        assertEquals(record, result.assertionRecords[0])
    }

    @Test
    fun `should create result with ExecutionError status`() {
        val exception = RuntimeException("Boom")
        val errorStatus = TestStatus.ExecutionError(exception)

        val result = TestResult(
            target = dummyTarget,
            finalStatus = errorStatus,
            duration = Duration.ZERO,
            assertionRecords = emptyList()
        )

        assertEquals(errorStatus, result.finalStatus)
        assertEquals(exception, (result.finalStatus as TestStatus.ExecutionError).cause)
    }

    @Test
    fun `equality check works with different TestStatus types`() {
        val duration = Duration.ofSeconds(1)
        val records = emptyList<AssertionRecord>()

        val passedResult = TestResult(dummyTarget, TestStatus.Passed, duration, records)
        val abortedResult = TestResult(dummyTarget, TestStatus.Aborted("Timeout"), duration, records)

        assertNotEquals(passedResult, abortedResult)
    }

    @Test
    fun `copy works with Sealed Interface status`() {
        val original = TestResult(
            target = dummyTarget,
            finalStatus = TestStatus.Passed,
            duration = Duration.ofSeconds(1),
            assertionRecords = emptyList()
        )

        val newStatus = TestStatus.AssertionFailed("New Fail", 1, 2)
        val copied = original.copy(finalStatus = newStatus)

        assertEquals(newStatus, copied.finalStatus)
        assertEquals(original.target, copied.target)
    }
}
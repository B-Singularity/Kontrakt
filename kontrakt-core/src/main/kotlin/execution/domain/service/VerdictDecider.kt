package execution.domain.service

import exception.ContractViolationException
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import execution.domain.vo.AssertionRecord

/**
 * [Domain Service] Test Verdict Decider.
 *
 * Encapsulates the logic for determining the final outcome ([TestStatus]) of a test session.
 * This separates the "Decision Policy" from the "Execution & Reporting" mechanism,
 * making the logic easily testable via Unit Tests without mocking infrastructure.
 */
class VerdictDecider {

    /**
     * Decides the final status based on exceptions and assertion records.
     *
     * **Decision Priority:**
     * 1. **Execution Error**: Unexpected exceptions (NPE, OOM, Configuration).
     * 2. **Assertion Failed**: Contract violations or failed checks (even without exceptions).
     * 3. **Passed**: No errors and no failed records.
     *
     * @param error The exception thrown during execution (if any).
     * @param records The list of assertion records collected during execution.
     * @return The comprehensive [TestStatus].
     */
    fun decide(error: Throwable?, records: List<AssertionRecord>): TestStatus {
        // Priority 1: Handle Exceptions
        if (error != null) {
            return mapErrorToStatus(error)
        }

        // Priority 2: Handle "Silent" Failures (Records with FAILED status)
        // Checks if any assertion failed even if no exception was thrown.
        val firstFailure = records.find { it.status == AssertionStatus.FAILED }
        if (firstFailure != null) {
            return TestStatus.AssertionFailed(
                message = firstFailure.message,
                expected = firstFailure.expected,
                actual = firstFailure.actual,
                cause = null
            )
        }

        // Priority 3: Success
        return TestStatus.Passed
    }

    /**
     * Maps raw exceptions to domain-specific statuses.
     */
    private fun mapErrorToStatus(error: Throwable): TestStatus {
        return when (error) {
            // Logical Failures (Contract / Assertion)
            is AssertionError, is ContractViolationException ->
                TestStatus.AssertionFailed(
                    message = error.message ?: "Assertion Failed",
                    expected = "Compliance",
                    actual = "Violation",
                    cause = error,
                )

            // System / Runtime Errors
            else -> TestStatus.ExecutionError(error)
        }
    }
}
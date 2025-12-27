package execution.domain

sealed interface TestStatus {
    data object Passed : TestStatus

    data class AssertionFailed(
        val message: String,
        val expected: Any?,
        val actual: Any?,
    ) : TestStatus

    data class ExecutionError(
        val cause: Throwable,
    ) : TestStatus

    data object Disabled : TestStatus

    data class Aborted(
        val reason: String,
    ) : TestStatus
}

package execution.domain.vo.result

sealed interface TestStatus {
    val isPassed: Boolean
        get() = this is Passed

    data object Passed : TestStatus

    data class AssertionFailed(
        val message: String,
        val expected: Any?,
        val actual: Any?,
        val cause: Throwable? = null,
    ) : TestStatus

    data class ExecutionError(
        val cause: Throwable,
    ) : TestStatus

    data object Disabled : TestStatus

    data class Aborted(
        val reason: String,
    ) : TestStatus
}

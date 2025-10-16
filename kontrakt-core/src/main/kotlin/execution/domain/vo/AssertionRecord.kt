package execution.domain.vo

import execution.domain.AssertionStatus
import execution.domain.TestStatus

data class AssertionRecord(
    val status: AssertionStatus,
    val message : String,
    val excepted: Any?,
    val actual: Any?
)

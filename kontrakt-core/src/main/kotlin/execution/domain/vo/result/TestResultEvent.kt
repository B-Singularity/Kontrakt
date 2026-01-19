package execution.domain.vo.result

import execution.domain.vo.context.WorkerId
import infrastructure.json.escapeJson

data class TestResultEvent(
    val runId: String,
    val testName: String,
    val workerId: WorkerId,
    val status: TestStatus,
    val durationMs: Long,
    val journalPath: String,
    val timestamp: Long,
    val seed: Long,
) {
    fun toJson(): String {
        val sb = StringBuilder()
        sb
            .append("""{"runId":"""")
            .append(runId.escapeJson())
            .append("\"")
            .append(""","testName":"""")
            .append(testName.escapeJson())
            .append(""","workerId":""")
            .append(workerId.value)
            .append(""","seed":""")
            .append(seed)
            .append(""","status":""")
            .append(statusToJson(status))
            .append(""","durationMs":""")
            .append(durationMs)
            .append(""","journalPath":"""")
            .append(journalPath.escapeJson())
            .append("\"")
            .append(""","timestamp":""")
            .append(timestamp)
            .append("}")

        return sb.toString()
    }

    private fun statusToJson(status: TestStatus): String {
        val sb = StringBuilder()
        sb.append("{")

        sb.append(""""type":"""")

        when (status) {
            is TestStatus.Passed -> {
                sb.append("Passed").append("\"")
            }

            is TestStatus.AssertionFailed -> {
                sb.append("AssertionFailed").append("\"")
                sb.append(""","message":"""").append(status.message.escapeJson()).append("\"")
                sb.append(""","expected":"""").append(status.expected.toString().escapeJson()).append("\"")
                sb.append(""","actual":"""").append(status.actual.toString().escapeJson()).append("\"")
            }

            is TestStatus.ExecutionError -> {
                sb.append("ExecutionError").append("\"")
                sb
                    .append(""","cause":"""")
                    .append(
                        status.cause.javaClass.name
                            .escapeJson(),
                    ).append("\"")
                sb.append(""","message":"""").append(status.cause.message?.escapeJson() ?: "null").append("\"")
            }

            is TestStatus.Disabled -> {
                sb.append("Disabled").append("\"")
            }

            is TestStatus.Aborted -> {
                sb.append("Aborted").append("\"")
                sb.append(""","reason":"""").append(status.reason.escapeJson()).append("\"")
            }
        }

        sb.append("}")
        return sb.toString()
    }
}

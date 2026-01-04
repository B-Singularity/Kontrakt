package execution.domain.vo

import execution.domain.TestStatus
import infrastructure.json.escapeJson

data class TestResultEvent(
    val runId: String,
    val workerId: Int,
    val status: TestStatus,
    val durationMs: Long,
    val journalPath: String,
    val failureMessage: String? = null,
    val timestamp: Long
) {
    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("""{"runId":"""").append(runId.escapeJson()).append("\"")
            .append(""","workerId":""").append(workerId)
            .append(""","status":"""").append(status).append("\"")
            .append(""","durationMs":""").append(durationMs)
            .append(""","journalPath":"""").append(journalPath.escapeJson()).append("\"")
            .append(""","failureMessage":""")

        if (failureMessage != null) {
            sb.append("\"").append(failureMessage.escapeJson()).append("\"")
        } else {
            sb.append("null")
        }

        sb.append(""","timestamp":""").append(timestamp)
            .append("}")

        return sb.toString()
    }
}

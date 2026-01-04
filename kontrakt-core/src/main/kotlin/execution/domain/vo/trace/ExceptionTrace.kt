package execution.domain.vo.trace

import infrastructure.json.escapeJson

data class ExceptionTrace(
    val exceptionType: String,
    val message: String,
    val stackTraceElements: List<StackTraceElement>,
    override val timestamp: Long
) : TraceEvent {
    override val phase = TracePhase.VERIFICATION

    override fun toNdjson(): String {
        val sb = StringBuilder()
        sb.append("""{"ts":""").append(timestamp)
            .append(""","ph":"EXCEPTION"""")
            .append(""","type":"""").append(exceptionType.escapeJson()).append("\"")
            .append(""","msg":"""").append(message.escapeJson()).append("\"")
            .append(""","stack":[""")

        val iterator = stackTraceElements.iterator()
        while (iterator.hasNext()) {
            val elem = iterator.next()
            val line = "${elem.className}.${elem.methodName}(${elem.fileName}:${elem.lineNumber})"
            sb.append("\"").append(line.escapeJson()).append("\"")
            if (iterator.hasNext()) sb.append(",")
        }
        sb.append("]}")
        return sb.toString()
    }
}

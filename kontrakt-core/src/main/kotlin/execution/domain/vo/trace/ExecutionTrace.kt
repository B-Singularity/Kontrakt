package execution.domain.vo.trace

import infrastructure.json.escapeJson

data class ExecutionTrace(
    val methodSignature: String,
    val arguments: List<String>,
    val durationMs: Long,
    override val timestamp: Long
) : TraceEvent {
    override val phase = TracePhase.EXECUTION

    override fun toNdjson(): String {
        val sb = StringBuilder()

        sb.append("""{"ts":""").append(timestamp)
            .append(""","ph":"$phase"""")
            .append(""","sig":"""").append(methodSignature.escapeJson()).append("\"")
            .append(""","args":[""")

        val iterator = arguments.iterator()
        while (iterator.hasNext()) {
            sb.append('"').append(iterator.next().escapeJson()).append("\"")
            if (iterator.hasNext()) sb.append(',')
        }

        sb.append("""]""")
            .append(""","dur":""").append(durationMs)
            .append("}")

        return sb.toString()
    }
}

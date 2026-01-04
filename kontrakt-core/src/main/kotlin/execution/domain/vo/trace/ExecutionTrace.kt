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

        sb.append("""{"timestamp":""").append(timestamp)
            .append(""","phase":"$phase"""")
            .append(""","methodSignature":"""").append(methodSignature.escapeJson()).append("\"")
            .append(""","arguments":[""")

        val iterator = arguments.iterator()
        while (iterator.hasNext()) {
            sb.append('"').append(iterator.next().escapeJson()).append("\"")
            if (iterator.hasNext()) sb.append(',')
        }

        sb.append("""]""")
            .append(""","durationMs":""").append(durationMs)
            .append("}")

        return sb.toString()
    }
}

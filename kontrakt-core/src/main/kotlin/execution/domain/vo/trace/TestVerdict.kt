package execution.domain.vo.trace

import execution.domain.TestStatus

data class TestVerdict(
    val status: TestStatus,
    val durationTotalMs: Long,
    override val timestamp: Long,
) : TraceEvent {
    override val phase = TracePhase.RESULT

    override fun toNdjson(): String {
        // Performance Optimization: Using StringBuilder to avoid allocation overhead
        val sb = StringBuilder()
        sb.append("""{"timestamp":""").append(timestamp)
            .append(""","phase":"$phase"""")
            // Serialize status as a simple string (e.g., "Passed", "AssertionFailed")
            .append(""","status":"${status::class.simpleName}"""")
            .append(""","durationTotalMs":""").append(durationTotalMs)
            .append("}")
        return sb.toString()
    }
}

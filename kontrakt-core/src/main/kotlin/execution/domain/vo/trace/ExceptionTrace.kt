package execution.domain.vo.trace

import exception.KontraktException

data class ExceptionTrace(
    val exception: Throwable,
    override val timestamp: Long,
    override val phase: TracePhase
) : TraceEvent {
    override val eventType: String = "EXCEPTION"

    override val details: Map<String, Any?> = mapOf(
        "type" to exception.javaClass.name,
        "message" to (exception.message ?: ""),
        "stackTrace" to exception.stackTrace.take(5).map { it.toString() }, // Stack summary
        "domain" to if (exception is KontraktException) exception.domain else "UNKNOWN"
    )

    override val isCritical: Boolean = true
}
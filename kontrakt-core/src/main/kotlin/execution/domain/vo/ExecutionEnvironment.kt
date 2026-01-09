package execution.domain.vo

import execution.domain.vo.trace.ExecutionTrace
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CopyOnWriteArrayList

data class ExecutionEnvironment(
    val trace: TraceInfo,
    val auth: AuthInfo,
    val tenant: TenantInfo,
    val request: RequestInfo,
)

data class TraceInfo(
    val traceId: String,
    val spanId: String,
    val parentSpanId: String? = null,
    val sampled: Boolean,
    val decisions: MutableList<ExecutionTrace> = CopyOnWriteArrayList(),
) {
    fun nextSpan(): TraceInfo =
        copy(
            spanId =
                java.util.UUID
                    .randomUUID()
                    .toString(),
            parentSpanId = this.spanId,
            decisions = CopyOnWriteArrayList(),
        )
}

data class AuthInfo(
    val userId: String,
    val roles: Set<String> = emptySet(),
    val attributes: Map<String, Any> = emptyMap(),
)

data class TenantInfo(
    val tenantId: String,
    val locale: String,
    val zoneId: ZoneId,
)

data class RequestInfo(
    val requestId: String,
    val clientIp: String?,
    val userAgent: String?,
    val timestamp: Instant,
)

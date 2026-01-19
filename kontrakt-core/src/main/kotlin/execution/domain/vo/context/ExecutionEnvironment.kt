package execution.domain.vo.context

import execution.domain.vo.trace.ExecutionTrace
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

data class ExecutionEnvironment(
    /**
     * The source of randomness for this execution.
     * Seeded by the Engine to ensure reproducibility.
     */
    val random: Random,
    /**
     * The time provider. Fixed or Real-time depending on configuration.
     */
    val clock: Clock,
    // --- Metadata (Context) ---
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
)

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

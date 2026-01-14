package execution.domain.factory

import execution.domain.vo.AuthInfo
import execution.domain.vo.ExecutionEnvironment
import execution.domain.vo.RequestInfo
import execution.domain.vo.TenantInfo
import execution.domain.vo.TraceInfo
import java.time.Clock
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

class ExecutionEnvironmentFactory(
    private val clock: Clock,
    private val localeProvider: () -> String = { System.getProperty("user.language", "en") },
    private val zoneIdProvider: () -> ZoneId = { ZoneId.systemDefault() },
    private val traceSampled: Boolean = true,
) {
    fun create(seed: Long): ExecutionEnvironment {
        val randomSource = Random(seed)
        
        return ExecutionEnvironment(
            random = randomSource,
            clock = clock,
            trace = createTrace(),
            auth = createAnonymousAuth(),
            tenant = createDefaultTenant(),
            request = createRequest(),
        )
    }

    private fun createTrace(): TraceInfo =
        TraceInfo(
            traceId = UUID.randomUUID().toString(),
            spanId = UUID.randomUUID().toString(),
            sampled = traceSampled,
        )

    private fun createAnonymousAuth() =
        AuthInfo(
            userId = "anonymous",
        )

    private fun createDefaultTenant() =
        TenantInfo(
            tenantId = "default",
            locale = localeProvider(),
            zoneId = zoneIdProvider(),
        )

    private fun createRequest() =
        RequestInfo(
            requestId = UUID.randomUUID().toString(),
            clientIp = null,
            userAgent = "Kontrakt-Test-Runner",
            timestamp = clock.instant(),
        )
}

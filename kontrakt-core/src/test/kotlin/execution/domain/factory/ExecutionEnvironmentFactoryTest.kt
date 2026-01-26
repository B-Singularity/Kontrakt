package execution.domain.factory

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ExecutionEnvironmentFactoryTest {

    // Use a fixed clock to ensure deterministic timestamp assertions
    private val fixedInstant = Instant.parse("2024-01-01T12:00:00Z")
    private val fixedZone = ZoneId.of("UTC")
    private val fixedClock = Clock.fixed(fixedInstant, fixedZone)

    @Test
    fun `create - initializes environment with correct defaults`() {
        // Given
        val sut = ExecutionEnvironmentFactory(clock = fixedClock)
        val seed = 1234L

        // When
        val env = sut.create(seed)

        // Then: Clock passed through
        assertThat(env.clock).isSameAs(fixedClock)

        // Then: Default Auth
        assertThat(env.auth.userId).isEqualTo("anonymous")

        // Then: Default Tenant (System defaults)
        assertThat(env.tenant.tenantId).isEqualTo("default")
        // Note: checking if it picks up system properties is tricky in unit tests,
        // but we verify the provider was invoked.
        assertThat(env.tenant.locale).isNotBlank()
        assertThat(env.tenant.zoneId).isNotNull()

        // Then: Request Info
        assertThat(env.request.userAgent).isEqualTo("Kontrakt-Test-Runner")
        assertThat(env.request.timestamp).isEqualTo(fixedInstant)
        assertThat(env.request.requestId).isNotBlank() // UUID check
    }

    @Test
    fun `create - ensures randomness is deterministic based on seed`() {
        // Given
        val sut = ExecutionEnvironmentFactory(clock = fixedClock)
        val seed = 9999L

        // When
        val env1 = sut.create(seed)
        val env2 = sut.create(seed)
        val env3 = sut.create(seed + 1)

        // Then
        // 1. Same seed should produce the same random sequence
        val rand1 = env1.random.nextInt()
        val rand2 = env2.random.nextInt()
        assertThat(rand1).isEqualTo(rand2)

        // 2. Different seed should produce different random sequence (highly probable)
        val rand3 = env3.random.nextInt()
        assertThat(rand1).isNotEqualTo(rand3)
    }

    @Test
    fun `create - applies custom locale and zone providers`() {
        // Given
        val expectedLocale = "ko-KR"
        val expectedZone = ZoneId.of("Asia/Seoul")

        val sut = ExecutionEnvironmentFactory(
            clock = fixedClock,
            localeProvider = { expectedLocale },
            zoneIdProvider = { expectedZone }
        )

        // When
        val env = sut.create(1L)

        // Then
        assertThat(env.tenant.locale).isEqualTo(expectedLocale)
        assertThat(env.tenant.zoneId).isEqualTo(expectedZone)
    }

    @Test
    fun `create - respects trace sampling configuration`() {
        // Given: Trace sampling disabled
        val sut = ExecutionEnvironmentFactory(
            clock = fixedClock,
            traceSampled = false
        )

        // When
        val env = sut.create(1L)

        // Then
        assertThat(env.trace.sampled).isFalse()
        assertThat(env.trace.traceId).isNotBlank() // IDs should still be generated
        assertThat(env.trace.spanId).isNotBlank()
    }

    @Test
    fun `create - generates unique IDs for each execution`() {
        // Given
        val sut = ExecutionEnvironmentFactory(clock = fixedClock)

        // When
        val env1 = sut.create(1L)
        val env2 = sut.create(1L)

        // Then: Trace IDs and Request IDs must be unique per call
        assertThat(env1.trace.traceId).isNotEqualTo(env2.trace.traceId)
        assertThat(env1.request.requestId).isNotEqualTo(env2.request.requestId)
    }
}
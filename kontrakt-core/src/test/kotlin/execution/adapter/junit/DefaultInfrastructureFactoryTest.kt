package execution.adapter.junit

import execution.adapter.reporting.BroadcastingResultPublisher
import execution.adapter.trace.WorkerTraceSinkPool
import execution.domain.vo.config.AuditPolicy
import execution.port.outgoing.TestResultPublisher
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories
import org.junit.jupiter.api.Test
import reporting.adapter.outgoing.console.AnsiTheme
import reporting.adapter.outgoing.console.ConsoleReporter
import reporting.adapter.outgoing.console.NoColorTheme
import reporting.adapter.outgoing.console.StandardConsoleLayout
import java.nio.file.Paths

class DefaultInfrastructureFactoryTest {
    
    @Test
    fun `resolveTheme - returns AnsiTheme when env is null or empty`() {
        // null input -> AnsiTheme
        assertThat(DefaultInfrastructureFactory.resolveTheme(null)).isEqualTo(AnsiTheme)

        // empty input -> AnsiTheme
        assertThat(DefaultInfrastructureFactory.resolveTheme("")).isEqualTo(AnsiTheme)
    }

    @Test
    fun `resolveTheme - returns NoColorTheme when env is set`() {
        // value present -> NoColorTheme
        assertThat(DefaultInfrastructureFactory.resolveTheme("true")).isEqualTo(NoColorTheme)
        assertThat(DefaultInfrastructureFactory.resolveTheme("1")).isEqualTo(NoColorTheme)
    }


    @Test
    fun `createConsoleReporter - wires components correctly`() {
        // Given
        val auditPolicy = AuditPolicy.DEFAULT

        // When
        val reporter = DefaultInfrastructureFactory.createConsoleReporter(auditPolicy)

        // Then
        assertThat(reporter).isInstanceOf(ConsoleReporter::class.java)

        assertThat(reporter)
            .extracting("layout")
            .isInstanceOf(StandardConsoleLayout::class.java)
            .extracting("policy")
            .isEqualTo(auditPolicy)

    }

    @Test
    fun `createResultPublisher - returns BroadcastingResultPublisher with correct delegates`() {
        val mockPublisher1 = mockk<TestResultPublisher>()
        val mockPublisher2 = mockk<TestResultPublisher>()
        val publishers = listOf(mockPublisher1, mockPublisher2)
        val onFailure: (String, Throwable) -> Unit = { _, _ -> }

        val resultPublisher = DefaultInfrastructureFactory.createResultPublisher(publishers, onFailure)

        assertThat(resultPublisher).isInstanceOf(BroadcastingResultPublisher::class.java)

        assertThat(resultPublisher)
            .extracting("publishers")
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .containsExactly(mockPublisher1, mockPublisher2)

        assertThat(resultPublisher)
            .extracting("onPublishFailure")
            .isEqualTo(onFailure)
    }

    @Test
    fun `createTraceSinkPool - returns WorkerTraceSinkPool with correct path`() {
        val path = Paths.get("build/test-traces")

        val pool = DefaultInfrastructureFactory.createTraceSinkPool(path)

        assertThat(pool).isInstanceOf(WorkerTraceSinkPool::class.java)

        assertThat(pool)
            .extracting("rootDir")
            .isEqualTo(path)
    }
}
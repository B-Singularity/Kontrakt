package execution.adapter.trace

import execution.domain.vo.context.WorkerId
import execution.exception.KontraktLifecycleException
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class WorkerTraceSinkPoolTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var sut: WorkerTraceSinkPool

    @BeforeEach
    fun setUp() {
        sut = WorkerTraceSinkPool(rootDir = tempDir)
    }

    @AfterEach
    fun tearDown() {
        sut.close()
        unmockkAll() // Essential: Release constructor mocks
    }

    // =========================================================================
    // 1. getSink() Branch Coverage
    // =========================================================================

    @Test
    fun `getSink - creates new sink if not present (Atomic Initialization)`() {
        // Given
        val workerId = WorkerId(1)

        // When
        val sink = sut.getSink(workerId)

        // Then
        assertThat(sink).isInstanceOf(RecyclingFileTraceSink::class.java)
        assertThat(sink.getJournalPath()).contains("worker-1.ndjson")
    }

    @Test
    fun `getSink - returns existing cached sink if already present`() {
        // Given
        val workerId = WorkerId(1)
        val firstCall = sut.getSink(workerId)

        // When
        val secondCall = sut.getSink(workerId)

        // Then
        assertThat(secondCall).isSameAs(firstCall)
    }

    @Test
    fun `getSink - throws exception if pool is closed`() {
        // Given
        sut.close()

        // When & Then
        assertThatThrownBy {
            sut.getSink(WorkerId(1))
        }.isInstanceOf(KontraktLifecycleException::class.java)
            .hasMessageContaining("pool is already closed")
    }

    // =========================================================================
    // 2. close() Branch Coverage
    // =========================================================================

    @Test
    fun `close - does nothing if pool is empty`() {
        // When: Closing an empty pool
        sut.close()

        // Then: No exception, safe exit
        assertThatThrownBy { sut.getSink(WorkerId(1)) }
            .isInstanceOf(KontraktLifecycleException::class.java)
    }

    @Test
    fun `close - is idempotent`() {
        // Given: Pool with resources
        sut.getSink(WorkerId(1))
        sut.close() // First close

        // When: Second close
        sut.close()

        // Then: Should accept the call gracefully without side effects
    }

    @Test
    fun `close - closes all active sinks`() {
        // Correct Syntax: mockkConstruction with explicit class reference
        mockkConstructor(RecyclingFileTraceSink::class) {
            // Fix: Use 'anyConstructed<RecyclingFileTraceSink>()' instead of 'anyConstruction()'
            every { anyConstructed<RecyclingFileTraceSink>().close() } just Runs

            // Given: Two active workers
            sut.getSink(WorkerId(1))
            sut.getSink(WorkerId(2))

            // When
            sut.close()

            // Then: Verify close was called on created instances
            verify(exactly = 2) { anyConstructed<RecyclingFileTraceSink>().close() }
        }
    }

    @Test
    fun `close - continues closing other sinks even if one fails`() {
        mockkConstructor(RecyclingFileTraceSink::class) {
            val worker1 = WorkerId(1)
            val worker2 = WorkerId(2)

            // Setup:
            // We use a counter to make the FIRST closed sink fail, and the SECOND succeed.
            // Note: Order depends on map iteration, but since we verify 'exactly = 2',
            // we confirm both were attempted regardless of order.
            var closeAttempts = 0
            every { anyConstructed<RecyclingFileTraceSink>().close() } answers {
                closeAttempts++
                if (closeAttempts == 1) {
                    throw RuntimeException("Simulated Close Failure")
                }
            }

            // Init Sinks
            sut.getSink(worker1)
            sut.getSink(worker2)

            // When
            sut.close()

            // Then:
            // 1. The exception from the first sink should be swallowed (logged).
            // 2. The second sink should still be attempted to close.
            verify(exactly = 2) { anyConstructed<RecyclingFileTraceSink>().close() }
        }
    }
}
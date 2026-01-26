package execution.port.outgoing

import execution.domain.vo.trace.TraceEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

interface TraceSinkTest {
// =================================================================================================
    // 1. Dependency Injection Points
    // =================================================================================================

    /**
     * Creates the System Under Test.
     */
    fun createSut(): TraceSink

    /**
     * Helper to retrieve the current content of the sink's storage.
     * Used for verification.
     */
    fun readJournalContent(): String

    /**
     * Helper to create a dummy event that is guaranteed to be persisted immediately
     * (bypassing any potential buffering in the implementation).
     */
    fun createCriticalEvent(): TraceEvent

    // =================================================================================================
    // 2. Contract Tests (Default Methods)
    // =================================================================================================

    @Test
    fun `emit - writes event to storage`() {
        val sut = createSut()
        val event = createCriticalEvent()

        sut.emit(event)

        assertThat(readJournalContent()).contains(event.toNdjson())
    }

    @Test
    fun `reset - clears all stored content`() {
        val sut = createSut()
        val event = createCriticalEvent()
        sut.emit(event)

        // Ensure content exists before reset
        assertThat(readJournalContent()).isNotEmpty()

        sut.reset()

        assertThat(readJournalContent()).isEmpty()
    }

    @Test
    fun `getJournalPath - returns a valid identifier path`() {
        val sut = createSut()

        val path = sut.getJournalPath()

        assertThat(path).isNotBlank()
    }

    @Test
    fun `snapshotTo - copies current content to target location`() {
        val sut = createSut()
        val event = createCriticalEvent()
        sut.emit(event)

        val snapshotName = "snapshot-test.log"

        // Perform snapshot
        val snapshotPath = sut.snapshotTo(snapshotName)

        // Verify snapshot file exists and has content
        val snapshotFile = File(snapshotPath)
        assertThat(snapshotFile).exists()
        assertThat(snapshotFile.readText()).contains(event.toNdjson())
    }
}
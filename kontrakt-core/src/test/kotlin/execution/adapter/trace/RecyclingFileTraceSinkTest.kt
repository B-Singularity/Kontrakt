package execution.adapter.trace

import execution.domain.vo.result.TestStatus
import execution.domain.vo.trace.DesignDecision
import execution.domain.vo.trace.ExceptionTrace
import execution.domain.vo.trace.ExecutionTrace
import execution.domain.vo.trace.TestVerdict
import execution.domain.vo.trace.TraceEvent
import execution.domain.vo.trace.VerificationTrace
import execution.domain.vo.verification.AssertionStatus
import execution.port.outgoing.TraceSink
import execution.port.outgoing.TraceSinkTest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

class RecyclingFileTraceSinkTest : TraceSinkTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var sut: RecyclingFileTraceSink
    private val workerId = 1

    @BeforeEach
    fun setUp() {
        sut = RecyclingFileTraceSink(workerId, tempDir)
    }

    @AfterEach
    fun tearDown() {
        sut.close()
    }

    // =========================================================================
    // Contract Implementation
    // =========================================================================

    override fun createSut(): TraceSink = sut

    override fun readJournalContent(): String {
        sut.forceFlush()
        val path = sut.getJournalPath()
        return if (File(path).exists()) File(path).readText() else ""
    }

    override fun createCriticalEvent(): TraceEvent {
        val event = mockk<ExecutionTrace>(relaxed = true)
        every { event.toNdjson() } returns """{"type":"critical_event"}"""
        return event
    }

    // =========================================================================
    // 1. Directory Creation Coverage
    // =========================================================================

    @Test
    fun `initialization - creates parent directories if they do not exist`() {
        // Given: Deeply nested path
        val deepDir = tempDir.resolve("deep/nested/logs")

        // When
        val deepSut = RecyclingFileTraceSink(99, deepDir)

        // Then
        val logPath = deepDir.resolve("logs/workers/worker-99.ndjson")
        assertThat(logPath.parent).exists()
        assertThat(logPath).exists()

        deepSut.close()
    }

    @Test
    fun `snapshotTo - creates parent directories if they do not exist`() {
        // Given
        val event = createCriticalEvent()
        sut.emit(event)

        // When: Snapshot to non-existent directory
        val targetPath = "snapshots/deep/saved.log"
        val result = sut.snapshotTo(targetPath)

        // Then
        val file = tempDir.resolve(targetPath)
        assertThat(file.parent).exists()
        assertThat(file).exists()
        assertThat(result).isEqualTo(file.toAbsolutePath().toString())
    }

    // =========================================================================
    // 2. Exception Handling Coverage (Internal try-catch blocks)
    // =========================================================================

    @Test
    fun `flushBuffer - handles write exceptions gracefully`() {
        // Given: Fill buffer
        val event = mockk<DesignDecision>(relaxed = true)
        every { event.toNdjson() } returns "buffered-data"
        sut.emit(event)

        injectBrokenFileHandle(sut)

        // When: Force flush (triggers exception internally)
        sut.forceFlush()

        // Then: Should not throw exception
    }

    @Test
    fun `emit - handles direct write exception for critical events`() {
        // Given: Critical Event (Direct Write)
        val event = createCriticalEvent()

        injectBrokenFileHandle(sut)

        // When
        sut.emit(event)

        // Then: Should not throw exception
    }

    @Test
    fun `emit - handles direct write exception for oversized payload`() {
        // Given: Payload larger than buffer (4096)
        val hugeEvent = mockk<DesignDecision>(relaxed = true)
        every { hugeEvent.toNdjson() } returns "A".repeat(5000)

        injectBrokenFileHandle(sut)

        // When
        sut.emit(hugeEvent)

        // Then: Should not throw exception
    }

    @Test
    fun `reset - handles truncate exception gracefully`() {
        injectBrokenFileHandle(sut)

        // When
        sut.reset()

        // Then: Should not throw exception
    }

    @Test
    fun `close - handles exception during resource release`() {
        injectBrokenFileHandle(sut)

        // When
        sut.close()

        // Then: Should not throw exception
    }

    // =========================================================================
    // 3. Shutdown Hook Logic Coverage
    // =========================================================================

    @Test
    fun `shutdownHook - runnable executes forceFlush`() {
        // Retrieve private shutdown hook thread via reflection
        val hookField = RecyclingFileTraceSink::class.java.getDeclaredField("shutdownHook")
        hookField.isAccessible = true
        val hookThread = hookField.get(sut) as Thread

        // Verify it runs without error (line coverage)
        hookThread.run()
    }

    // =========================================================================
    // 4. Edge Cases & Initialization Failure
    // =========================================================================

    @Test
    fun `initialization - handles creation failure gracefully`() {
        // Given: Root is a file, preventing directory creation
        val invalidRootDir = tempDir.resolve("im_a_file")
        Files.createFile(invalidRootDir)

        // When
        val failSut = RecyclingFileTraceSink(workerId, invalidRootDir)

        // Then: Should gracefully degrade (isClosed = true)
        val event = mockk<ExecutionTrace>(relaxed = true)
        failSut.emit(event)

        failSut.close()
    }

    @Test
    fun `snapshotTo - returns failure message on IO error`() {
        val event = mockk<ExecutionTrace>(relaxed = true)
        every { event.toNdjson() } returns "data"
        sut.emit(event)

        // Given: A file acting as a blocker for directory structure
        val blockerFile = tempDir.resolve("blocker_file")
        Files.createFile(blockerFile)

        // Try to create a file UNDER the file (Invalid Path)
        val invalidTarget = "blocker_file/fail.log"

        // When
        val result = sut.snapshotTo(invalidTarget)

        // Then
        assertThat(result).isEqualTo("SNAPSHOT_FAILED")
    }

    @Test
    fun `isCriticalEvent - validates prioritization logic`() {
        val method = RecyclingFileTraceSink::class.java.getDeclaredMethod("isCriticalEvent", TraceEvent::class.java)
        method.isAccessible = true

        val assertions = mapOf(
            TestVerdict(TestStatus.Passed, 100L, 0L) to true,
            ExceptionTrace("RuntimeException", "error", emptyList(), 0L) to true,
            VerificationTrace("Rule", AssertionStatus.PASSED, "detail", 0L) to true,
            ExecutionTrace("methodSignature", emptyList(), 10L, 0L) to true,
            DesignDecision("subject", "strategy", "value", 0L) to false
        )

        assertions.forEach { (event, expected) ->
            val result = method.invoke(sut, event) as Boolean
            assertThat(result)
                .withFailMessage("Event ${event::class.simpleName} should be critical=$expected")
                .isEqualTo(expected)
        }
    }

    // =========================================================================
    // Helper: Reflection Logic
    // =========================================================================

    private fun injectBrokenFileHandle(sink: RecyclingFileTraceSink) {
        val brokenHandle = mockk<RandomAccessFile>(relaxed = true)

        every { brokenHandle.write(any<ByteArray>()) } throws IOException("Simulated Write Error")
        every { brokenHandle.write(any<ByteArray>(), any(), any()) } throws IOException("Simulated Buffer Write Error")
        every { brokenHandle.setLength(any()) } throws IOException("Simulated Truncate Error")
        every { brokenHandle.close() } throws IOException("Simulated Close Error")

        val mockChannel = mockk<java.nio.channels.FileChannel>(relaxed = true)
        every { mockChannel.force(any()) } throws IOException("Simulated Force Error")
        every { brokenHandle.channel } returns mockChannel

        val field = RecyclingFileTraceSink::class.java.getDeclaredField("fileHandle")
        field.isAccessible = true
        field.set(sink, brokenHandle)
    }

    @Test
    fun `emit - flushes buffer when accumulated size exceeds limit`() {
        val fillerSize = 4000
        val fillerEvent = mockk<DesignDecision>(relaxed = true)
        every { fillerEvent.toNdjson() } returns "A".repeat(fillerSize - 1)
        sut.emit(fillerEvent)

        val overflowEvent = mockk<DesignDecision>(relaxed = true)
        every { overflowEvent.toNdjson() } returns "B".repeat(99)
        sut.emit(overflowEvent)

        // Then:
        // 1. The existing 4000 bytes ("A"...) must be flushed to the file.
        val logFile = tempDir.resolve("logs/workers/worker-$workerId.ndjson")
        val content = logFile.readText()
        assertThat(content).contains("A".repeat(fillerSize - 1))

        // 2. The new 100 bytes ("B"...) should remain in the buffer (not yet in the file).
        assertThat(content).doesNotContain("B".repeat(99))
    }

    @Test
    fun `safeCalls - handles null fileHandle without crashing`() {
        val field = RecyclingFileTraceSink::class.java.getDeclaredField("fileHandle")
        field.isAccessible = true
        field.set(sut, null)

        val criticalEvent = createCriticalEvent()
        sut.emit(criticalEvent)

        sut.close()
    }


    @Test
    fun `emit - handles oversized payload safely when fileHandle is null`() {
        // Given: 'fileHandle' is forced to null via reflection
        val field = RecyclingFileTraceSink::class.java.getDeclaredField("fileHandle")
        field.isAccessible = true
        field.set(sut, null)

        // Given: Payload larger than buffer (> 4096 bytes)
        val hugeEvent = mockk<DesignDecision>(relaxed = true)
        every { hugeEvent.toNdjson() } returns "A".repeat(5000)

        // When: emit is called
        // Expected behavior:
        // if (bytes.size > buffer.size) -> fileHandle?.write(bytes)
        // Since fileHandle is null, the write operation is safely skipped.
        sut.emit(hugeEvent)
    }

    @Test
    fun `reset - handles null fileHandle gracefully`() {
        // Given: 'fileHandle' is null
        val field = RecyclingFileTraceSink::class.java.getDeclaredField("fileHandle")
        field.isAccessible = true
        field.set(sut, null)

        // When: reset is called
        // Expected behavior: fileHandle?.setLength(0) is skipped.
        sut.reset()
    }

    @Test
    fun `flushBuffer - skips write when fileHandle is null (Zombie State)`() {
        // Given: Buffer contains data (bufferPos > 0)
        val event = mockk<DesignDecision>(relaxed = true)
        every { event.toNdjson() } returns "buffered-data"
        sut.emit(event)

        // But: fileHandle becomes null unexpectedly
        val field = RecyclingFileTraceSink::class.java.getDeclaredField("fileHandle")
        field.isAccessible = true
        field.set(sut, null)

        // When: forceFlush -> flushBuffer is called
        // Expected behavior:
        // if (bufferPos > 0 && fileHandle != null) -> Returns false, block skipped.
        sut.forceFlush()
    }

    @Test
    fun `close - handles exception when removing shutdown hook fails`() {
        // Given: Mock Runtime to throw an exception during removeShutdownHook
        io.mockk.mockkStatic(Runtime::class)
        val runtimeMock = mockk<Runtime>(relaxed = true)
        every { Runtime.getRuntime() } returns runtimeMock
        every { runtimeMock.removeShutdownHook(any()) } throws IllegalStateException("Hook not found")

        // When: close is called
        // Expected behavior:
        // runCatching { Runtime.getRuntime().removeShutdownHook(...) } swallows the exception.
        sut.close()

        // Cleanup: Unmock static to avoid side effects
        io.mockk.unmockkStatic(Runtime::class)
    }


    @Test
    fun `emit - flushes existing buffer before writing oversized payload`() {
        // Given: Buffer is partially filled (e.g., 100 bytes)
        val smallEvent = mockk<DesignDecision>(relaxed = true)
        every { smallEvent.toNdjson() } returns "small-data"
        sut.emit(smallEvent)

        // Given: A new event that is larger than the buffer (Oversized)
        val hugeEvent = mockk<DesignDecision>(relaxed = true)
        every { hugeEvent.toNdjson() } returns "A".repeat(5000)

        // When: Oversized event is emitted
        sut.emit(hugeEvent)

        // Then:
        // 1. The buffered data ("small-data") must be flushed to disk first.
        // 2. The oversized data must be written immediately after.
        val logFile = tempDir.resolve("logs/workers/worker-$workerId.ndjson")
        val content = logFile.readText()

        assertThat(content)
            .contains("small-data")
            .contains("A".repeat(5000))
    }

    // Covers:
    // if (!workerLogPath.parent.toFile().exists()) { ... }
    // Verifies the "False" branch (Directory already exists).
    @Test
    fun `initialization - skips directory creation if parent already exists`() {
        // Given: The parent directory already exists
        val existingDir = tempDir.resolve("logs/workers")
        Files.createDirectories(existingDir)

        // When: Initializing the sink
        // The logic `if (!exists)` should evaluate to false and skip `Files.createDirectories`.
        val newSut = RecyclingFileTraceSink(workerId, tempDir)

        // Then: Initialization succeeds without error, and file is created inside the existing dir.
        val logPath = existingDir.resolve("worker-$workerId.ndjson")
        assertThat(logPath).exists()

        newSut.close()
    }
}
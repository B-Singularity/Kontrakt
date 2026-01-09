package execution.adapter.trace

import execution.domain.vo.trace.TraceEvent
import execution.port.outgoing.TraceSink
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

/**
 * [Adapter] File System Trace Sink
 *
 * Records audit logs in NDJSON format to the local file system.
 *
 * ## Fail-Safe Design
 * This component is designed to ensure that logging failures do not interrupt
 * the main test execution flow.
 * - **Write Errors**: Logs a warning and swallows the exception.
 * - **Post-Close Calls**: Any calls to [emit] after [close] are silently ignored (Silent Drop).
 */
class FileTraceSink(
    private val rootDir: Path = Path.of("build/kontrakt"),
) : TraceSink {
    private val logger = KotlinLogging.logger {}

    // Worker ID: Unique identifier to prevent physical file conflicts (internally managed).
    private val workerId: String = UUID.randomUUID().toString()
    private val activeLogPath: Path = rootDir.resolve("logs/worker-$workerId.ndjson")

    private var writer: BufferedWriter? = null

    init {
        initializeFile()
    }

    private fun initializeFile() {
        try {
            if (!activeLogPath.parent.exists()) {
                activeLogPath.parent.createDirectories()
            }
            if (!activeLogPath.exists()) {
                activeLogPath.createFile()
            }
            // Open in append mode
            writer = BufferedWriter(FileWriter(activeLogPath.toFile(), true))
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize TraceSink file at $activeLogPath" }
            // If initialization fails, writer remains null, and all subsequent emits will be ignored.
            writer = null
        }
    }

    override fun emit(event: TraceEvent) {
        synchronized(this) {
            // [Silent Drop] Ignore if closed or initialization failed.
            val currentWriter = writer ?: return

            try {
                currentWriter.write(event.toNdjson())
                currentWriter.newLine()
                currentWriter.flush()
            } catch (e: Exception) {
                // [Fail-Safe] Do not propagate exceptions to prevent test interruption.
                logger.warn(e) { "Failed to write trace event: ${event.phase}" }
            }
        }
    }

    override fun getJournalPath(): String = activeLogPath.absolutePathString()

    override fun snapshotTo(targetFileName: String): String {
        synchronized(this) {
            writer?.flush()

            val targetPath = rootDir.resolve(targetFileName)
            try {
                if (!targetPath.parent.exists()) {
                    targetPath.parent.createDirectories()
                }
                Files.copy(activeLogPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                return targetPath.absolutePathString()
            } catch (e: Exception) {
                logger.error(e) { "Failed to snapshot trace log to $targetFileName" }
                return "SNAPSHOT_FAILED"
            }
        }
    }

    override fun reset() {
        synchronized(this) {
            close()
            try {
                Files.deleteIfExists(activeLogPath)
            } catch (e: Exception) {
                logger.warn(e) { "Failed to delete old worker log: $activeLogPath" }
            }
            initializeFile()
        }
    }

    override fun close() {
        synchronized(this) {
            try {
                writer?.close()
            } catch (e: Exception) {
                logger.warn(e) { "Error while closing trace writer" }
            } finally {
                // Explicitly set to null to block subsequent emits.
                writer = null
            }
        }
    }
}

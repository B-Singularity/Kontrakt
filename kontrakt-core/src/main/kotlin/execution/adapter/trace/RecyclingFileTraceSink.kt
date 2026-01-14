package execution.adapter.trace

import execution.domain.vo.trace.DesignDecision
import execution.domain.vo.trace.ExceptionTrace
import execution.domain.vo.trace.ExecutionTrace
import execution.domain.vo.trace.TestVerdict
import execution.domain.vo.trace.TraceEvent
import execution.domain.vo.trace.VerificationTrace
import execution.port.outgoing.TraceSink
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [Infrastructure] Worker-Based Hybrid Journaling Sink.
 *
 * Implements **ADR-017 (Worker-Based Isolation)** and **ADR-021 (Zero-Config Hybrid Journaling)**.
 *
 * This component acts as a high-performance "Black Box Recorder" for Generative Testing.
 * It balances **Forensic Safety** (crash resilience) with **Cloud Efficiency** (IOPS reduction).
 *
 * **Key Mechanisms:**
 * - **File Recycling:** Reuses `worker-{id}.ndjson` to avoid open/close overhead.
 * - **Micro-Batching:** Buffers non-critical logs (4KB) to reduce System Calls by ~30x.
 * - **Smart Flush:** Bypasses buffering for Critical Events (Execution, Verification) to ensure data safety.
 * - **Last Will:** Registers a JVM Shutdown Hook to flush remaining buffers on crash.
 */
class RecyclingFileTraceSink(
    private val workerId: Int,
    private val rootDir: Path,
) : TraceSink {
    private val logger = KotlinLogging.logger {}

    // The dedicated log file for this worker thread.
    // Example: build/kontrakt/logs/workers/worker-1.ndjson
    private val workerLogPath: Path = rootDir.resolve("logs/workers/worker-$workerId.ndjson")

    // 'RandomAccessFile' allows us to truncate content (reset) without closing the file handle.
    // Mode "rw" utilizes OS page cache for performance while ensuring data hits the kernel buffer.
    private var fileHandle: RandomAccessFile? = null

    // Atomic flag to prevent operations after closure.
    private val isClosed = AtomicBoolean(false)

    // [Optimization] 4KB Memory Buffer (Matches typical OS Page Size)
    private val buffer = ByteArray(4096)
    private var bufferPos = 0

    // [Safety Net] Flushes data when JVM terminates unexpectedly
    private val shutdownHook = Thread { this.forceFlush() }

    init {
        initializeHandle()
    }

    /**
     * Initializes the file handle and registers the safety hook.
     * Uses kotlin 'runCatching' for elegant error handling in the cold path.
     */
    private fun initializeHandle() {
        runCatching {
            if (!workerLogPath.parent.toFile().exists()) {
                Files.createDirectories(workerLogPath.parent)
            }
            RandomAccessFile(workerLogPath.toFile(), "rw").apply {
                setLength(0) // Start with a clean slate
            }
        }.onSuccess {
            fileHandle = it
            // Register the safety net immediately upon successful initialization.
            Runtime.getRuntime().addShutdownHook(shutdownHook)
        }.onFailure { e ->
            logger.error(e) { "Failed to initialize worker log: $workerLogPath" }
            isClosed.set(true)
        }
    }

    override fun emit(event: TraceEvent) {
        if (isClosed.get()) return

        // Performance Note: We use try-catch instead of 'runCatching' here because this is a "Hot Path".
        // Creating a Result object for every log emission would cause unnecessary GC pressure.
        try {
            val bytes = (event.toNdjson() + "\n").toByteArray(StandardCharsets.UTF_8)

            // [Strategy: Smart Flush]
            // If the event is critical, flush immediately to preserve the "Cause of Death".
            if (isCriticalEvent(event)) {
                flushBuffer()
                fileHandle?.write(bytes) // Direct System Call
                return
            }

            // [Strategy: Micro-Batching]
            // Flush only if the buffer is full
            if (bufferPos + bytes.size > buffer.size) {
                flushBuffer()
            }

            bytes.copyInto(
                destination = buffer,
                destinationOffset = bufferPos,
                startIndex = 0,
                endIndex = bytes.size
            )
            bufferPos += bytes.size

        } catch (e: Exception) {
            // Intentionally swallow logging errors to keep the test runner alive
        }
    }

    /**
     * Determines the urgency of an event using exhaustive pattern matching.
     * * - **Critical:** Must be written to disk immediately to prevent data loss on crash.
     * - **Non-Critical:** Can be buffered to save IOPS.
     */
    private fun isCriticalEvent(event: TraceEvent): Boolean {
        return when (event) {
            // [MUST SAVE] The final verdict is the most important record (Graceful Shutdown proof).
            is TestVerdict -> true

            // [MUST SAVE] Exceptions explain 'Why it crashed'.
            is ExceptionTrace -> true

            // [MUST SAVE] Verification steps (Assertions) are the core value of the test.
            // Even passed assertions are valuable context if it crashes later.
            is VerificationTrace -> true

            // [SAFE] Execution steps are critical for debugging logic errors.
            // In extremely high-perf mode, this could be toggled, but for MVP/Enterprise, safety first.
            is ExecutionTrace -> true

            // [BUFFER] Generation logs (Design) are high-volume noise.
            // Losing the last 20 generated values is acceptable if we have the Execution context.
            is DesignDecision -> false
        }
    }

    /**
     * Flushes the memory buffer to the OS Kernel (via RandomAccessFile).
     * This incurs a System Call overhead.
     */
    private fun flushBuffer() {
        if (bufferPos > 0 && fileHandle != null) {
            try {
                fileHandle?.write(buffer, 0, bufferPos)
                bufferPos = 0
            } catch (e: Exception) {
                // Ignore write failures during flush
            }
        }
    }

    /**
     * Forces a flush of the buffer and syncs the OS file system cache to the physical disk.
     * Called during snapshots or shutdown.
     */
    fun forceFlush() {
        runCatching {
            flushBuffer()
            // fsync: Forces the OS to write dirty pages to the physical storage device.
            // This is expensive but ensures data survives OS crashes/Power loss.
            fileHandle?.channel?.force(false)
        }
    }

    /**
     * Creates a snapshot of the current log for reporting/debugging.
     * Typically called when a test fails to capture the evidence.
     */
    override fun snapshotTo(targetFileName: String): String {
        forceFlush()

        return runCatching {
            val targetPath = rootDir.resolve(targetFileName)
            if (!targetPath.parent.toFile().exists()) {
                Files.createDirectories(targetPath.parent)
            }
            // Atomic copy of the worker log to the destination.
            Files.copy(workerLogPath, targetPath, StandardCopyOption.REPLACE_EXISTING)

            targetPath.toAbsolutePath().toString()
        }.getOrElse { e ->
            logger.error(e) { "Failed to snapshot worker log to $targetFileName" }
            "SNAPSHOT_FAILED"
        }
    }

    override fun getJournalPath(): String {
        return workerLogPath.toAbsolutePath().toString()
    }

    /**
     * Resets the sink for the next test execution.
     * Instead of deleting the file, it truncates the length to 0 (Recycling).
     */
    override fun reset() {
        bufferPos = 0
        runCatching {
            fileHandle?.setLength(0) // Truncate file (Very fast O(1) operation)
        }
    }

    /**
     * Closes the file handle and unregisters the shutdown hook.
     * Should be called when the Test Engine shuts down.
     */
    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                // Remove the hook since we are closing gracefully.
                runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }

                forceFlush()

                fileHandle?.close()
            } catch (e: Exception) {
                logger.warn(e) { "Error closing worker log handle" }
            }
        }
    }
}



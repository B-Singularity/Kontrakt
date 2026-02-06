package execution.adapter.trace

import exception.safety.PayloadSanitizer
import execution.domain.exception.KontraktLifecycleException
import execution.domain.vo.trace.TraceEvent
import execution.domain.vo.trace.TracePhase
import execution.port.outgoing.TraceSink
import infrastructure.json.JsonUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [Adapter] File-based Trace Sink implementation of [TraceSink].
 *
 * ## Responsibilities
 * - **Persistence:** Writes [TraceEvent]s to an append-only NDJSON file.
 * - **Concurrency:** Enforces strict thread confinement to a single Worker Thread.
 * - **Resiliency:** Handles IO and Serialization failures without crashing the worker.
 * - **Lifecycle:** Supports recycling (reset/truncate) to avoid file open/close overhead.
 *
 * ## Safety Mechanisms
 * - **Sanitization:** Uses [PayloadSanitizer] to protect against malicious or huge payloads.
 * - **FD Leak Prevention:** Ensures file descriptors are closed on initialization failures.
 * - **Safety Net:** Forces flush on [TracePhase.RESULT] to prevent log loss.
 *
 * @property workerId The ID of the worker owning this sink.
 * @property rootDir The base directory for log output.
 */
class RecyclingFileTraceSink(
    private val workerId: Int,
    private val rootDir: Path,
) : TraceSink {

    /**
     * The ID of the thread that created this sink.
     * Used to enforce strict thread confinement in [emit].
     */
    override val ownerThreadId: Long = Thread.currentThread().id

    private val logger = KotlinLogging.logger {}
    private val workerLogPath: Path = rootDir.resolve("logs/workers/worker-$workerId.ndjson")

    private var fileHandle: RandomAccessFile? = null
    private val isClosed = AtomicBoolean(false)
    private val buffer = ByteArray(4096)
    private var bufferPosition = 0
    private val flushLock = Any()

    private val shutdownHook = Thread { this.forceFlushAndClose() }

    init {
        initializeHandle()
    }

    /**
     * Opens the RandomAccessFile safely.
     * Guaranteed to close any intermediate resources if initialization fails.
     */
    private fun initializeHandle() {
        var tempFileHandle: RandomAccessFile? = null
        try {
            if (!workerLogPath.parent.toFile().exists()) {
                Files.createDirectories(workerLogPath.parent)
            }
            tempFileHandle = RandomAccessFile(workerLogPath.toFile(), "rw")
            tempFileHandle.setLength(0)
            tempFileHandle.seek(0)

            Runtime.getRuntime().addShutdownHook(shutdownHook)

            fileHandle = tempFileHandle

        } catch (exception: Throwable) {
            logger.error(exception) { "Failed to initialize worker log: $workerLogPath" }
            isClosed.set(true)
            try {
                tempFileHandle?.close()
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Writes a trace event to the journal.
     *
     * @throws KontraktLifecycleException if called from a thread other than [ownerThreadId].
     */
    override fun emit(event: TraceEvent) {
        if (isClosed.get()) return

        if (Thread.currentThread().id != ownerThreadId) {
            throw KontraktLifecycleException(
                component = "TraceSink",
                action = "emit",
                reason = "Thread Confinement Violation! Sink owned by $ownerThreadId but called by ${Thread.currentThread().id}"
            )
        }

        // 1. Serialization Block
        val jsonBytes = try {
            serializeSafe(event)
        } catch (serializationException: Exception) {
            createSerializationFailureMarker(event, serializationException)
        }

        // 2. IO Block
        try {
            synchronized(flushLock) {
                if (isClosed.get()) return

                if (event.isCritical || event.phase == TracePhase.RESULT || jsonBytes.size > buffer.size) {
                    flushBufferLocked()
                    fileHandle?.write(jsonBytes)
                } else {
                    if (bufferPosition + jsonBytes.size > buffer.size) {
                        flushBufferLocked()
                    }
                    jsonBytes.copyInto(buffer, bufferPosition, 0, jsonBytes.size)
                    bufferPosition += jsonBytes.size
                }
            }
        } catch (ioException: Exception) {
            logger.error(ioException) { "TraceSink IO Failure. Closing sink." }
            forceFlushAndClose()
        }
    }

    private fun serializeSafe(event: TraceEvent): ByteArray {
        val safeDetails = PayloadSanitizer.sanitizeMap(event.details)
        val safeEnvelope = mapOf(
            "timestamp" to event.timestamp,
            "phase" to event.phase.name,
            "type" to event.eventType,
            "details" to safeDetails
        )
        val jsonString = JsonUtils.toJson(safeEnvelope)
        return (jsonString + "\n").toByteArray(StandardCharsets.UTF_8)
    }

    private fun createSerializationFailureMarker(event: TraceEvent, error: Throwable): ByteArray {
        val markerDetails = PayloadSanitizer.sanitizeMap(
            mapOf(
                "originalType" to event.eventType,
                "errorType" to error.javaClass.name,
                "error" to (error.message ?: "<no-message>")
            )
        )
        val envelope = mapOf(
            "timestamp" to event.timestamp,
            "phase" to event.phase.name,
            "type" to "SERIALIZATION_FAILURE",
            "details" to markerDetails
        )
        val jsonString = runCatching { JsonUtils.toJson(envelope) }
            .getOrElse { """{"timestamp":${event.timestamp},"type":"SERIALIZATION_FAILURE_CRITICAL","phase":"${event.phase.name}"}""" }

        return (jsonString + "\n").toByteArray(StandardCharsets.UTF_8)
    }

    private fun flushBufferLocked() {
        if (bufferPosition > 0) {
            fileHandle?.write(buffer, 0, bufferPosition)
            bufferPosition = 0
        }
    }

    private fun forceFlushAndClose() {
        if (isClosed.compareAndSet(false, true)) {
            synchronized(flushLock) {
                try {
                    flushBufferLocked()
                    fileHandle?.close()
                } catch (ignored: Exception) {
                } finally {
                    fileHandle = null
                }
            }
        }
    }

    override fun close() {
        try {
            runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }
            forceFlushAndClose()
        } catch (ignored: Exception) {
        }
    }

    override fun getJournalPath(): String = workerLogPath.toAbsolutePath().toString()
    override fun snapshotTo(targetFileName: String): String = "" // Implementation specific

    override fun reset() {
        synchronized(flushLock) {
            bufferPosition = 0
            fileHandle?.setLength(0)
            fileHandle?.seek(0)
        }
    }
}
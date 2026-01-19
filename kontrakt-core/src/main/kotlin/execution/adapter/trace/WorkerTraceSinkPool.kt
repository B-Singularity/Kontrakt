package execution.adapter.trace

import execution.domain.vo.context.WorkerId
import execution.exception.KontraktLifecycleException
import execution.port.outgoing.TraceSink
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * [Infrastructure] Trace Sink Manager.
 *
 * Implements **ADR-017 (Worker-Based Isolation)** strategy.
 * This component is responsible for managing the lifecycle of [RecyclingFileTraceSink] instances.
 *
 * **Key Responsibilities:**
 * 1. **Isolation:** Ensures that each worker thread interacts with its own dedicated log file (Sink).
 * 2. **Lifecycle Safety:** Prevents usage after closure and ensures atomic initialization using [computeIfAbsent].
 * 3. **Thread Safety:** Uses [ConcurrentHashMap] for non-blocking concurrent access.
 */
class WorkerTraceSinkPool(
    private val rootDir: Path = Path.of("build/kontrakt"),
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}

    // Registry to hold active sinks mapped by Worker ID.
    // ConcurrentHashMap ensures thread safety for parallel test execution.
    private val pool = ConcurrentHashMap<WorkerId, RecyclingFileTraceSink>()

    // [Safety] Volatile flag to prevent usage after shutdown.
    @Volatile
    private var isClosed = false

    /**
     * Retrieves the dedicated [TraceSink] for the specified worker.
     *
     * **Concurrency Note:**
     * Uses [ConcurrentHashMap.computeIfAbsent] instead of `getOrPut`.
     * This guarantees that the initialization lambda (file creation) is executed **atomically and at most once** per key,
     * preventing potential race conditions where multiple file handles could be opened for the same worker.
     *
     * @param workerId The strongly typed identifier of the worker.
     * @return The dedicated sink instance.
     * @throws KontraktLifecycleException if the pool has already been closed.
     */
    fun getSink(workerId: WorkerId): TraceSink {
        if (isClosed) {
            throw KontraktLifecycleException(
                componentName = "WorkerTraceSinkPool",
                action = "lease new sink",
                reason = "the pool is already closed (Engine Shutdown Phase).",
            )
        }
        // [Optimization] Atomic check-then-act.
        // Ensures only one file handle is created per worker ID, even under heavy contention.
        return pool.computeIfAbsent(workerId) { id ->
            logger.debug { "Initializing new TraceSink for Worker-${id.value}" }
            RecyclingFileTraceSink(id.value, rootDir)
        }
    }

    /**
     * Closes all managed sinks and releases file system resources.
     *
     * Sets the [isClosed] flag immediately to reject subsequent requests.
     * Uses [runCatching] to ensure a failure in one sink does not block others from closing.
     */
    override fun close() {
        if (isClosed) return
        isClosed = true

        if (pool.isEmpty()) return

        logger.debug { "Closing TraceSinkPool: Releasing ${pool.size} file handles..." }

        pool.values.forEach { sink ->
            // Robust Shutdown: Ensure one failure doesn't stop the cleanup process.
            runCatching {
                sink.close()
            }.onFailure { e ->
                logger.warn(e) { "Failed to close a worker sink during pool shutdown" }
            }
        }
        pool.clear()
    }
}

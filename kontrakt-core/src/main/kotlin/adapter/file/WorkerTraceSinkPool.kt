package adapter.file

import execution.domain.exception.KontraktLifecycleException
import execution.domain.vo.context.WorkerId
import execution.port.outgoing.TraceSink
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [Adapter] Manages a pool of TraceSinks.
 *
 * ## Shutdown Contract
 * The [close] method MUST only be called after all worker threads have been joined/stopped.
 * This prevents "Team Kill" scenarios where a late borrow races with the pool shutdown.
 */
class WorkerTraceSinkPool(
    private val logDirectory: Path,
) : AutoCloseable {
    private val pool = ConcurrentHashMap<WorkerId, TraceSink>()
    private val isClosed = AtomicBoolean(false)

    fun borrowSink(workerId: WorkerId): TraceSink {
        if (isClosed.get()) {
            throw IllegalStateException("WorkerTraceSinkPool is closed. Cannot borrow sink for $workerId")
        }

        val sink =
            pool.computeIfAbsent(workerId) { id ->
                RecyclingFileTraceSink(
                    workerId = id.value,
                    rootDir = logDirectory,
                )
            }

        // Race Condition Check
        if (isClosed.get()) {
            // Because we enforce "Close after Join", hitting this means a logic error in the Engine.
            // We clean up to prevent leaks, even if it might technically be a "Team Kill" for a rogue worker.
            pool.remove(workerId, sink)
            runCatching { sink.close() }
            throw IllegalStateException("WorkerTraceSinkPool closed during borrow operation for $workerId")
        }

        // Thread Confinement Check
        if (sink.ownerThreadId != Thread.currentThread().id) {
            // Strategy: Throw-only (Protect the Victim)
            // We do NOT close the sink here to avoid killing a legitimate worker if this is a spurious check.
            throw KontraktLifecycleException(
                component = "WorkerTraceSinkPool",
                action = "borrowSink",
                reason = "Thread Confinement Violation! Sink for $workerId is owned by thread ${sink.ownerThreadId} but borrowed by ${Thread.currentThread().id}.",
            )
        }

        return sink
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            pool.values.forEach {
                runCatching { it.close() }
            }
            pool.clear()
        }
    }
}

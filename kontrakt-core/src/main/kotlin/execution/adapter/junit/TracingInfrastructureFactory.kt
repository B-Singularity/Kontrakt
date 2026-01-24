package execution.adapter.junit

import execution.adapter.trace.WorkerTraceSinkPool
import java.nio.file.Path

/**
 * [Testing Seam] Factory interface for Tracing Infrastructure.
 *
 * Isolate the creation of tracing components (like file sinks or memory buffers)
 * to support independent evolution and testing of the execution trace pipeline.
 */
interface TracingInfrastructureFactory {
    /**
     * Creates a [WorkerTraceSinkPool] for managing trace files per worker thread.
     *
     * @param path The base directory where trace files should be stored.
     */
    fun createTraceSinkPool(path: Path): WorkerTraceSinkPool
}
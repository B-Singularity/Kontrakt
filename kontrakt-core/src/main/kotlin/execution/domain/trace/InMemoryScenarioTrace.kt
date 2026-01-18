package execution.domain.trace

import execution.domain.vo.trace.TraceEvent
import execution.spi.trace.ScenarioTrace
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * [Domain Entity] Thread-Safe In-Memory Trace.
 *
 * Stores execution events and arguments in a thread-safe manner.
 * Implements a **Bounded Queue** pattern to prevent OutOfMemoryError during long-running tests.
 *
 * Key Features:
 * 1. **Bounded Capacity:** Automatically evicts oldest events when [maxCapacity] is reached.
 * 2. **Non-Blocking:** Uses [ConcurrentLinkedQueue] and [AtomicInteger] for lock-free operations.
 * 3. **Operational Safety:** Ensures counter consistency even under race conditions.
 *
 * @param runId Unique identifier for the test run.
 * @param maxCapacity Maximum number of events to retain (FIFO). Defaults to 100,000.
 */
class InMemoryScenarioTrace(
    override val runId: String,
    private val maxCapacity: Int = 100_000,
) : ScenarioTrace {
    // [Performance] ConcurrentLinkedQueue.size is O(n).
    // We use AtomicInteger to track size in O(1) for high-throughput adding.
    private val _eventCount = AtomicInteger(0)

    // Internal mutable queue (Lock-free)
    private val _events = ConcurrentLinkedQueue<TraceEvent>()

    // Internal mutable arguments map (Lock-free)
    private val _generatedArguments = ConcurrentHashMap<String, Any?>()

    /**
     * Current number of recorded events.
     * Provides O(1) access unlike [ConcurrentLinkedQueue.size] which is O(N).
     */
    val eventCount: Int
        get() = _eventCount.get()

    /**
     * Returns a strictly immutable view of the events.
     * Uses [Collections.unmodifiableList] to enforce the read-only contract at runtime.
     * Note: This returns a snapshot at the time of calling.
     */
    override val events: List<TraceEvent>
        get() = Collections.unmodifiableList(_events.toList())

    /**
     * Returns a strictly immutable view of the arguments.
     * Note: This returns a snapshot at the time of calling.
     */
    override val generatedArguments: Map<String, Any?>
        get() = Collections.unmodifiableMap(_generatedArguments.toMap())

    /**
     * Appends a new event with bounded capacity logic.
     * * **Safety Mechanism:**
     * Enforces the capacity limit by evicting the oldest event if full.
     * Checks for null on poll() to maintain counter consistency.
     */
    override fun add(event: TraceEvent) {
        _events.add(event)

        // Optimistic check: Increment first
        if (_eventCount.incrementAndGet() > maxCapacity) {
            // Attempt eviction
            val evicted = _events.poll()

            // Only decrement if an item was actually removed.
            // This prevents counter drift if the queue was concurrently emptied.
            if (evicted != null) {
                _eventCount.decrementAndGet()
            }
        }
    }

    /**
     * Adds generated arguments.
     * * **Policy: Last Write Wins**
     * If multiple threads generate arguments for the same key, the latest one overwrites.
     */
    override fun addGeneratedArguments(args: Map<String, Any?>) {
        _generatedArguments.putAll(args)
    }
}

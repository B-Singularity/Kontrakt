package planning.infrastructure.runtime.time

/**
 * Internal monotonic time source for runtime-boundary infrastructure.
 *
 * This is intentionally NOT a Domain Core port.
 *
 * Architectural role:
 * - adapter/lane/shard internal runtime utility
 * - source of monotonic elapsed-time measurements for:
 *   - waiter-local timeout deadlines
 *   - adapter-owned quiescence grace handling
 *
 * It must not be used to define semantic planner progress.
 * Semantic planner progress remains governed by explicit cost centers and resolved
 * immutable policy snapshots, not by elapsed time.
 */
internal interface MonotonicTimeSource {
    fun nowNanos(): Long
}

/**
 * Default JVM implementation backed by System.nanoTime().
 *
 * Important:
 * - monotonic, not wall-clock
 * - adapter/runtime concern only
 * - replaceable in tests with deterministic/fake implementations
 */
internal object SystemMonotonicTimeSource : MonotonicTimeSource {
    override fun nowNanos(): Long = System.nanoTime()
}

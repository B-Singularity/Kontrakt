package planning.infrastructure.cache

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.LongAdder

/**
 * Per-key in-flight join slot.
 *
 * Keeps hot-key contention local and prevents duplicate expensive builds.
 * Wait/degrade/fuel policy belongs to the owning adapter/service layer.
 */
class InFlightSlot<V : Any>(
    val future: CompletableFuture<V> = CompletableFuture(),
    val startedAtNanos: Long = System.nanoTime(),
) {
    private val waiters = LongAdder()

    val waitersCount: Long
        get() = waiters.sum()

    fun incrementWaiters() {
        waiters.increment()
    }

    fun decrementWaiters() {
        waiters.decrement()
    }

    fun isExpired(
        nowNanos: Long,
        timeoutNanos: Long,
    ): Boolean {
        return nowNanos - startedAtNanos > timeoutNanos
    }
}
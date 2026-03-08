package planning.infrastructure.cache

import planning.domain.exception.PlanningProtocolIntegrityException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.LongAdder

/**
 * Per-key in-flight join slot.
 *
 * Keeps hot-key contention local and prevents duplicate expensive builds.
 * Wait/degrade/fuel policy belongs to the owning adapter/service layer.
 */
class InFlightSlot<V : Any> private constructor(
    val future: CompletableFuture<V>,
    val startedAtNanos: Long,
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

    companion object {
        @JvmStatic
        fun <V : Any> issue(
            startedAtNanos: Long = System.nanoTime(),
        ): InFlightSlot<V> {
            if (startedAtNanos < 0L) {
                throw PlanningProtocolIntegrityException(
                    "InFlightSlot.startedAtNanos must be >= 0: $startedAtNanos"
                )
            }

            return InFlightSlot(
                future = CompletableFuture(),
                startedAtNanos = startedAtNanos,
            )
        }
    }
}
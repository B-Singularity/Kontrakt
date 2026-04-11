package planning.infrastructure.cache.adapter.outgoing

import ir.plan.node.CanonicalPlanNode
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.port.outgoing.JoinContinuation
import planning.domain.port.outgoing.JoinRegistrationDecision
import planning.domain.port.outgoing.JoinResumeSignal
import planning.domain.runtime.lifecycle.WaiterState
import planning.infrastructure.cache.InFlightSlot
import planning.infrastructure.cache.WaiterCell
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport

/**
 * Deterministic adapter-owned delivery plane for L2 joined-wait continuation dispatch.
 *
 * Architectural position:
 * - This is NOT a domain port.
 * - This is NOT a lifecycle-truth owner.
 * - This is an adapter-local execution-policy surface used by the in-memory
 *   PlanInternRepository adapter and its shard/slot machinery.
 *
 * What this class owns:
 * - continuation registration
 * - already-terminal delivery scheduling
 * - terminal-sweep scheduling
 * - a single delivery thread / queue
 * - orderly shutdown / quiescence waiting
 *
 * What this class does NOT own:
 * - shared-slot truth
 * - waiter truth
 * - builder-handle truth
 * - fault taxonomy
 * - bucket re-verification
 *
 * Design stance:
 * - no caller-thread direct callback execution
 * - single consumer for deterministic callback sequencing
 * - best-effort quiescence barrier for slow administrative paths such as partition drop
 *
 * Important note about queue boundedness:
 * The surrounding orchestration layer does not yet expose an enqueue-rejection surface.
 * For correctness and simplicity, this cut uses a single consumer with an internal queue
 * and avoids caller-thread fallback execution entirely.
 */
internal class DeterministicL2JoinDispatchPlane private constructor(
    workerThreadName: String,
) : L2JoinDispatchPlane, AutoCloseable {

    /**
     * Continuation registry keyed by waiter identity shell.
     *
     * Registry ownership is purely operational:
     * - the waiter remains the owner of waiter-local terminal truth
     * - the slot remains the owner of shared terminal truth
     *
     * This registry only answers:
     * - who should be called when a lawful restart-ready signal becomes deliverable?
     */
    private val registrations =
        ConcurrentHashMap<WaiterCell, RegisteredContinuation>()

    /**
     * Single-consumer task queue.
     *
     * We deliberately avoid direct callback execution on registration and on terminal sweep.
     * All continuation delivery is funneled through the worker thread below.
     */
    private val queue = LinkedBlockingQueue<DispatchTask>()

    /**
     * Delivery-plane liveness.
     */
    private val closed = AtomicBoolean(false)

    /**
     * Number of tasks currently being executed by the delivery thread.
     *
     * This is used only for quiescence waiting on administrative slow paths.
     */
    private val activeTasks = AtomicInteger(0)

    /**
     * Delivery thread.
     *
     * The thread is created eagerly because:
     * - already-terminal delivery must remain adapter-owned
     * - sweep scheduling should not depend on lazy bootstrap races
     */
    private val worker: Thread = Thread(::runLoop, workerThreadName).apply {
        isDaemon = true
        start()
    }

    override fun registerOrDeliverImmediate(
        slot: InFlightSlot<CanonicalPlanNode>,
        waiter: WaiterCell,
        continuation: JoinContinuation,
    ): JoinRegistrationDecision {
        ensureOpen()

        val visibleTerminal = slot.resolveSharedTerminalAcquire()
        if (visibleTerminal != null) {
            enqueueSharedTerminalDelivery(
                slot = slot,
                waiter = waiter,
                continuation = continuation,
            )
            return JoinRegistrationDecision.AlreadyReady
        }

        val previous = registrations.putIfAbsent(
            waiter,
            RegisteredContinuation(
                slot = slot,
                continuation = continuation,
            ),
        )

        if (previous != null) {
            throw PlanningProtocolIntegrityException(
                "Duplicate continuation registration for the same waiter episode is not allowed."
            )
        }

        /*
         * Re-check terminal visibility after registration.
         *
         * This closes the race:
         * - slot was pending at the first check
         * - slot terminalized before/while we installed the registration
         *
         * In that case we immediately upgrade the path into delivery-ready scheduling.
         */
        if (slot.resolveSharedTerminalAcquire() != null) {
            val removed = registrations.remove(waiter)
            if (removed != null) {
                enqueueSharedTerminalDelivery(
                    slot = slot,
                    waiter = waiter,
                    continuation = removed.continuation,
                )
            }
            return JoinRegistrationDecision.AlreadyReady
        }

        return JoinRegistrationDecision.Registered
    }

    override fun enqueueTerminalSweep(
        slot: InFlightSlot<CanonicalPlanNode>,
    ) {
        ensureOpen()
        queue.put(
            DispatchTask.TerminalSweep(
                slot = slot,
            )
        )
    }

    /**
     * Administrative quiescence barrier.
     *
     * This is intentionally conservative and exists for:
     * - partition drop
     * - adapter shutdown
     *
     * It does NOT prove that the entire application is idle.
     * It proves only that this delivery plane has no pending/active work at the moment.
     */
    fun awaitQuiescence(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean {
        if (timeout <= 0L) {
            throw PlanningProtocolIntegrityException(
                "DeterministicL2JoinDispatchPlane.awaitQuiescence requires timeout > 0."
            )
        }

        val deadlineNanos = System.nanoTime() + unit.toNanos(timeout)
        while (System.nanoTime() < deadlineNanos) {
            if (queue.isEmpty() && activeTasks.get() == 0) {
                return true
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1))
        }
        return false
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }

        worker.interrupt()
        try {
            worker.join(5_000L)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        registrations.clear()
        queue.clear()
    }

    // -------------------------------------------------------------------------
    // Worker loop
    // -------------------------------------------------------------------------

    private fun runLoop() {
        while (true) {
            if (closed.get() && queue.isEmpty()) {
                return
            }

            val task = try {
                queue.poll(250, TimeUnit.MILLISECONDS)
            } catch (_: InterruptedException) {
                if (closed.get()) {
                    return
                }
                continue
            } ?: continue

            activeTasks.incrementAndGet()
            try {
                when (task) {
                    is DispatchTask.TerminalSweep -> runTerminalSweep(task.slot)
                    is DispatchTask.DeliverRestartReady -> runRestartReadyDelivery(task)
                }
            } catch (_: Throwable) {
                /*
                 * Delivery-path exceptions are intentionally swallowed here so that:
                 * - one bad continuation does not kill the delivery plane
                 * - slot/waiter truth remains owned by their lifecycle hosts
                 *
                 * The current cut does not install a telemetry/reporting side-channel yet.
                 */
            } finally {
                activeTasks.decrementAndGet()
            }
        }
    }

    /**
     * Terminal sweep over visible waiters for one already-terminal slot.
     *
     * Important:
     * - this does NOT decide shared truth
     * - this does NOT re-verify buckets
     * - this only converts already-visible shared terminality into queued restart-ready
     *   delivery for registered waiters that still remain ATTACHED
     */
    private fun runTerminalSweep(
        slot: InFlightSlot<CanonicalPlanNode>,
    ) {
        slot.forEachVisibleWaiter { waiter ->
            val registration = registrations.remove(waiter) ?: return@forEachVisibleWaiter

            enqueueSharedTerminalDelivery(
                slot = slot,
                waiter = waiter,
                continuation = registration.continuation,
            )
        }

        /*
         * Delivery pending is an operational flag only.
         * Once a sweep pass has been scheduled and executed, the flag no longer needs
         * to remain raised for this slot.
         */
        slot.clearDeliveryPending()
    }

    /**
     * Converts visible shared terminality into one queued restart-ready callback.
     *
     * This helper preserves boundary ownership:
     * - the slot tells us whether shared terminal truth is visible
     * - the waiter decides whether it can still converge through RESUMED
     * - this dispatch plane only queues the callback if both are lawful
     */
    private fun enqueueSharedTerminalDelivery(
        slot: InFlightSlot<CanonicalPlanNode>,
        waiter: WaiterCell,
        continuation: JoinContinuation,
    ) {
        if (slot.resolveSharedTerminalAcquire() == null) {
            return
        }

        /*
         * If ATTACHED wins, shared terminal signal may converge the waiter through RESUMED.
         * If timeout/cancel already won, delivery must not override waiter-local truth.
         */
        val resumedNow = waiter.tryResumeFromSharedSignal(payload = null)
        val stateAfter = waiter.readStateAcquire()

        val mayDeliverRestartSignal =
            resumedNow || stateAfter == WaiterState.RESUMED

        if (!mayDeliverRestartSignal) {
            return
        }

        if (!waiter.tryMarkDeliveryQueued()) {
            return
        }

        queue.put(
            DispatchTask.DeliverRestartReady(
                slot = slot,
                waiter = waiter,
                continuation = continuation,
            )
        )
    }

    /**
     * Executes one continuation callback under the adapter-owned delivery thread.
     *
     * Contract:
     * - never inline on the caller thread
     * - never mutate shared-slot truth here
     * - waiter delivery bookkeeping is updated after callback execution
     */
    private fun runRestartReadyDelivery(
        task: DispatchTask.DeliverRestartReady,
    ) {
        try {
            task.continuation.resume(JoinResumeSignal.ReadyForRestart)
        } finally {
            task.waiter.markDeliveryDoneRelease()
        }
    }

    private fun ensureOpen() {
        if (closed.get()) {
            throw PlanningProtocolIntegrityException(
                "DeterministicL2JoinDispatchPlane is closed."
            )
        }
    }

    private class RegisteredContinuation(
        val slot: InFlightSlot<CanonicalPlanNode>,
        val continuation: JoinContinuation,
    )

    private sealed interface DispatchTask {
        class TerminalSweep(
            val slot: InFlightSlot<CanonicalPlanNode>,
        ) : DispatchTask

        class DeliverRestartReady(
            val slot: InFlightSlot<CanonicalPlanNode>,
            val waiter: WaiterCell,
            val continuation: JoinContinuation,
        ) : DispatchTask
    }

    companion object {
        @JvmStatic
        fun issue(
            workerThreadName: String = "planning-l2-join-dispatch",
        ): DeterministicL2JoinDispatchPlane {
            return DeterministicL2JoinDispatchPlane(
                workerThreadName = workerThreadName,
            )
        }
    }
}
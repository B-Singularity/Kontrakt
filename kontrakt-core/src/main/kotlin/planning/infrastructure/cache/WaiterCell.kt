package planning.infrastructure.cache

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.runtime.lifecycle.L2LifecycleLaw
import planning.domain.runtime.lifecycle.WaiterState
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle

/**
 * Object identity shell for one attached waiter episode in the Planning L2 runtime.
 *
 * ## Why this class exists
 *
 * The Planning L2 runtime must support asynchronous races among multiple event sources:
 *
 * - shared completion delivery,
 * - waiter-local timeout,
 * - waiter-local cancellation,
 * - drop / close sweeps,
 * - panic delivery sweeps,
 * - and delayed callbacks that may arrive after the original slot activity moved on.
 *
 * A waiter therefore cannot be modeled as a loose callback token or an implicit continuation.
 * It must be represented by a dedicated lifecycle host with its own identity boundary.
 *
 * This class provides that identity boundary.
 *
 * ## Architectural position
 *
 * This class is intentionally **not** the owner of shared-slot truth.
 * It is the owner of **waiter-local terminalization truth only**.
 *
 * In other words:
 *
 * - the shared slot decides whether the shared lifecycle converged through
 *   `SUCCESS`, `FAILED`, or `DROPPED`,
 * - the waiter decides only how *this waiter episode* terminalized.
 *
 * The waiter top-level state therefore remains closed and small:
 *
 * - `ATTACHED`
 * - `RESUMED`
 * - `TIMED_OUT`
 * - `CANCELLED`
 *
 * Shared success, shared failure, and shared drop all converge through `RESUMED`
 * on the waiter axis, because the waiter axis models *how this waiter terminalized*,
 * not which shared cause taxonomy existed upstream.
 *
 * ## Ownership
 *
 * This class owns:
 *
 * - waiter-local terminal state,
 * - waiter-local timeout / cancel race coordination,
 * - delivery bookkeeping bits,
 * - callback linkage,
 * - and generation-tagged stale-callback defense material.
 *
 * This class does **not** own:
 *
 * - shared-slot terminal truth,
 * - commit-right arbitration,
 * - partition-region lifecycle,
 * - publication-before-completion law,
 * - or retention / reclamation authority.
 *
 * ## Identity and reclamation model
 *
 * This class is an **object identity shell**.
 * The runtime currently prefers object identity shells for waiter episodes because:
 *
 * - they provide strong identity isolation for asynchronous callback targets,
 * - they avoid premature index reuse / ABA-style confusion,
 * - and they align with the current "no immediate pooling" / "grace-aware reuse only"
 *   direction of the L2 lifecycle mechanics.
 *
 * The `generation` field is immutable and factory-issued. It exists so that
 * future reclamation or delayed callback machinery can reject stale work against
 * the wrong waiter episode without relying on mutable rebinding.
 *
 * ## Concurrency contract
 *
 * Waiter-local truth is carried only by [waiterWord].
 *
 * The packed waiter word currently uses the following conceptual layout:
 *
 * - bits `0..1` : [WaiterState] code
 * - bit `2`     : delivery queued
 * - bit `3`     : delivery done
 *
 * The exact layout is a mechanical detail, but the meaning is stable.
 *
 * All terminalization races must flow through this authority word via CAS.
 * No other field in this class is allowed to become an alternative semantic authority.
 *
 * ## Payload model
 *
 * [terminalEnvelopeRef] is **not** lifecycle authority.
 * It is a winner-published auxiliary payload holder.
 *
 * The authority order is:
 *
 * 1. terminal state wins through CAS on [waiterWord]
 * 2. the winner publishes the envelope with release semantics
 *
 * Readers must interpret the payload only together with the current waiter state.
 * This avoids stale payload consumption and preserves single-winner waiter truth.
 *
 * ## No pooling guarantee
 *
 * This implementation assumes:
 *
 * - no immediate pooling,
 * - no reuse before grace-aware reclamation completion,
 * - and no reassignment of this identity shell to another waiter episode.
 *
 * If pooling is ever introduced later, that change must preserve the same
 * generation-based stale-callback safety law.
 */
internal class WaiterCell private constructor(
    generation: Long,
) {
    /**
     * Sole waiter-axis authority field.
     *
     * This is the only field that decides which top-level waiter state won.
     * Every terminalization path (`RESUMED`, `TIMED_OUT`, `CANCELLED`) must race here,
     * and nowhere else.
     *
     * Delivery bookkeeping bits are co-located in the same word because delivery flags
     * are operational metadata closely associated with the waiter episode, while still
     * remaining subordinate to the top-level waiter state.
     */
    @Volatile
    private var waiterWord: Int = encodeInitialWord()

    /**
     * Intrusive linkage for waiter registry traversal.
     *
     * This field exists for operational registry bookkeeping only.
     * It is **not** lifecycle authority.
     *
     * In particular:
     *
     * - changing [next] must never be interpreted as semantic waiter state change,
     * - a broken linkage must not silently rewrite waiter truth,
     * - and terminalization correctness must not depend on [next] being perfect.
     *
     * The runtime may eventually replace this with another registry mechanism if desired,
     * but the semantic ownership rules above must remain unchanged.
     */
    @Volatile
    var next: WaiterCell? = null

    /**
     * Winner-published terminal payload envelope.
     *
     * This field is intentionally separate from [waiterWord].
     * It is auxiliary publication material, not lifecycle truth.
     *
     * Readers must always validate this payload against the current waiter state.
     * The current implementation does so by embedding the expected terminal state
     * in the envelope itself.
     */
    @Volatile
    private var terminalEnvelopeRef: Any? = null

    /**
     * Immutable generation tag for stale-callback defense.
     *
     * This value is issued at construction time and never changes afterward.
     * Final-field publication is therefore sufficient.
     *
     * The purpose of this field is not immediate behavior in the hot path;
     * it is a correctness boundary for future grace-aware reclamation, delayed callback
     * rejection, and eventual lifecycle-host reuse policies.
     */
    private val generation: Long = generation

    /**
     * Reads the current waiter top-level state with acquire semantics.
     *
     * Acquire semantics are used because callers may subsequently consume payload,
     * delivery flags, or surrounding control flow decisions that rely on the waiter
     * state being observed in a causally safe order.
     */
    fun readStateAcquire(): WaiterState {
        val word = WAITER_WORD_HANDLE.getAcquire(this) as Int
        return decodeState(word)
    }

    /**
     * Returns true if this waiter already converged to a terminal top-level state.
     *
     * This is derived from [readStateAcquire] and therefore respects the same
     * acquire-read discipline.
     */
    fun isTerminalAcquire(): Boolean = readStateAcquire().isTerminal

    /**
     * Returns the immutable generation tag.
     *
     * No acquire read is required because the value is immutable after construction.
     */
    fun readGeneration(): Long = generation

    /**
     * Returns true if the delivery plane has already marked this waiter
     * as queued for delivery.
     *
     * This flag is operational only. It must never be interpreted as semantic
     * waiter terminalization by itself.
     */
    fun isDeliveryQueuedAcquire(): Boolean {
        val word = WAITER_WORD_HANDLE.getAcquire(this) as Int
        return (word and DELIVERY_QUEUED_BIT) != 0
    }

    /**
     * Returns true if the delivery plane has already marked delivery as completed.
     *
     * This flag is also operational only and does not replace waiter terminal truth.
     */
    fun isDeliveryDoneAcquire(): Boolean {
        val word = WAITER_WORD_HANDLE.getAcquire(this) as Int
        return (word and DELIVERY_DONE_BIT) != 0
    }

    /**
     * Attempts waiter convergence by authoritative shared terminal signal.
     *
     * ## Intended use
     *
     * This method is correct when the upstream shared slot has already converged through:
     *
     * - shared `SUCCESS`,
     * - shared `FAILED`,
     * - or shared `DROPPED`.
     *
     * On the waiter axis, all of those causes converge through `RESUMED`.
     *
     * ## Enforcement model
     *
     * [L2LifecycleLaw.requireTransition] is used here as a **fixed legal-edge assertion**:
     * it states that `ATTACHED -> RESUMED` is a lawful waiter transition in the abstract model.
     *
     * It is **not** the dynamic current-state authority.
     * Dynamic truth remains the CAS on [waiterWord].
     *
     * ## Winner rule
     *
     * - exactly one contender wins the waiter CAS
     * - only the winner publishes the terminal envelope
     * - losers return `false` and must not rewrite waiter truth
     */
    fun tryResumeFromSharedSignal(payload: Any?): Boolean {
        L2LifecycleLaw.requireTransition(WaiterState.ATTACHED, WaiterState.RESUMED)

        val envelope =
            TerminalEnvelope.issue(
                terminalState = WaiterState.RESUMED,
                payload = payload,
            )

        while (true) {
            val observed = WAITER_WORD_HANDLE.getAcquire(this) as Int
            val observedState = decodeState(observed)
            if (observedState != WaiterState.ATTACHED) {
                return false
            }

            val updated =
                encodeStatePreservingFlags(
                    currentWord = observed,
                    newState = WaiterState.RESUMED,
                )

            if (WAITER_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                TERMINAL_ENVELOPE_HANDLE.setRelease(this, envelope)
                return true
            }
        }
    }

    /**
     * Attempts waiter-local timeout.
     *
     * ## Invariant
     *
     * Timeout is a waiter-local terminalization outcome only.
     * It must never mutate shared-slot terminal truth.
     *
     * This method therefore asserts two things:
     *
     * 1. `ATTACHED -> TIMED_OUT` is a legal waiter-axis transition
     * 2. timeout must not affect shared-slot state
     *
     * The second invariant is intentionally explicit because it is one of the core
     * constitutional boundaries in the L2 lifecycle law.
     */
    fun tryTimeout(timeoutPayload: Any? = null): Boolean {
        L2LifecycleLaw.requireTransition(WaiterState.ATTACHED, WaiterState.TIMED_OUT)
        L2LifecycleLaw.requireWaiterEventDoesNotAffectSharedSlot(WaiterState.TIMED_OUT)

        val envelope =
            TerminalEnvelope.issue(
                terminalState = WaiterState.TIMED_OUT,
                payload = timeoutPayload,
            )

        while (true) {
            val observed = WAITER_WORD_HANDLE.getAcquire(this) as Int
            val observedState = decodeState(observed)
            if (observedState != WaiterState.ATTACHED) {
                return false
            }

            val updated =
                encodeStatePreservingFlags(
                    currentWord = observed,
                    newState = WaiterState.TIMED_OUT,
                )

            if (WAITER_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                TERMINAL_ENVELOPE_HANDLE.setRelease(this, envelope)
                return true
            }
        }
    }

    /**
     * Attempts waiter-local cancellation.
     *
     * ## Invariant
     *
     * Cancellation is a waiter-local terminalization outcome only.
     * It must never mutate shared-slot terminal truth.
     *
     * Like [tryTimeout], this method asserts both:
     *
     * 1. legal waiter-axis transition
     * 2. explicit non-interference with shared-slot truth
     */
    fun tryCancel(cancellationPayload: Any? = null): Boolean {
        L2LifecycleLaw.requireTransition(WaiterState.ATTACHED, WaiterState.CANCELLED)
        L2LifecycleLaw.requireWaiterEventDoesNotAffectSharedSlot(WaiterState.CANCELLED)

        val envelope =
            TerminalEnvelope.issue(
                terminalState = WaiterState.CANCELLED,
                payload = cancellationPayload,
            )

        while (true) {
            val observed = WAITER_WORD_HANDLE.getAcquire(this) as Int
            val observedState = decodeState(observed)
            if (observedState != WaiterState.ATTACHED) {
                return false
            }

            val updated =
                encodeStatePreservingFlags(
                    currentWord = observed,
                    newState = WaiterState.CANCELLED,
                )

            if (WAITER_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                TERMINAL_ENVELOPE_HANDLE.setRelease(this, envelope)
                return true
            }
        }
    }

    /**
     * Marks this waiter as queued for delivery.
     *
     * This method updates only operational delivery bookkeeping.
     * It does not change the top-level waiter meaning.
     *
     * Returns `true` only when this call newly installed the queued bit.
     */
    fun tryMarkDeliveryQueued(): Boolean {
        while (true) {
            val observed = WAITER_WORD_HANDLE.getAcquire(this) as Int
            if ((observed and DELIVERY_QUEUED_BIT) != 0) {
                return false
            }

            val updated = observed or DELIVERY_QUEUED_BIT
            if (WAITER_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    /**
     * Marks delivery as completed using release publication.
     *
     * Release semantics are used because downstream convergence logic may treat the
     * delivery-done observation as an operational barrier.
     *
     * This still does not replace waiter semantic truth.
     */
    fun markDeliveryDoneRelease() {
        while (true) {
            val observed = WAITER_WORD_HANDLE.getAcquire(this) as Int
            if ((observed and DELIVERY_DONE_BIT) != 0) {
                return
            }

            val updated = observed or DELIVERY_DONE_BIT
            if (WAITER_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return
            }
        }
    }

    /**
     * Reads the winner-published opaque payload only if:
     *
     * - the waiter is already terminal, and
     * - the envelope terminal state matches the current waiter terminal state.
     *
     * This prevents stale or mismatched payload consumption.
     *
     * ## Important nuance
     *
     * Immediately after a winning terminal CAS, the payload may not yet be visible
     * for a very short window, because payload publication happens after the winning CAS.
     * Callers must therefore tolerate transient `null` and retry or defer if the payload
     * is mandatory at that exact moment.
     */
    fun readOpaquePayloadAcquire(): Any? {
        val currentState = readStateAcquire()
        if (!currentState.isTerminal) {
            return null
        }

        val raw = TERMINAL_ENVELOPE_HANDLE.getAcquire(this) ?: return null
        val envelope = raw as? TerminalEnvelope ?: return null

        return if (envelope.terminalState == currentState) {
            envelope.payload
        } else {
            null
        }
    }

    companion object {
        private const val STATE_MASK: Int = 0b0011
        private const val DELIVERY_QUEUED_BIT: Int = 1 shl 2
        private const val DELIVERY_DONE_BIT: Int = 1 shl 3

        private val LOOKUP = MethodHandles.lookup()

        private val WAITER_WORD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                WaiterCell::class.java,
                "waiterWord",
                Int::class.javaPrimitiveType,
            )

        private val TERMINAL_ENVELOPE_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                WaiterCell::class.java,
                "terminalEnvelopeRef",
                Any::class.java,
            )

        /**
         * Factory-issued construction only.
         *
         * This keeps waiter episode issuance explicit and prevents casual rebinding
         * of identity shells without generation material.
         */
        @JvmStatic
        fun issue(generation: Long): WaiterCell {
            if (generation < 0L) {
                throw PlanningProtocolIntegrityException(
                    "WaiterCell.generation must be >= 0: $generation",
                )
            }
            return WaiterCell(generation = generation)
        }

        private fun encodeInitialWord(): Int = WaiterState.ATTACHED.code

        private fun decodeState(word: Int): WaiterState = WaiterState.fromCode(word and STATE_MASK)

        private fun encodeStatePreservingFlags(
            currentWord: Int,
            newState: WaiterState,
        ): Int {
            val flags = currentWord and STATE_MASK.inv()
            return flags or newState.code
        }
    }

    /**
     * Winner-published payload envelope for one waiter terminalization episode.
     *
     * This is intentionally not a `data class`.
     *
     * Rationale:
     * - no generated `copy()`
     * - no accidental structural mutation workflow
     * - private constructor
     * - factory-issued only
     *
     * This object is deliberately tiny and sealed by construction style.
     */
    private class TerminalEnvelope private constructor(
        val terminalState: WaiterState,
        val payload: Any?,
    ) {
        companion object {
            @JvmStatic
            fun issue(
                terminalState: WaiterState,
                payload: Any?,
            ): TerminalEnvelope =
                TerminalEnvelope(
                    terminalState = terminalState,
                    payload = payload,
                )
        }
    }
}

package planning.infrastructure.cache.adapter.outgoing.dispatch.lifecycle

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level operational lifecycle vocabulary for one lane-owned delivery entry.
 *
 * Architectural role:
 * - This enum models adapter-owned delivery progress for one joined-wait continuation.
 * - It is intentionally distinct from:
 *   - SharedSlotState,
 *   - WaiterState,
 *   - BuilderHandleState,
 *   - CommitRightState,
 *   - PartitionRegionState.
 *
 * Constitutional meaning:
 * - EMPTY      : the table slot currently carries no live delivery episode and is reusable
 * - REGISTERED : continuation registration completed; terminal visibility is not yet queued
 * - SIGNALED   : terminal visibility is known; delivery token has not yet been queued
 * - QUEUED     : delivery token is enqueued for lane-owned callback execution
 * - DELIVERING : lane thread is currently invoking the continuation
 * - DONE       : callback delivery completed successfully for the current episode
 * - ABANDONED  : the current episode converged without callback delivery
 *
 * Important semantic notes:
 * - EMPTY is a substrate state, not a shared-slot semantic state.
 * - DONE and ABANDONED are terminal for one delivery episode, but the table slot may
 *   be explicitly reclaimed back to EMPTY and re-used for a later episode.
 * - Reclamation is explicit. Silent disappearance is forbidden.
 *
 * Stable code contract:
 * - Codes are stable runtime values.
 * - Codes must not drift casually because packed-state / telemetry / testing surfaces
 *   may rely on them.
 */
internal enum class DeliveryEntryState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * The physical table slot carries no live delivery episode and is reusable.
     */
    EMPTY(code = 0, isTerminal = false),

    /**
     * Continuation registration completed and the entry is waiting for a lawful signal
     * that makes delivery reachable.
     */
    REGISTERED(code = 1, isTerminal = false),

    /**
     * A lawful delivery signal is now visible, but the entry has not yet acquired a
     * ready-queue position.
     */
    SIGNALED(code = 2, isTerminal = false),

    /**
     * Delivery token has been admitted into the lane-owned ready queue.
     */
    QUEUED(code = 3, isTerminal = false),

    /**
     * Lane thread is currently invoking the continuation.
     */
    DELIVERING(code = 4, isTerminal = false),

    /**
     * Callback delivery completed successfully for the current episode and the entry is
     * awaiting explicit reclamation back to EMPTY.
     */
    DONE(code = 5, isTerminal = true),

    /**
     * The current episode converged without callback delivery and the entry is awaiting
     * explicit reclamation back to EMPTY.
     */
    ABANDONED(code = 6, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<DeliveryEntryState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<DeliveryEntryState?> =
            arrayOfNulls<DeliveryEntryState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "DeliveryEntryState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate DeliveryEntryState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        /**
         * Stable decode surface for packed-state or telemetry-facing implementations.
         */
        @JvmStatic
        fun fromCode(code: Int): DeliveryEntryState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown DeliveryEntryState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown DeliveryEntryState code: $code")
        }
    }
}

package planning.domain.runtime.lifecycle

import planning.domain.exception.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level lifecycle vocabulary for one successfully attached waiter/joiner.
 *
 * Architectural role:
 * - This enum models how the waiter terminalized.
 * - It does not model the shared cause taxonomy directly.
 *
 * Constitutional meaning:
 * - ATTACHED  : waiter successfully entered the shared-slot lifecycle
 * - RESUMED   : waiter terminalized by observing the authoritative shared terminal signal
 * - TIMED_OUT : waiter-local timeout won
 * - CANCELLED : waiter-local cancellation won
 *
 * Important semantic note:
 * - Shared success, shared failure, and shared drop all converge through RESUMED,
 *   because the waiter axis models terminalization mode, not shared cause identity.
 *
 * Attach rejection:
 * - Attach rejection does not create a waiter lifecycle object.
 * - Therefore attach rejection is not represented as a WaiterState.
 */
enum class WaiterState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * Waiter successfully entered the shared-slot lifecycle and
     * exactly one future terminal waiter outcome remains reachable.
     */
    ATTACHED(code = 0, isTerminal = false),

    /**
     * Waiter terminalized by observing the authoritative shared terminal signal.
     * The payload may represent shared success, shared failure, or shared drop.
     */
    RESUMED(code = 1, isTerminal = true),

    /**
     * Waiter-local timeout won.
     * Must never mutate shared-slot terminal state.
     */
    TIMED_OUT(code = 2, isTerminal = true),

    /**
     * Waiter-local cancellation won.
     * Must never mutate shared-slot terminal state.
     */
    CANCELLED(code = 3, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<WaiterState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<WaiterState?> =
            arrayOfNulls<WaiterState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "WaiterState.code must be >= 0: name=${state.name}, code=${state.code}"
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate WaiterState.code detected: code=${state.code}"
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): WaiterState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown WaiterState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown WaiterState code: $code")
        }
    }
}
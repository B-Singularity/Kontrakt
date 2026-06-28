package planning.infrastructure.cache.adapter.outgoing.dispatch.lifecycle

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level operational lifecycle vocabulary for one dispatch lane.
 *
 * Architectural role:
 * - This enum models lane-owned execution authority for joined-wait completion delivery.
 * - It is intentionally adapter-owned infrastructure state, not domain semantic state.
 *
 * Constitutional meaning:
 * - OPEN            : the lane may still admit fresh registration work
 * - CLOSE_REQUESTED : external authority has published a close request; fresh admission
 *                     must no longer rely on openness
 * - DRAINING        : lane is converging in-flight operational work under its own authority
 * - STOPPED         : lane-owned mutable state has terminalized and the worker thread is
 *                     no longer executing delivery mechanics
 *
 * Important semantic notes:
 * - CLOSE_REQUESTED does not imply that all live delivery entries have already converged.
 * - DRAINING is stronger than CLOSE_REQUESTED: it means the lane itself has entered
 *   internal convergence handling.
 * - STOPPED is terminal and is not lawful until lane-owned final clear has completed.
 *
 * Stable code contract:
 * - Codes are stable runtime values.
 * - Codes are not presentation values and must not drift casually.
 */
internal enum class DispatchLaneState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * Lane may still admit fresh registration work.
     */
    OPEN(code = 0, isTerminal = false),

    /**
     * External authority has published a close request.
     * Fresh admission must no longer rely on the lane being open.
     */
    CLOSE_REQUESTED(code = 1, isTerminal = false),

    /**
     * Lane thread is actively converging live operational work toward terminalization.
     */
    DRAINING(code = 2, isTerminal = false),

    /**
     * Lane-owned mutable state has terminalized and the worker thread is no longer active.
     */
    STOPPED(code = 3, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<DispatchLaneState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<DispatchLaneState?> =
            arrayOfNulls<DispatchLaneState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "DispatchLaneState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate DispatchLaneState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): DispatchLaneState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown DispatchLaneState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown DispatchLaneState code: $code")
        }
    }
}

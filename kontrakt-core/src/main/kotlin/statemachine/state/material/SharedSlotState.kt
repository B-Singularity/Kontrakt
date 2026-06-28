package statemachine.state.material

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level lifecycle vocabulary for one routed shared in-flight slot.
 *
 * Architectural role:
 * - This enum is a semantic/runtime vocabulary, not an adapter detail.
 * - It is intentionally closed. Adding or removing a state is an ADR-level change.
 * - It models only shared-slot truth. It does not model waiter-local timeout/cancel.
 *
 * Constitutional meaning:
 * - PENDING  : the slot exists and has not terminalized
 * - SUCCESS  : authoritative publication has already linearized and is visible
 * - FAILED   : the slot terminalized as a shared failure
 * - DROPPED  : the slot terminalized due to region close / bulk drop governance
 *
 * Stable code contract:
 * - Codes are stable runtime values because hot-path encoding / decoding may depend on them.
 * - Codes are not presentation values and must not drift casually.
 *
 * Important:
 * - The exact bit packing is implementation-defined.
 * - The semantic meaning and stable code identity are not.
 */
enum class SharedSlotState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * Shared slot exists, has not terminalized, and may still admit lawful lifecycle activity.
     */
    PENDING(code = 0, isTerminal = false),

    /**
     * Authoritative publication has already linearized.
     * Waiters may now lawfully converge by observing the shared terminal signal.
     */
    SUCCESS(code = 1, isTerminal = true),

    /**
     * Shared slot terminalized unsuccessfully as a shared failure.
     * This is not waiter-local timeout and not waiter-local cancellation.
     */
    FAILED(code = 2, isTerminal = true),

    /**
     * Shared slot terminalized by partition / region close or bulk-drop governance.
     */
    DROPPED(code = 3, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<SharedSlotState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<SharedSlotState?> =
            arrayOfNulls<SharedSlotState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "SharedSlotState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate SharedSlotState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        /**
         * Stable decode surface for packed-state implementations.
         */
        @JvmStatic
        fun fromCode(code: Int): SharedSlotState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown SharedSlotState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown SharedSlotState code: $code")
        }
    }
}

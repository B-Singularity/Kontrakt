package statemachine.state.material.condition

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed operational lifecycle vocabulary for one runtime-boundary joined-wait bridge episode.
 *
 * This state machine is intentionally separate from:
 * - PlanningRunState
 * - SharedSlotState
 * - WaiterState
 * - DeliveryEntryState
 *
 * It models only the bridge-local progression from:
 * - callback registration
 * - to readiness publication
 * - to fresh-session consume permission
 * - to actual ready-result consumption
 *
 * Meanings:
 * - INITIAL:
 *   no callback registration has completed yet
 * - CALLBACK_REGISTERED:
 *   one-shot callback registration completed, readiness not yet published
 * - READY_PUBLISHED:
 *   bridge has published READY_TO_RESTART eligibility exactly once
 * - CONSUME_PERMITTED:
 *   lawful restart admission granted fresh-session consume authority
 * - CONSUMING:
 *   the bridge is currently consuming the ready result through the restart session
 * - CONSUMED:
 *   ready result consumption finished; terminal
 * - CANCELLED:
 *   the bridge episode was cancelled before readiness publication; terminal
 */
enum class PlanningRunSuspensionBridgeState(
    val code: Int,
    val isTerminal: Boolean,
) {
    INITIAL(code = 0, isTerminal = false),
    CALLBACK_REGISTERED(code = 1, isTerminal = false),
    READY_PUBLISHED(code = 2, isTerminal = false),
    CONSUME_PERMITTED(code = 3, isTerminal = false),
    CONSUMING(code = 4, isTerminal = false),
    CONSUMED(code = 5, isTerminal = true),
    CANCELLED(code = 6, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<PlanningRunSuspensionBridgeState> = entries
        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<PlanningRunSuspensionBridgeState?> =
            arrayOfNulls<PlanningRunSuspensionBridgeState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "PlanningRunSuspensionBridgeState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate PlanningRunSuspensionBridgeState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): PlanningRunSuspensionBridgeState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException(
                    "Unknown PlanningRunSuspensionBridgeState code: $code",
                )
            }

            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException(
                    "Unknown PlanningRunSuspensionBridgeState code: $code",
                )
        }
    }
}
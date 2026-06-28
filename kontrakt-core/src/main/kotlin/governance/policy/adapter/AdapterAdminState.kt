package governance.policy.adapter

import planning.domain.exception.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed administrative lifecycle vocabulary for one in-memory Tier-2 adapter instance.
 *
 * Scope:
 * - adapter-level partition-drop serialization
 * - adapter-level whole-close serialization
 *
 * This state machine is intentionally adapter-local operational state.
 *
 * It is NOT:
 * - partition-region lifecycle
 * - shared-slot lifecycle
 * - waiter lifecycle
 * - builder lifecycle
 * - dispatch-lane lifecycle
 * - planning-run lifecycle
 *
 * Meanings:
 * - OPEN:
 *   normal steady state; resolveOrIntern and administrative operations are admissible
 * - DROP_IN_PROGRESS:
 *   one administrative partition-drop operation currently owns exclusive adapter admin authority
 * - CLOSING:
 *   whole-adapter close has been published and terminalization is in progress
 * - CLOSED:
 *   whole-adapter close has completed or terminalized fail-closed; terminal state
 */
internal enum class AdapterAdminState(
    val code: Int,
    val isTerminal: Boolean,
) {
    OPEN(code = 0, isTerminal = false),
    DROP_IN_PROGRESS(code = 1, isTerminal = false),
    CLOSING(code = 2, isTerminal = false),
    CLOSED(code = 3, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<AdapterAdminState> = entries
        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<AdapterAdminState?> =
            arrayOfNulls<AdapterAdminState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "AdapterAdminState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate AdapterAdminState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): AdapterAdminState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException(
                    "Unknown AdapterAdminState code: $code",
                )
            }

            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException(
                    "Unknown AdapterAdminState code: $code",
                )
        }
    }
}

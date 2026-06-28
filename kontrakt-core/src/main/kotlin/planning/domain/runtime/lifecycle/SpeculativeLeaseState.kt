package planning.domain.runtime.lifecycle

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level lifecycle vocabulary for slot-owned speculative leases.
 *
 * Important semantic note:
 * - Lease absence is represented by absence of a lease object/record,
 *   not by an extra top-level state.
 *
 * Constitutional meaning:
 * - ISSUED   : slot-owned speculative lease is live
 * - RELEASED : lease converged and must not be re-used for the same lease episode
 *
 * This axis is orthogonal to:
 * - shared-slot lifecycle,
 * - waiter lifecycle,
 * - builder-handle lifecycle,
 * - commit-right lifecycle.
 */
enum class SpeculativeLeaseState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * Lease has been issued and is still live.
     */
    ISSUED(code = 0, isTerminal = false),

    /**
     * Lease has been released and must not be used again for the same lease episode.
     */
    RELEASED(code = 1, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<SpeculativeLeaseState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<SpeculativeLeaseState?> =
            arrayOfNulls<SpeculativeLeaseState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "SpeculativeLeaseState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate SpeculativeLeaseState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): SpeculativeLeaseState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown SpeculativeLeaseState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown SpeculativeLeaseState code: $code")
        }
    }
}

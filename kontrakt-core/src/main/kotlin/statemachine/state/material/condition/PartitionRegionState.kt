package statemachine.state.material.condition

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level lifecycle vocabulary for region / partition authority.
 *
 * Constitutional meaning:
 * - OPEN            : region may still admit lawful lifecycle activity
 * - CLOSE_PUBLISHED : close gate is authoritative; no new admission may rely on openness
 * - RECLAIMED       : terminalization convergence, pending terminal delivery reachability,
 *                     and grace barrier requirements have all completed
 *
 * Important:
 * - RECLAIMED is stronger than "cleanup started" or "slot map removed".
 * - RECLAIMED is not lawful until grace completion has been satisfied.
 */
enum class PartitionRegionState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * Region may still admit lawful shared-slot and waiter activity.
     */
    OPEN(code = 0, isTerminal = false),

    /**
     * Close gate is authoritative.
     * No new lifecycle admission may rely on the region being open.
     */
    CLOSE_PUBLISHED(code = 1, isTerminal = false),

    /**
     * Terminalization convergence is complete, pending terminal deliveries are no longer
     * reachable as pending work, and the required grace barrier has completed.
     */
    RECLAIMED(code = 2, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<PartitionRegionState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<PartitionRegionState?> =
            arrayOfNulls<PartitionRegionState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "PartitionRegionState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate PartitionRegionState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): PartitionRegionState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown PartitionRegionState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown PartitionRegionState code: $code")
        }
    }
}

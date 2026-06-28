package statemachine.state.material

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level lifecycle vocabulary for authoritative publication arbitration.
 *
 * Architectural role:
 * - Build permission and publish permission are distinct.
 * - Multiple builders may execute, but exactly one commit-right winner may enter
 *   authoritative publication for one publication episode.
 *
 * Constitutional meaning:
 * - UNCLAIMED : publication authority has not yet been won
 * - CLAIMED   : exactly one contender has won entry into authoritative publication
 * - RELEASED  : publication arbitration has terminalized for the current episode
 *
 * Important:
 * - This axis is orthogonal to BuilderHandleState.
 * - Commit-right does not model builder progress.
 * - BuilderHandleState does not model authoritative publication admission.
 */
enum class CommitRightState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * No contender has yet won publication authority.
     */
    UNCLAIMED(code = 0, isTerminal = false),

    /**
     * Exactly one contender has won the right to enter authoritative publication.
     */
    CLAIMED(code = 1, isTerminal = false),

    /**
     * Publication arbitration has converged and must not admit another winner
     * for the same publication episode.
     */
    RELEASED(code = 2, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<CommitRightState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<CommitRightState?> =
            arrayOfNulls<CommitRightState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "CommitRightState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate CommitRightState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): CommitRightState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown CommitRightState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown CommitRightState code: $code")
        }
    }
}

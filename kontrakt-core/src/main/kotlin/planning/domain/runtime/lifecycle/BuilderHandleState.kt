package planning.domain.runtime.lifecycle

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import kotlin.enums.EnumEntries

/**
 * Closed top-level lifecycle vocabulary for builder-handle authority on the miss path.
 *
 * Architectural role:
 * - This enum models the lifecycle of builder-owned authority, not shared-slot truth.
 * - It is intentionally distinct from commit-right arbitration.
 *
 * Constitutional meaning:
 * - OPEN      : builder authority is outstanding under a lawful supervisory regime
 * - COMMITTED : builder authority converged through successful commit
 * - ABORTED   : builder authority converged through abort or supervisory force-abort
 *
 * Important semantic note:
 * - OPEN is supervision-bound rather than indefinitely passive.
 * - The runtime must guarantee eventual convergence to COMMITTED or ABORTED.
 */
enum class BuilderHandleState(
    val code: Int,
    val isTerminal: Boolean,
) {
    /**
     * Builder authority is outstanding and has not yet converged.
     * This is a supervision-bound state, not indefinite passive waiting.
     */
    OPEN(code = 0, isTerminal = false),

    /**
     * Builder authority converged through successful commit.
     */
    COMMITTED(code = 1, isTerminal = true),

    /**
     * Builder authority converged through abort, including supervisory force-abort.
     */
    ABORTED(code = 2, isTerminal = true),
    ;

    companion object {
        private val ALL: EnumEntries<BuilderHandleState> = entries

        private val MAX_CODE: Int = ALL.maxOf { it.code }

        private val BY_CODE: Array<BuilderHandleState?> =
            arrayOfNulls<BuilderHandleState>(MAX_CODE + 1).also { table ->
                for (state in ALL) {
                    if (state.code < 0) {
                        throw PlanningProtocolIntegrityException(
                            "BuilderHandleState.code must be >= 0: name=${state.name}, code=${state.code}",
                        )
                    }
                    if (table[state.code] != null) {
                        throw PlanningProtocolIntegrityException(
                            "Duplicate BuilderHandleState.code detected: code=${state.code}",
                        )
                    }
                    table[state.code] = state
                }
            }

        @JvmStatic
        fun fromCode(code: Int): BuilderHandleState {
            if (code < 0 || code >= BY_CODE.size) {
                throw PlanningProtocolIntegrityException("Unknown BuilderHandleState code: $code")
            }
            return BY_CODE[code]
                ?: throw PlanningProtocolIntegrityException("Unknown BuilderHandleState code: $code")
        }
    }
}

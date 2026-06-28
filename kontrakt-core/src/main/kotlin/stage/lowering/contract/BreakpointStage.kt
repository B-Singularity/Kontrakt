package stage.lowering.contract

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Tri-stage breakpoint priority from ADR-0030.
 *
 * Ordering:
 * - NONE            = not breakable
 * - SUBSTITUTABLE   = stage 2
 * - DEFERRED        = stage 1
 *
 * Larger tag value means stronger priority in primitive hot-path comparisons.
 */
enum class BreakpointStage(
    val tag: Byte,
) {
    NONE(0),
    SUBSTITUTABLE(1),
    DEFERRED(2),
    ;

    companion object {
        @JvmStatic
        fun fromTag(tag: Byte): BreakpointStage =
            when (tag) {
                0.toByte() -> NONE
                1.toByte() -> SUBSTITUTABLE
                2.toByte() -> DEFERRED
                else -> throw PlanningProtocolIntegrityException(
                    "Unknown BreakpointStage tag: $tag",
                )
            }
    }
}

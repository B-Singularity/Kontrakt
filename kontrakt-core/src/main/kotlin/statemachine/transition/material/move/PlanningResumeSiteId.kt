package statemachine.transition.material.move

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

@JvmInline
value class PlanningResumeSiteId private constructor(
    val value: String,
) {
    override fun toString(): String = value

    companion object {
        @JvmStatic
        fun issue(value: String): PlanningResumeSiteId {
            if (value.isBlank()) {
                throw PlanningProtocolIntegrityException(
                    "PlanningResumeSiteId.value must not be blank.",
                )
            }

            return PlanningResumeSiteId(value)
        }

        val L2_ALLOCATE_INTERN: PlanningResumeSiteId =
            issue("L2_ALLOCATE_INTERN")

        val L2_CYCLE_BREAK_INTERN: PlanningResumeSiteId =
            issue("L2_CYCLE_BREAK_INTERN")
    }
}

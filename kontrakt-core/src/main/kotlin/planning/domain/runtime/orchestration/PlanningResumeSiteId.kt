package planning.domain.runtime.orchestration

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Stable identifier for one planning-run restart site family.
 *
 * The runtime may start with only a small number of restart sites, but this type
 * remains intentionally open-ended so that later waves may add new sites without
 * redefining the orchestration law.
 */
@JvmInline
value class PlanningResumeSiteId(
    val value: String,
) {
    init {
        if (value.isBlank()) {
            throw PlanningProtocolIntegrityException(
                "PlanningResumeSiteId.value must not be blank."
            )
        }
    }

    override fun toString(): String = value

    companion object {
        val L2_ALLOCATE_INTERN: PlanningResumeSiteId =
            PlanningResumeSiteId("L2_ALLOCATE_INTERN")
        
        val L2_CYCLE_BREAK_INTERN: PlanningResumeSiteId =
            PlanningResumeSiteId("L2_CYCLE_BREAK_INTERN")
    }
}
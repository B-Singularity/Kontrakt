package stage.lowering.material.projection

/**
 * Core-owned property admission decision.
 *
 * Admitted properties become projected active members.
 * Demoted properties remain diagnostic evidence only and must not enter traversal.
 *
 * This type deliberately uses regular object/class declarations, not data
 * object/data class.
 */
sealed interface PropertyAdmissionDecision {
    val isAdmitted: Boolean

    object Admitted : PropertyAdmissionDecision {
        override val isAdmitted: Boolean = true
    }

    class Demoted private constructor(
        val reason: PropertyDemotionReason,
    ) : PropertyAdmissionDecision {
        override val isAdmitted: Boolean = false

        companion object {
            @JvmStatic
            fun issue(reason: PropertyDemotionReason): Demoted = Demoted(reason)
        }
    }
}

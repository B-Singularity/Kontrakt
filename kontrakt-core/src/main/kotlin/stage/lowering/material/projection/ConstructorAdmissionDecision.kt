package stage.lowering.material.projection

/**
 * Core-owned constructor admission decision.
 *
 * This is a closed decision surface used before deterministic constructor
 * selection. It does not select a constructor; it only says whether one raw
 * constructor candidate may participate in selection.
 *
 * This type deliberately uses regular object/class declarations, not data
 * object/data class.
 */
sealed interface ConstructorAdmissionDecision {
    val isAdmitted: Boolean

    object Admitted : ConstructorAdmissionDecision {
        override val isAdmitted: Boolean = true
    }

    class Rejected private constructor(
        val reason: ConstructorRejectionReason,
    ) : ConstructorAdmissionDecision {
        override val isAdmitted: Boolean = false

        companion object {
            @JvmStatic
            fun issue(reason: ConstructorRejectionReason): Rejected = Rejected(reason)
        }
    }
}

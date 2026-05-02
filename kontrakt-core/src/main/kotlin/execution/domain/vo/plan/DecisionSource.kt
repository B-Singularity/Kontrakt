package execution.domain.vo.plan

/**
 * Tracks the origin of a decision in the ExecutablePlan.
 * Used for auditing (Why was this generator selected?).
 */
interface DecisionSource {
    val description: String

    /**
     * Decision made by the System/Framework (e.g., Cycle Breaking, Default Policies).
     */
    data class Framework(
        val reason: String,
    ) : DecisionSource {
        override val description: String = "Framework: $reason"
    }

    /**
     * Decision explicitly defined by the User (e.g., @Generator annotation).
     */
    data class User(
        val source: String = "Explicit Configuration",
    ) : DecisionSource {
        override val description: String = "User Defined: $source"

        companion object {
            // [Fix] Allows 'DecisionSource.User' usage if Linker uses it as a pseudo-constant
            val DEFAULT = User()
        }
    }

    /**
     * Decision made by a selected Strategy (e.g., Random Selection).
     */
    data class Strategy(
        val strategyName: String = "Default Strategy",
    ) : DecisionSource {
        override val description: String = "Strategy: $strategyName"

        companion object {
            val DEFAULT = Strategy()
        }
    }
}

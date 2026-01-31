package execution.domain.vo.plan

/**
 * Tracks the origin of a decision in the ExecutablePlan.
 * Crucial for auditing why a specific value or generator was selected (ADR-016).
 */
interface DecisionSource {
    val description: String

    data class System(override val description: String) : DecisionSource {
        companion object {
            val DEFAULT = System("Default Policy")
            val CYCLE_CUT = System("Cycle Cut Strategy")
        }
    }

    data class User(val source: String) : DecisionSource {
        override val description: String = "User Defined: $source"
    }

    data class Strategy(val strategyName: String) : DecisionSource {
        override val description: String = "Strategy Selected: $strategyName"
    }
}
package realization.identity.interning

import stage.lowering.material.candidate.CanonicalPlanNode

/**
 * Domain-level orchestration result returned by PlanInterner.
 *
 * This surface is intentionally minimal:
 * - Completed
 * - SuspendedOnJoin
 *
 * The runtime boundary's real concern is:
 * - did the work complete now?
 * - or must the request suspend and later restart?
 */
sealed interface InternerStepResult {
    /**
     * The interning operation completed immediately and produced the final
     * canonical winner node.
     */
    class Completed internal constructor(
        val node: CanonicalPlanNode,
    ) : InternerStepResult {
        override fun toString(): String = "InternerStepResult.Completed(node=$node)"
    }

    /**
     * The interning operation must suspend on a non-blocking joined wait.
     *
     * The runtime boundary must not block a worker.
     * It must hand this pending join to orchestration / continuation machinery.
     */
    class SuspendedOnJoin internal constructor(
        val pendingJoin: PendingJoin,
    ) : InternerStepResult {
        override fun toString(): String = "InternerStepResult.SuspendedOnJoin"
    }
}

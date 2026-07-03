package realization.identity.interning

import governance.budget.contract.CostCenter
import migration.quarantine.JoinContinuation
import migration.quarantine.JoinHandle
import migration.quarantine.JoinRegistrationDecision
import migration.quarantine.JoinResumeStep
import realization.planning.assembly.CanonicalPayloadSealer
import realization.planning.session.PlannerSession
import statemachine.transition.diagnostics.L2FaultKind

/**
 * Suspended join episode lifted into domain/service space.
 *
 * This object retains only:
 * - JoinHandle
 * - ReplaySafeCanonicalFallbackPlan
 *
 * It deliberately retains:
 * - no lambda closure
 * - no PlannerSession reference
 * - no worker-local mutable planning state
 */
class PendingJoin private constructor(
    private val handle: JoinHandle,
    private val fallbackPlan: ReplaySafeCanonicalFallbackPlan,
) {
    /**
     * Registers a continuation for non-blocking completion delivery.
     */
    fun registerContinuation(continuation: JoinContinuation): JoinRegistrationDecision =
        handle.registerContinuation(continuation)

    /**
     * Exposes the monotonic waiter deadline for orchestration / diagnostics.
     */
    fun deadlineNanos(): Long = handle.deadlineNanos()

    /**
     * Requests waiter-local cancellation.
     */
    fun cancel(reason: Throwable): Boolean = handle.cancel(reason)

    /**
     * Resumes through a fresh planner session.
     *
     * The supplied session is the session that pays resumed work accounting.
     * No stale pre-suspension session is reused here.
     */
    fun resume(
        session: PlannerSession,
        sealer: CanonicalPayloadSealer,
    ): InternerStepResult.Completed =
        when (val resumed = handle.consumeReadyResult(session)) {
            is JoinResumeStep.Hit -> {
                session.step(CostCenter.L2_HIT)
                InternerStepResult.Completed(resumed.node)
            }

            is JoinResumeStep.Fault -> {
                val node =
                    when (resumed.kind) {
                        L2FaultKind.TRANSIENT -> {
                            session.step(CostCenter.L2_FAULT_TRANSIENT)
                            fallbackPlan.build(sealer)
                        }

                        L2FaultKind.CIRCUIT_OPEN -> {
                            session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                            session.markL2Bypassed()
                            fallbackPlan.build(sealer)
                        }
                    }
                InternerStepResult.Completed(node)
            }
        }

    companion object {
        @JvmStatic
        fun issue(
            handle: JoinHandle,
            fallbackPlan: ReplaySafeCanonicalFallbackPlan,
        ): PendingJoin =
            PendingJoin(
                handle = handle,
                fallbackPlan = fallbackPlan,
            )
    }
}

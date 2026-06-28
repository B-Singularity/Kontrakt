package planning.domain.interner

import governance.budget.CostCenter
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.JoinContinuation
import planning.domain.port.outgoing.JoinHandle
import planning.domain.port.outgoing.JoinRegistrationDecision
import planning.domain.port.outgoing.JoinResumeStep
import planning.domain.service.assembly.CanonicalPayloadSealer
import planning.domain.session.PlannerSession

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

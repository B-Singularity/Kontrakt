package planning.infrastructure.runtime.orchestration

import planning.domain.port.outgoing.JoinHandle
import planning.domain.port.outgoing.JoinRegistrationDecision
import planning.domain.port.outgoing.JoinResumeSignal
import planning.domain.port.outgoing.JoinResumeStep
import planning.domain.session.PlannerSession
import planning.infrastructure.runtime.orchestration.lifecycle.PlanningRunSuspensionBridgeState
import java.util.concurrent.atomic.AtomicInteger

/**
 * Concrete runtime-boundary bridge from adapter-level JoinHandle to
 * PlanningRunSuspensionHandle.
 *
 * Architectural role:
 * - hides raw adapter join mechanics from PlanningRunContext
 * - translates adapter-owned readiness into planning-run restart readiness
 * - preserves the fresh-session consume rule on resumed work
 *
 * Concurrency stance:
 * - explicit bridge-local state machine
 * - atomic state-code transitions
 * - no monitor-based authority surface
 *
 * Design notes:
 * - callback registration is one-shot
 * - readiness publication is one-shot
 * - fresh-session consume permission is separate from readiness publication
 * - actual ready-result consumption is exactly-once
 *
 * Therefore four things are intentionally separated:
 * - callback registration
 * - readiness publication
 * - fresh-session consume permit
 * - actual consume execution
 */
class JoinedWaitPlanningRunSuspensionHandle private constructor(
    private val joinHandle: JoinHandle,
) : PlanningRunSuspensionHandle {
    private val stateCode = AtomicInteger(PlanningRunSuspensionBridgeState.INITIAL.code)

    override fun registerReadyToRestartCallback(onReadyToRestart: () -> Unit): PlanningRunSuspensionRegistrationDecision {
        transitionInitialToCallbackRegistered()

        return when (
            joinHandle.registerContinuation { signal ->
                when (signal) {
                    JoinResumeSignal.ReadyForRestart -> {
                        if (tryPublishReadiness()) {
                            onReadyToRestart()
                        }
                    }
                }
            }
        ) {
            JoinRegistrationDecision.Registered ->
                PlanningRunSuspensionRegistrationDecision.REGISTERED

            JoinRegistrationDecision.AlreadyReady -> {
                if (tryPublishReadiness()) {
                    onReadyToRestart()
                }
                PlanningRunSuspensionRegistrationDecision.ALREADY_READY
            }
        }
    }

    override fun grantFreshSessionConsumePermit() {
        while (true) {
            val from = stateAcquire()

            PlanningRunSuspensionBridgeLifecycleLaw.requireReadyPublishedForConsumePermit(from)

            val to = PlanningRunSuspensionBridgeState.CONSUME_PERMITTED
            PlanningRunSuspensionBridgeLifecycleLaw.requireTransition(from, to)

            if (stateCode.compareAndSet(from.code, to.code)) {
                return
            }
        }
    }

    override fun consumeReadyResult(session: PlannerSession): JoinResumeStep {
        while (true) {
            val from = stateAcquire()

            PlanningRunSuspensionBridgeLifecycleLaw.requireConsumePermittedForConsumeStart(from)

            val to = PlanningRunSuspensionBridgeState.CONSUMING
            PlanningRunSuspensionBridgeLifecycleLaw.requireTransition(from, to)

            if (stateCode.compareAndSet(from.code, to.code)) {
                break
            }
        }

        return try {
            joinHandle.consumeReadyResult(session).also {
                transitionConsumingToConsumed()
            }
        } catch (t: Throwable) {
            /*
             * Fail-closed exactly-once stance:
             * once consumption has started, the bridge does not permit re-entry even if
             * the underlying adapter/session path throws. We therefore terminalize the
             * bridge episode to CONSUMED and rethrow.
             */
            transitionConsumingToConsumed()
            throw t
        }
    }

    override fun cancel(cause: Throwable): Boolean {
        val cancelled = joinHandle.cancel(cause)

        if (!cancelled) {
            return false
        }

        while (true) {
            val from = stateAcquire()

            if (!PlanningRunSuspensionBridgeLifecycleLaw.canTransitionToCancelled(from)) {
                return true
            }

            val to = PlanningRunSuspensionBridgeState.CANCELLED
            PlanningRunSuspensionBridgeLifecycleLaw.requireTransition(from, to)

            if (stateCode.compareAndSet(from.code, to.code)) {
                return true
            }
        }
    }

    override fun deadlineNanos(): Long = joinHandle.deadlineNanos()

    fun stateAcquire(): PlanningRunSuspensionBridgeState = PlanningRunSuspensionBridgeState.fromCode(stateCode.get())

    private fun transitionInitialToCallbackRegistered() {
        while (true) {
            val from = stateAcquire()

            PlanningRunSuspensionBridgeLifecycleLaw.requireInitialForCallbackRegistration(from)

            val to = PlanningRunSuspensionBridgeState.CALLBACK_REGISTERED
            PlanningRunSuspensionBridgeLifecycleLaw.requireTransition(from, to)

            if (stateCode.compareAndSet(from.code, to.code)) {
                return
            }
        }
    }

    /**
     * Best-effort one-shot readiness publication.
     *
     * Returns true exactly once for one bridge episode when readiness becomes newly
     * published. Returns false for duplicates or for states where readiness publication
     * is no longer lawful.
     */
    private fun tryPublishReadiness(): Boolean {
        while (true) {
            val from = stateAcquire()

            if (!PlanningRunSuspensionBridgeLifecycleLaw.canPublishReadiness(from)) {
                return false
            }

            val to = PlanningRunSuspensionBridgeState.READY_PUBLISHED
            PlanningRunSuspensionBridgeLifecycleLaw.requireTransition(from, to)

            if (stateCode.compareAndSet(from.code, to.code)) {
                return true
            }
        }
    }

    private fun transitionConsumingToConsumed() {
        while (true) {
            val from = stateAcquire()

            if (from == PlanningRunSuspensionBridgeState.CONSUMED) {
                return
            }

            val to = PlanningRunSuspensionBridgeState.CONSUMED
            PlanningRunSuspensionBridgeLifecycleLaw.requireTransition(from, to)

            if (stateCode.compareAndSet(from.code, to.code)) {
                return
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(joinHandle: JoinHandle): JoinedWaitPlanningRunSuspensionHandle =
            JoinedWaitPlanningRunSuspensionHandle(
                joinHandle = joinHandle,
            )
    }
}

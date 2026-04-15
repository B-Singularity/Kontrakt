package planning.infrastructure.runtime.orchestration

import planning.domain.port.outgoing.JoinResumeStep
import planning.domain.session.PlannerSession

/**
 * Runtime-boundary abstraction for one joined-wait suspension token.
 *
 * The orchestration layer must not depend directly on one concrete L2 pending-join
 * implementation detail. This handle abstracts that dependency boundary.
 *
 * Responsibilities:
 * - register a one-shot readiness callback for the planning-run axis
 * - receive the fresh-session consume permit only from lawful restart admission
 * - expose fresh-session result consumption
 * - support best-effort cancellation
 * - expose the monotonic deadline carried by the suspended join episode
 *
 * Important:
 * - this handle is operational runtime state
 * - it is intentionally distinct from the immutable PlanningResumePoint
 * - fresh-session consume authority is not implied by readiness publication alone
 */
interface PlanningRunSuspensionHandle {

    /**
     * Registers the runtime-boundary callback that should fire when the suspended
     * join becomes ready for fresh-session restart.
     *
     * The callback MUST be treated as one-shot.
     * Calling this method more than once is a protocol violation.
     */
    fun registerReadyToRestartCallback(
        onReadyToRestart: () -> Unit,
    ): PlanningRunSuspensionRegistrationDecision

    /**
     * Grants the one-shot permission to consume the ready result through the
     * fresh restart session.
     *
     * This permission must be granted only by lawful restart admission.
     * Readiness publication alone is insufficient.
     */
    fun grantFreshSessionConsumePermit()

    /**
     * Consumes the already-ready result through a fresh or freshly-reset PlannerSession.
     *
     * This closes the bridge from:
     * - adapter-owned joined completion delivery
     * to
     * - planning-run restart execution.
     *
     * This method must require:
     * - already-published readiness
     * - previously granted fresh-session consume permit
     * - exactly-once consumption
     */
    fun consumeReadyResult(
        session: PlannerSession,
    ): JoinResumeStep

    /**
     * Best-effort cancellation of the suspended join episode.
     *
     * Returning false means the handle was already terminal or otherwise could
     * not be cancelled by this call.
     */
    fun cancel(cause: Throwable): Boolean

    /**
     * Monotonic deadline of the suspended join episode.
     */
    fun deadlineNanos(): Long
}
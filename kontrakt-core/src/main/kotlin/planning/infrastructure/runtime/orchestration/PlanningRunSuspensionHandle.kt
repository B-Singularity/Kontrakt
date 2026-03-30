package planning.infrastructure.runtime.orchestration

/**
 * Runtime-boundary abstraction for one joined-wait suspension token.
 *
 * The orchestration layer must not depend directly on one concrete L2 pending-join
 * implementation detail. This handle abstracts that dependency boundary.
 */
interface PlanningRunSuspensionHandle {

    /**
     * Best-effort cancellation of the suspended join episode.
     *
     * Returning false means the handle was already terminal or otherwise could
     * not be cancelled by this call.
     */
    fun cancel(cause: Throwable): Boolean
}
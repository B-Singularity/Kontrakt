package realization.runtime.orchestration

import migration.quarantine.JoinHandle
import statemachine.transition.material.move.PlanningResumePoint

/**
 * Runtime-boundary bridge from adapter-level joined wait to planning-run orchestration.
 *
 * Responsibilities:
 * - wrap JoinHandle with a PlanningRunSuspensionHandle
 * - materialize a PlanningRunSuspension records
 * - transition the run into SUSPENDED_ON_JOIN
 * - wire adapter-owned readiness into PlanningRunContext.tryMarkReadyToRestart()
 *
 * Important:
 * - this bridge owns only runtime-boundary coordination
 * - it does not own PlannerSession cleanup
 * - ordinary PlannerSession finally-cleanup remains the caller's responsibility
 */
object PlanningRunJoinBridge {
    @JvmStatic
    fun suspendOnJoin(
        context: PlanningRunContext,
        activeLease: PlanningRunWorkerSessionLease,
        resumePoint: PlanningResumePoint,
        joinHandle: JoinHandle,
    ): PlanningRunSuspensionRegistrationDecision {
        val suspensionHandle = JoinedWaitPlanningRunSuspensionHandle.issue(joinHandle)
        val suspension =
            PlanningRunSuspension.issue(
                resumePoint = resumePoint,
                suspensionHandle = suspensionHandle,
            )

        /*
         * Suspend the run first.
         *
         * Rationale:
         * - if registration returns ALREADY_READY, the callback must observe the run
         *   already in SUSPENDED_ON_JOIN so that READY_TO_RESTART publication is lawful
         * - if readiness arrives asynchronously later, the same callback path remains valid
         */
        context.suspendOnJoin(
            activeLease = activeLease,
            suspension = suspension,
        )

        return suspensionHandle.registerReadyToRestartCallback {
            context.tryMarkReadyToRestart()
        }
    }
}

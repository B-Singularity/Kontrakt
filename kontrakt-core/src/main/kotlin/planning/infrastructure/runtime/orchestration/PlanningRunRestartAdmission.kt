package planning.infrastructure.runtime.orchestration

import planning.domain.runtime.orchestration.PlanningResumePoint
import planning.domain.runtime.orchestration.PlanningRunEpoch
import planning.domain.runtime.orchestration.PlanningRunRemainingBudget
import planning.infrastructure.runtime.policy.RuntimePolicyEpoch

/**
 * Runtime-boundary admission result for a lawful fresh-session restart.
 *
 * The runtime uses this object to:
 * - acquire/build a fresh worker-local PlannerSession
 * - pin the already-selected RuntimePolicyEpoch
 * - continue the same PlanningRunEpoch
 * - carry forward the same remaining run-scoped execution budget
 * - consume the already-ready suspended-join result through the carried
 *   runtime-boundary suspension handle
 *
 * Important:
 * - resumePoint is immutable semantic restart data
 * - suspensionHandle is operational runtime state
 * - these two axes are intentionally separate
 *
 * Permit authority:
 * - fresh-session consume permission is granted here, inside issue(...)
 * - this keeps restart-admission issuance and consume-authority issuance in one place
 */
class PlanningRunRestartAdmission private constructor(
    val workerSessionLease: PlanningRunWorkerSessionLease,
    val runEpoch: PlanningRunEpoch,
    val pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
    val resumePoint: PlanningResumePoint,
    val remainingBudget: PlanningRunRemainingBudget,
    val suspensionHandle: PlanningRunSuspensionHandle,
) {
    companion object {
        @JvmStatic
        fun issue(
            workerSessionLease: PlanningRunWorkerSessionLease,
            runEpoch: PlanningRunEpoch,
            pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
            resumePoint: PlanningResumePoint,
            remainingBudget: PlanningRunRemainingBudget,
            suspensionHandle: PlanningRunSuspensionHandle,
        ): PlanningRunRestartAdmission {
            /*
             * Grant consume authority as part of lawful restart admission issuance.
             *
             * This keeps the authority source singular:
             * - admitRestart() decides restart is lawful
             * - PlanningRunRestartAdmission.issue(...) materializes the admission packet
             * - the admission packet is the moment consume authority becomes valid
             */
            suspensionHandle.grantFreshSessionConsumePermit()

            return PlanningRunRestartAdmission(
                workerSessionLease = workerSessionLease,
                runEpoch = runEpoch,
                pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
                resumePoint = resumePoint,
                remainingBudget = remainingBudget,
                suspensionHandle = suspensionHandle,
            )
        }
    }
}
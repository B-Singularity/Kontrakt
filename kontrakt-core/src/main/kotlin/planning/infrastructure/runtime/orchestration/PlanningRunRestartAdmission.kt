package planning.infrastructure.runtime.orchestration

import planning.domain.runtime.orchestration.PlanningResumePoint
import planning.domain.runtime.orchestration.PlanningRunEpoch
import planning.infrastructure.runtime.policy.RuntimePolicyEpoch

/**
 * Runtime-boundary admission result for a lawful fresh-session restart.
 *
 * The runtime uses this object to:
 * - acquire/build a fresh worker-local PlannerSession,
 * - pin the already-selected RuntimePolicyEpoch,
 * - continue the same PlanningRunEpoch,
 * - debit/request remaining physical budget under the same run.
 */
class PlanningRunRestartAdmission private constructor(
    val workerSessionLease: PlanningRunWorkerSessionLease,
    val runEpoch: PlanningRunEpoch,
    val pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
    val resumePoint: PlanningResumePoint,
    val remainingPhysicalBudget: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            workerSessionLease: PlanningRunWorkerSessionLease,
            runEpoch: PlanningRunEpoch,
            pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
            resumePoint: PlanningResumePoint,
            remainingPhysicalBudget: Int,
        ): PlanningRunRestartAdmission {
            return PlanningRunRestartAdmission(
                workerSessionLease = workerSessionLease,
                runEpoch = runEpoch,
                pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
                resumePoint = resumePoint,
                remainingPhysicalBudget = remainingPhysicalBudget,
            )
        }
    }
}
package realization.runtime.orchestration

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import statemachine.transition.material.PlanningResumePoint

/**
 * Immutable-orchestration records for one joined-wait suspension.
 *
 * It binds:
 * - the immutable resume-point descriptor
 * - the runtime-boundary suspension handle
 *
 * The handle is operational.
 * The resume point is semantic restart data.
 */
class PlanningRunSuspension private constructor(
    val resumePoint: PlanningResumePoint,
    val suspensionHandle: PlanningRunSuspensionHandle,
) {
    companion object {
        @JvmStatic
        fun issue(
            resumePoint: PlanningResumePoint,
            suspensionHandle: PlanningRunSuspensionHandle,
        ): PlanningRunSuspension {
            if (resumePoint.schemaVersion < 1) {
                throw PlanningProtocolIntegrityException(
                    "PlanningResumePoint.schemaVersion must be >= 1: ${resumePoint.schemaVersion}",
                )
            }

            return PlanningRunSuspension(
                resumePoint = resumePoint,
                suspensionHandle = suspensionHandle,
            )
        }
    }
}

package planning.infrastructure.runtime.orchestration

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.runtime.orchestration.PlanningResumePoint

/**
 * Immutable-orchestration record for one joined-wait suspension.
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

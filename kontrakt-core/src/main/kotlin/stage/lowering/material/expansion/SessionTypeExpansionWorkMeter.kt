package stage.lowering.material.expansion

import realization.planning.session.PlannerSession
import stage.canonicalization.material.TypeReference

/**
 * Session-bound implementation of TypeExpansionWorkMeter.
 *
 * It is intentionally tiny:
 * - no semantic decisions
 * - no projection
 * - no ordering
 * - no raw fact access
 *
 * It only maps event -> CostCenter and calls PlannerSession.step(center).
 */
class SessionTypeExpansionWorkMeter private constructor(
    private val session: PlannerSession,
) : TypeExpansionWorkMeter {
    override fun record(
        event: TypeExpansionWorkEvent,
        subject: TypeReference,
    ) {
        /*
         * subject is intentionally unused by the baseline meter.
         * It exists for deterministic diagnostics and future structured tracing.
         */
        session.step(
            TypeExpansionCostCenterMapper.map(event),
        )
    }

    companion object {
        @JvmStatic
        fun issue(session: PlannerSession): SessionTypeExpansionWorkMeter = SessionTypeExpansionWorkMeter(session)
    }
}

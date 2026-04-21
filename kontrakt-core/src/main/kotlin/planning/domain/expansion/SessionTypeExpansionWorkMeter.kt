package planning.domain.expansion

import metamodel.domain.vo.TypeReference
import planning.domain.session.PlannerSession

/**
 * Session-bound TypeExpansionWorkMeter implementation.
 *
 * Boundary rule:
 * - TypeExpansionPipeline emits closed TypeExpansionWorkEvent values.
 * - This meter maps those events to CostCenter.
 * - PlannerSession.step(center) remains the only runtime-metering mutation gate.
 *
 * This class is intentionally tiny.
 * It must not perform semantic decisions, shape resolution, raw fact resolution,
 * projection, ordering, or traversal.
 */
class SessionTypeExpansionWorkMeter private constructor(
    private val session: PlannerSession,
) : TypeExpansionWorkMeter {

    override fun record(
        event: TypeExpansionWorkEvent,
        subject: TypeReference,
    ) {
        /*
         * The subject is intentionally not consumed by this baseline meter.
         *
         * It is retained in the interface so future structured diagnostics or
         * trace sinks can attach deterministic subject identity without changing
         * TypeExpansionPipeline.
         */
        session.step(
            TypeExpansionCostCenterMapper.map(event),
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            session: PlannerSession,
        ): SessionTypeExpansionWorkMeter {
            return SessionTypeExpansionWorkMeter(session)
        }
    }
}
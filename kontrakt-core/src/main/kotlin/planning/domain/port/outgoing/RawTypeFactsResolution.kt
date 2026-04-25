package planning.domain.port.outgoing

import metamodel.domain.dto.RawTypeFactsDTO

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
/**
 * Port-return value for raw type-fact retrieval.
 *
 * This is not telemetry.
 * It is protocol-level accounting metadata required to distinguish:
 *
 * - cached already-ratified facts,
 * - actual backend fact discovery/reconciliation.
 */
class RawTypeFactsResolution private constructor(
    val facts: RawTypeFactsDTO,
    val kind: RawTypeFactsResolutionKind,
) {
    companion object {
        @JvmStatic
        fun cacheHit(
            facts: RawTypeFactsDTO,
        ): RawTypeFactsResolution {
            return RawTypeFactsResolution(
                facts = facts,
                kind = RawTypeFactsResolutionKind.CACHE_HIT,
            )
        }

        @JvmStatic
        fun actualResolution(
            facts: RawTypeFactsDTO,
        ): RawTypeFactsResolution {
            return RawTypeFactsResolution(
                facts = facts,
                kind = RawTypeFactsResolutionKind.ACTUAL_RESOLUTION,
            )
        }

        @Deprecated(
            message = "Use cacheHit(...) or actualResolution(...). This bridge conservatively charges as actual resolution.",
            replaceWith = ReplaceWith("RawTypeFactsResolution.actualResolution(facts)"),
            level = DeprecationLevel.WARNING,
        )
        @JvmStatic
        fun conservativeActualResolution(
            facts: RawTypeFactsDTO,
        ): RawTypeFactsResolution {
            return actualResolution(facts)
        }
    }
}
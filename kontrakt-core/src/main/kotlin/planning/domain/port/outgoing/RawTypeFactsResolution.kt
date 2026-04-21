package planning.domain.port.outgoing

import metamodel.domain.dto.RawTypeFactsDTO

/**
 * Port-return value for raw type-fact retrieval.
 *
 * This exists because the planner must meter raw-fact cache hit and actual
 * raw-fact resolution differently.
 *
 * It is not adapter telemetry.
 * It is protocol-level accounting metadata.
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
            return issue(
                facts = facts,
                kind = RawTypeFactsResolutionKind.CACHE_HIT,
            )
        }

        @JvmStatic
        fun actualResolution(
            facts: RawTypeFactsDTO,
        ): RawTypeFactsResolution {
            return issue(
                facts = facts,
                kind = RawTypeFactsResolutionKind.ACTUAL_RESOLUTION,
            )
        }

        @JvmStatic
        fun issue(
            facts: RawTypeFactsDTO,
            kind: RawTypeFactsResolutionKind,
        ): RawTypeFactsResolution {
            return RawTypeFactsResolution(
                facts = facts,
                kind = kind,
            )
        }

        /**
         * Migration bridge for providers that cannot yet distinguish cache hit from
         * actual backend resolution.
         *
         * This intentionally overcharges as ACTUAL_RESOLUTION, which is the safe
         * semantic accounting choice.
         */
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
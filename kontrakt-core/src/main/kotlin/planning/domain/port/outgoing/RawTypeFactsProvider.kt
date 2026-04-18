package planning.domain.port.outgoing

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.vo.TypeReference

/**
 * Outbound raw-fact port.
 *
 * Architectural role:
 * - Hexagonal driven port
 * - metamodel adapter boundary
 * - normalized raw structural fact supplier
 *
 * This port is intentionally separate from the older TypeFactsProvider.
 * It allows incremental migration without breaking all existing adapters at once.
 *
 * The provider owns:
 * - backend-specific discovery,
 * - source-artifact reconciliation,
 * - normalization before DTO emission,
 * - explicit unavailable/unknown sentinel emission,
 * - precomputed nodeIdentity64.
 *
 * The provider does NOT own:
 * - constructor selection,
 * - capability-based demotion,
 * - Active Member Set projection,
 * - uniqueness verification,
 * - canonical ordering,
 * - traversal input freezing,
 * - PlanCacheKey issuance,
 * - route64 derivation.
 */
interface RawTypeFactsProvider {
    fun resolveRawFacts(reference: TypeReference): RawTypeFactsDTO
}
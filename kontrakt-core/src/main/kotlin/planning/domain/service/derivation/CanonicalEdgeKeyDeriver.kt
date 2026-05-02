package planning.domain.service.derivation

import metamodel.domain.dto.MemberOrigin

/**
 * Deterministic edge-key lowering bound to edgeOrderingVersion.
 */
interface CanonicalEdgeKeyProvider {
    fun deriveEdgeKey(
        name: String,
        origin: MemberOrigin,
    ): Long
}

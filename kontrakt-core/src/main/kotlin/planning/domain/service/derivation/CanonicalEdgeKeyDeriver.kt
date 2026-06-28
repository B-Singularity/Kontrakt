package planning.domain.service.derivation

import stage.input.material.MemberOrigin

/**
 * Deterministic edge-key lowering bound to edgeOrderingVersion.
 */
interface CanonicalEdgeKeyProvider {
    fun deriveEdgeKey(
        name: String,
        origin: MemberOrigin,
    ): Long
}

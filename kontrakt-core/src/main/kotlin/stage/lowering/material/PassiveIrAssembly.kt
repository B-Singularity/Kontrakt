package stage.lowering.material

import stage.canonicalization.material.CanonicalSignature
import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Immutable result of passive IR assembly before canonical sealing.
 */
class PassiveIrAssembly private constructor(
    val payload: RawPayloadNode,
    val equalityKey: CanonicalSignature,
    val selfSemanticCostUpperBound: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            payload: RawPayloadNode,
            equalityKey: CanonicalSignature,
            selfSemanticCostUpperBound: Long,
        ): PassiveIrAssembly {
            if (selfSemanticCostUpperBound < 0L) {
                throw PlanningProtocolIntegrityException(
                    "PassiveIrAssembly.selfSemanticCostUpperBound must be >= 0: $selfSemanticCostUpperBound",
                )
            }
            return PassiveIrAssembly(
                payload = payload,
                equalityKey = equalityKey,
                selfSemanticCostUpperBound = selfSemanticCostUpperBound,
            )
        }
    }
}

package planning.domain.vo

import ir.identity.CanonicalSignature
import ir.plan.node.RawPayloadNode
import planning.domain.exception.PlanningProtocolIntegrityException

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
                    "PassiveIrAssembly.selfSemanticCostUpperBound must be >= 0: $selfSemanticCostUpperBound"
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
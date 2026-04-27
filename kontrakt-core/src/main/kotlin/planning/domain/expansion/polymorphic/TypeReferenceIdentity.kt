package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference
import planning.domain.canonical.text.CanonicalTextLaw
import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Semantic identity helper for TypeReference values inside polymorphic expansion.
 *
 * Rule:
 * - signature is the primary semantic identity surface for this layer;
 * - id and cycleId must still be valid canonical components;
 * - if two references have the same signature but inconsistent id/cycleId, this
 *   is upstream metamodel drift and must fail closed.
 *
 * This object does not use id as an ordering tie-breaker.
 */
internal object TypeReferenceIdentity {

    fun requireValid(reference: TypeReference) {
        CanonicalTextLaw.validateCanonicalComponent(
            field = "TypeReference.id",
            value = reference.id,
        )
        CanonicalTextLaw.validateCanonicalComponent(
            field = "TypeReference.cycleId",
            value = reference.cycleId,
        )
        CanonicalTextLaw.validateCanonicalComponent(
            field = "TypeReference.signature",
            value = reference.signature,
        )
    }

    fun compareBySignature(
        left: TypeReference,
        right: TypeReference,
    ): Int {
        requireValid(left)
        requireValid(right)

        val result = CanonicalTextLaw.compareCanonicalStrings(
            left.signature,
            right.signature,
        )

        if (result == 0) {
            requireCoherentSameSignature(left, right)
        }

        return result
    }

    fun sameSemanticType(
        left: TypeReference,
        right: TypeReference,
    ): Boolean {
        return compareBySignature(left, right) == 0
    }

    fun requireSameSemanticType(
        left: TypeReference,
        right: TypeReference,
        reason: String,
    ) {
        if (!sameSemanticType(left, right)) {
            throw TypeExpansionContractViolationException(
                reason = "$reason: left=${left.signature}, right=${right.signature}",
            )
        }
    }

    fun requireDistinctSemanticType(
        left: TypeReference,
        right: TypeReference,
        reason: String,
    ) {
        if (sameSemanticType(left, right)) {
            throw TypeExpansionContractViolationException(
                reason = "$reason: type=${left.signature}",
            )
        }
    }

    fun semanticHash(reference: TypeReference): Int {
        requireValid(reference)
        return reference.signature.hashCode()
    }

    private fun requireCoherentSameSignature(
        left: TypeReference,
        right: TypeReference,
    ) {
        if (left.id != right.id || left.cycleId != right.cycleId) {
            throw TypeExpansionContractViolationException(
                reason = "TypeReference drift: same signature but different id/cycleId. " +
                        "signature=${left.signature}, leftId=${left.id}, rightId=${right.id}, " +
                        "leftCycleId=${left.cycleId}, rightCycleId=${right.cycleId}",
            )
        }
    }
}
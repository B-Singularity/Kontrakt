package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Proof token produced by TypeReferenceFactory.
 *
 * TypeReference must not be manually assembled from unrelated id/cycle/signature
 * values. The factory derives all identity axes from one normalized source and
 * issues this proof.
 *
 * This is not cryptographic proof. It is a domain construction barrier.
 */
class TypeIdentityCoherenceProof private constructor(
    val proofId: String,
) {
    override fun equals(other: Any?): Boolean {
        return other is TypeIdentityCoherenceProof && proofId == other.proofId
    }

    override fun hashCode(): Int {
        return proofId.hashCode()
    }

    override fun toString(): String {
        return "TypeIdentityCoherenceProof($proofId)"
    }

    companion object {
        @JvmStatic
        fun issueFromFactory(proofId: String): TypeIdentityCoherenceProof {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "TypeIdentityCoherenceProof.proofId",
                value = proofId,
            )

            return TypeIdentityCoherenceProof(proofId)
        }
    }
}
package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference
import planning.domain.canonical.text.CanonicalTextLaw
import planning.domain.exception.TypeExpansionContractViolationException
import planning.domain.expansion.sequence.ExpansionSequence

/**
 * Deterministically ordered implementation candidate set.
 *
 * The set has a single contract origin.
 *
 * Empty set is allowed only through emptyFor(contractType), making vacancy an
 * explicit state instead of an accidental empty collection.
 */
class PolymorphicImplementationSet private constructor(
    val contractType: TypeReference,
    val candidates: ExpansionSequence<PolymorphicImplementationCandidate>,
) {
    val size: Int
        get() = candidates.size

    fun isEmpty(): Boolean = candidates.isEmpty()

    fun candidateAt(index: Int): PolymorphicImplementationCandidate = candidates[index]

    fun onlyCandidateOrNull(): PolymorphicImplementationCandidate? =
        if (candidates.size == 1) {
            candidates[0]
        } else {
            null
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PolymorphicImplementationSet) return false

        return TypeReferenceIdentity.sameSemanticType(contractType, other.contractType) &&
            candidates == other.candidates
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(contractType)
        result = 31 * result + candidates.hashCode()
        return result
    }

    override fun toString(): String = "PolymorphicImplementationSet(contract=${contractType.signature}, size=${candidates.size})"

    companion object {
        private val CANDIDATE_ORDER: Comparator<PolymorphicImplementationCandidate> =
            Comparator { left, right ->
                CanonicalTextLaw.compareCanonicalIdentifiers(
                    left.implementation.canonicalIdentifier,
                    right.implementation.canonicalIdentifier,
                )
            }

        @JvmStatic
        fun emptyFor(contractType: TypeReference): PolymorphicImplementationSet {
            TypeReferenceIdentity.requireValid(contractType)

            return PolymorphicImplementationSet(
                contractType = contractType,
                candidates = ExpansionSequence.empty(),
            )
        }

        @JvmStatic
        fun issue(
            contractType: TypeReference,
            candidates: Collection<PolymorphicImplementationCandidate>,
        ): PolymorphicImplementationSet {
            TypeReferenceIdentity.requireValid(contractType)

            if (candidates.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "Use PolymorphicImplementationSet.emptyFor(contractType) for explicit empty implementation set.",
                )
            }

            requireSingleContractOrigin(contractType, candidates)

            return PolymorphicImplementationSet(
                contractType = contractType,
                candidates =
                    ExpansionSequence.orderedStrict(
                        elements = candidates,
                        comparator = CANDIDATE_ORDER,
                        duplicateMessage = { left, right ->
                            "Duplicate polymorphic implementation candidate: " +
                                "${left.implementation.canonicalIdentifier} and ${right.implementation.canonicalIdentifier}"
                        },
                    ),
            )
        }

        private fun requireSingleContractOrigin(
            contractType: TypeReference,
            candidates: Collection<PolymorphicImplementationCandidate>,
        ) {
            val iterator = candidates.iterator()

            while (iterator.hasNext()) {
                val candidate = iterator.next()

                TypeReferenceIdentity.requireSameSemanticType(
                    left = contractType,
                    right = candidate.contractType,
                    reason = "PolymorphicImplementationSet contains candidate for a different contract type",
                )
            }
        }
    }
}

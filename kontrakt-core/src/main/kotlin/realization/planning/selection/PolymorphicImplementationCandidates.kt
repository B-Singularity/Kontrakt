package realization.planning.selection

import realization.planning.sequence.ExpansionSequence
import stage.canonicalization.contract.representative.MetamodelProtocolOrdering
import stage.canonicalization.material.representation.TypeReference
import stage.lowering.diagnostics.TypeExpansionContractViolationException

/**
 * Deterministically ordered implementation candidate set.
 *
 * This is not:
 *
 * - a runtime binding snapshot;
 * - a selected implementation;
 * - a DI container result;
 * - a reflection/KSP handle collection;
 * - a mutable candidate buffer;
 * - or a cache/interner key by itself.
 *
 * The set has a single contract origin.
 *
 * Empty-set law:
 *
 * Empty sets are allowed only through emptyFor(contractType).
 *
 * This makes vacancy an explicit state instead of an accidental empty
 * collection. Contract-subject vacancy policy is handled later by
 * PolymorphicExpansionPlan.ContractSubjectPlan.
 *
 * Contract-origin law:
 *
 * Every candidate in this set must belong to the same semantic contract type.
 *
 * Candidate ordering law:
 *
 * Candidates are ordered by implementation.canonicalIdentifier using the
 * metamodel order ordering primitive. Reflection/provider enumeration order
 * is never trusted.
 *
 * Duplicate law:
 *
 * The canonical implementation identifier is the duplicate identity inside one
 * contract's candidate set.
 *
 * If two candidates have the same canonicalIdentifier, the provider emitted
 * conflicting candidate material and the set fails closed.
 *
 * TypeReference trust law:
 *
 * contractType is a final domain-issued TypeReference VO.
 *
 * This class must not revalidate:
 *
 * - contractType.id.value;
 * - contractType.signature.value;
 * - contractType.cycleKey.value;
 * - contractType.useSiteAnnotations;
 * - contractType.coherenceProof.
 *
 * Their integrity is already enforced by TypeReference.issue(...).
 *
 * Resource law:
 *
 * Candidate count is capped before origin validation and deterministic ordering.
 * This prevents malformed providers from forcing unbounded O(N log N) sort work.
 *
 * Hash law:
 *
 * This class intentionally does not precompute hashCode yet.
 *
 * Hash precomputation should be decided together with candidate-set freezing,
 * interning, or allocation policy once this object's hot-path role is measured.
 */
class PolymorphicImplementationCandidates private constructor(
    val contractType: TypeReference,
    val candidates: ExpansionSequence<PolymorphicImplementationCandidate>,
) {
    val size: Int
        get() = candidates.size

    fun isEmpty(): Boolean {
        return candidates.isEmpty()
    }

    fun candidateAt(
        index: Int,
    ): PolymorphicImplementationCandidate {
        return candidates[index]
    }

    fun onlyCandidateOrNull(): PolymorphicImplementationCandidate? {
        return if (candidates.size == 1) {
            candidates[0]
        } else {
            null
        }
    }

    fun renderSummary(): String {
        return "PolymorphicImplementationCandidates(" +
                "contract=${contractType.signature.value}, " +
                "size=${candidates.size}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is PolymorphicImplementationCandidates) return false

        return TypeReferenceIdentity.sameSemanticType(
            left = contractType,
            right = other.contractType,
        ) &&
                candidates == other.candidates
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(contractType)
        result = 31 * result + candidates.hashCode()
        return result
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        /**
         * Hard order cap for one contract's implementation candidates.
         *
         * This is intentionally generous for ordinary polymorphic hierarchies.
         * If a future plugin ecosystem needs more, the limit must be raised by
         * policy, not bypassed by adapters.
         */
        const val MAX_IMPLEMENTATION_CANDIDATES: Int = 1_024

        private val CANDIDATE_ORDER: Comparator<PolymorphicImplementationCandidate> =
            Comparator { left, right ->
                MetamodelProtocolOrdering.compareUtf16CodeUnits(
                    left = left.implementation.canonicalIdentifier,
                    right = right.implementation.canonicalIdentifier,
                )
            }

        @JvmStatic
        fun emptyFor(
            contractType: TypeReference,
        ): PolymorphicImplementationCandidates {
            return PolymorphicImplementationCandidates(
                contractType = contractType,
                candidates = ExpansionSequence.empty(),
            )
        }

        @JvmStatic
        fun issue(
            contractType: TypeReference,
            candidates: Collection<PolymorphicImplementationCandidate>,
        ): PolymorphicImplementationCandidates {
            if (candidates.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "Use PolymorphicImplementationCandidates.emptyFor(contractType) " +
                            "for explicit empty implementation set.",
                )
            }

            requireCandidateCountWithinLimit(
                contractType = contractType,
                candidates = candidates,
            )

            requireSingleContractOrigin(
                contractType = contractType,
                candidates = candidates,
            )

            return PolymorphicImplementationCandidates(
                contractType = contractType,
                candidates =
                    ExpansionSequence.orderedStrict(
                        elements = candidates,
                        comparator = CANDIDATE_ORDER,
                        duplicateMessage = { left, right ->
                            "Duplicate polymorphic implementation candidate for contract=" +
                                    "${contractType.signature.value}: " +
                                    "identifier=${left.implementation.canonicalIdentifier}, " +
                                    "left=${left.renderSummary()}, " +
                                    "right=${right.renderSummary()}"
                        },
                    ),
            )
        }

        private fun requireCandidateCountWithinLimit(
            contractType: TypeReference,
            candidates: Collection<PolymorphicImplementationCandidate>,
        ) {
            if (candidates.size > MAX_IMPLEMENTATION_CANDIDATES) {
                throw TypeExpansionContractViolationException(
                    reason = "Polymorphic implementation candidate count exceeds cap=" +
                            "$MAX_IMPLEMENTATION_CANDIDATES: " +
                            "contract=${contractType.signature.value}, actual=${candidates.size}",
                )
            }
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
                    reason = "PolymorphicImplementationCandidates contains candidate for a different contract type",
                )
            }
        }
    }
}
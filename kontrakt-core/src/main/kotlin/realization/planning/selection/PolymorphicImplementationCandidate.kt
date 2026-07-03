package realization.planning.selection

import stage.canonicalization.material.representation.TypeReference

/**
 * Concrete implementation candidate for one interface / abstract contract.
 *
 * This is not:
 *
 * - a selected implementation;
 * - a runtime binding result;
 * - a DI container entry;
 * - a reflection/KSP handle;
 * - a materialization execution object;
 * - or a cache/interner key by itself.
 *
 * This value represents one possible concrete implementation for a polymorphic
 * contract surface.
 *
 * Identity law:
 *
 * The implementation identity is centralized in ConcreteImplementationReference.
 * This prevents FQCN-vs-TypeReference drift inside the candidate itself.
 *
 * Contract law:
 *
 * contractType is the requested polymorphic surface.
 *
 * implementation.type is the concrete/materializable implementation surface.
 *
 * A valid candidate must lower the contract to a semantically distinct concrete
 * implementation. If both sides are the same semantic type, this is not a
 * polymorphic implementation candidate; it belongs to identity materialization.
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
 * Equality law:
 *
 * contractType is compared by polymorphic semantic identity:
 *
 * - signature.value is the primary semantic surface;
 * - same signature with different id/cycleKey is upstream metamodel drift and
 *   must fail closed through TypeReferenceIdentity.
 *
 * implementation is compared by ConcreteImplementationReference equality.
 *
 * Hash law:
 *
 * This class intentionally does not precompute hashCode yet.
 *
 * Candidate-level hash precomputation should be decided together with
 * PolymorphicImplementationCandidates freezing / interning / allocation policy.
 */
class PolymorphicImplementationCandidate private constructor(
    val contractType: TypeReference,
    val implementation: ConcreteImplementationReference,
) {
    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is PolymorphicImplementationCandidate) return false

        return TypeReferenceIdentity.sameSemanticType(
            left = contractType,
            right = other.contractType,
        ) &&
                implementation == other.implementation
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(contractType)
        result = 31 * result + implementation.hashCode()
        return result
    }

    fun renderSummary(): String {
        return "PolymorphicImplementationCandidate(" +
                "contract=${contractType.signature.value}, " +
                "implementation=${implementation.canonicalIdentifier}, " +
                "materialization=${implementation.materializationKind.protocolToken}" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        fun issue(
            contractType: TypeReference,
            implementation: ConcreteImplementationReference,
        ): PolymorphicImplementationCandidate {
            TypeReferenceIdentity.requireDistinctSemanticType(
                left = contractType,
                right = implementation.type,
                reason = "Polymorphic implementation candidate must lower contract to a distinct concrete implementation",
            )

            return PolymorphicImplementationCandidate(
                contractType = contractType,
                implementation = implementation,
            )
        }
    }
}
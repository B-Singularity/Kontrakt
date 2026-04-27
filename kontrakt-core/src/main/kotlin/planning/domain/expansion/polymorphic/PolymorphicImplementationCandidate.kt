package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference

/**
 * Concrete implementation candidate for one interface / abstract contract.
 *
 * The implementation identity is centralized in ConcreteImplementationReference.
 * This avoids FQCN-vs-TypeReference drift inside the candidate itself.
 */
class PolymorphicImplementationCandidate private constructor(
    val contractType: TypeReference,
    val implementation: ConcreteImplementationReference,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PolymorphicImplementationCandidate) return false

        return TypeReferenceIdentity.sameSemanticType(contractType, other.contractType) &&
                implementation == other.implementation
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(contractType)
        result = 31 * result + implementation.hashCode()
        return result
    }

    override fun toString(): String {
        return "PolymorphicImplementationCandidate(contract=${contractType.signature}, implementation=${implementation.canonicalIdentifier})"
    }

    companion object {
        @JvmStatic
        fun issue(
            contractType: TypeReference,
            implementation: ConcreteImplementationReference,
        ): PolymorphicImplementationCandidate {
            TypeReferenceIdentity.requireValid(contractType)
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
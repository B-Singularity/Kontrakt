package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference

/**
 * Frozen host-project runtime binding fact.
 *
 * This is not resolved during planning.
 * It is supplied by the run-ratified RuntimeBindingSnapshot.
 *
 * This value must not hold runtime DI container handles, reflection objects, or
 * backend-native symbols.
 */
class ResolvedBinding private constructor(
    val requestedType: TypeReference,
    val selectedImplementation: ConcreteImplementationReference,
    val bindingKind: BindingKind,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResolvedBinding) return false

        return TypeReferenceIdentity.sameSemanticType(requestedType, other.requestedType) &&
                selectedImplementation == other.selectedImplementation &&
                bindingKind == other.bindingKind
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(requestedType)
        result = 31 * result + selectedImplementation.hashCode()
        result = 31 * result + bindingKind.protocolToken.hashCode()
        return result
    }

    override fun toString(): String {
        return buildString {
            append("ResolvedBinding(")
            append("requested=")
            append(requestedType.signature)
            append(", selected=")
            append(selectedImplementation.canonicalIdentifier)
            append(", kind=")
            append(bindingKind.protocolToken)
            append(')')
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
        ): ResolvedBinding {
            TypeReferenceIdentity.requireValid(requestedType)
            TypeReferenceIdentity.requireDistinctSemanticType(
                left = requestedType,
                right = selectedImplementation.type,
                reason = "ResolvedBinding must lower a request to a distinct concrete implementation",
            )

            return ResolvedBinding(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = bindingKind,
            )
        }
    }
}
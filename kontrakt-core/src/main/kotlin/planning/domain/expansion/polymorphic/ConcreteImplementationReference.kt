package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference
import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Provider-issued concrete implementation reference.
 *
 * This value is the concreteness boundary for polymorphic expansion.
 * A plain TypeReference is not enough because TypeReference does not encode
 * whether a type is instantiable.
 *
 * The provider must issue this value only after normalized type-shape checks
 * prove that the implementation is concrete/materializable.
 */
class ConcreteImplementationReference private constructor(
    val type: TypeReference,
    val canonicalIdentifier: String,
    val materializationKind: ImplementationMaterializationKind,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConcreteImplementationReference) return false

        return TypeReferenceIdentity.sameSemanticType(type, other.type) &&
                canonicalIdentifier == other.canonicalIdentifier &&
                materializationKind == other.materializationKind
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(type)
        result = 31 * result + canonicalIdentifier.hashCode()
        result = 31 * result + materializationKind.protocolToken.hashCode()
        return result
    }

    override fun toString(): String {
        return buildString {
            append("ConcreteImplementationReference(")
            append("type=")
            append(type.signature)
            append(", canonicalIdentifier=")
            append(canonicalIdentifier)
            append(", materializationKind=")
            append(materializationKind.protocolToken)
            append(')')
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            type: TypeReference,
            canonicalIdentifier: String,
            materializationKind: ImplementationMaterializationKind,
        ): ConcreteImplementationReference {
            TypeReferenceIdentity.requireValid(type)
            CanonicalTextLaw.validateCanonicalQualifiedIdentifier(
                field = "ConcreteImplementationReference.canonicalIdentifier",
                value = canonicalIdentifier,
            )

            return ConcreteImplementationReference(
                type = type,
                canonicalIdentifier = canonicalIdentifier,
                materializationKind = materializationKind,
            )
        }
    }
}
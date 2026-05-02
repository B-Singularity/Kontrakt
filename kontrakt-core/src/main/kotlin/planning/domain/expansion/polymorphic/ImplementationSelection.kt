package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference
import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Final selected implementation for dependency-site / structural-member
 * polymorphic expansion.
 *
 * This value is not merely a ResolvedBinding copy.
 *
 * It also represents:
 * - runtime binding selection;
 * - discovered single implementation selection;
 * - identity materialization for concrete data/test targets.
 */
class ImplementationSelection private constructor(
    val requestedType: TypeReference,
    val selectedImplementation: ConcreteImplementationReference,
    val bindingKind: BindingKind,
    val selectionMode: ImplementationSelectionMode,
) {
    val materializationKind: ImplementationMaterializationKind
        get() = selectedImplementation.materializationKind

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImplementationSelection) return false

        return TypeReferenceIdentity.sameSemanticType(requestedType, other.requestedType) &&
            selectedImplementation == other.selectedImplementation &&
            bindingKind == other.bindingKind &&
            selectionMode == other.selectionMode
    }

    override fun hashCode(): Int {
        var result = TypeReferenceIdentity.semanticHash(requestedType)
        result = 31 * result + selectedImplementation.hashCode()
        result = 31 * result + bindingKind.protocolToken.hashCode()
        result = 31 * result + selectionMode.protocolToken.hashCode()
        return result
    }

    override fun toString(): String =
        "ImplementationSelection(requested=${requestedType.signature}, selected=${selectedImplementation.canonicalIdentifier}, kind=${bindingKind.protocolToken}, mode=${selectionMode.protocolToken}, materialization=${materializationKind.protocolToken})"

    companion object {
        @JvmStatic
        fun strictPolymorphicLowering(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
        ): ImplementationSelection {
            TypeReferenceIdentity.requireDistinctSemanticType(
                left = requestedType,
                right = selectedImplementation.type,
                reason = "Strict polymorphic lowering must lower request to a distinct implementation",
            )

            return issueUnchecked(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = bindingKind,
                selectionMode = ImplementationSelectionMode.STRICT_POLYMORPHIC_LOWERING,
            )
        }

        @JvmStatic
        fun identityMaterialization(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
        ): ImplementationSelection {
            TypeReferenceIdentity.requireSameSemanticType(
                left = requestedType,
                right = selectedImplementation.type,
                reason = "Identity materialization requires selected implementation to equal requested type",
            )

            return issueUnchecked(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = BindingKind.IDENTITY_MATERIALIZATION,
                selectionMode = ImplementationSelectionMode.IDENTITY_MATERIALIZATION,
            )
        }

        @JvmStatic
        fun discoveredSingleImplementation(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
        ): ImplementationSelection =
            strictPolymorphicLowering(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = BindingKind.DISCOVERED_SINGLE_IMPLEMENTATION,
            )

        private fun issueUnchecked(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
            selectionMode: ImplementationSelectionMode,
        ): ImplementationSelection {
            TypeReferenceIdentity.requireValid(requestedType)

            if (selectionMode == ImplementationSelectionMode.IDENTITY_MATERIALIZATION &&
                bindingKind != BindingKind.IDENTITY_MATERIALIZATION
            ) {
                throw TypeExpansionContractViolationException(
                    reason = "IDENTITY_MATERIALIZATION selection mode requires IDENTITY_MATERIALIZATION binding kind.",
                )
            }

            return ImplementationSelection(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = bindingKind,
                selectionMode = selectionMode,
            )
        }
    }
}

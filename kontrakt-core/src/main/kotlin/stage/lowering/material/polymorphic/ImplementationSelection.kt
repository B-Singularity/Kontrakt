package stage.lowering.material.polymorphic

import stage.lowering.diagnostics.TypeExpansionContractViolationException
import stage.canonicalization.material.TypeReference

/**
 * Final selected implementation for dependency-site / structural-member
 * polymorphic expansion.
 *
 * This is not:
 *
 * - a raw runtime binding;
 * - a reflection/KSP handle;
 * - a DI container result;
 * - a mutable selection builder;
 * - a cache key;
 * - or a materialization execution object.
 *
 * This value represents the domain-ratified result of selecting an
 * implementation for a requested type.
 *
 * It covers:
 *
 * - runtime binding selection;
 * - discovered single implementation selection;
 * - identity materialization for concrete data/test targets;
 * - structural-member implementation selection;
 * - dependency-site implementation selection.
 *
 * TypeReference trust law:
 *
 * requestedType is a final domain-issued TypeReference VO.
 *
 * This class must not revalidate:
 *
 * - requestedType.id.value;
 * - requestedType.signature.value;
 * - requestedType.cycleKey.value;
 * - requestedType.useSiteAnnotations;
 * - requestedType.coherenceProof.
 *
 * Their integrity is already enforced by TypeReference.issue(...).
 *
 * Semantic identity law:
 *
 * For polymorphic expansion, requested type identity is compared by:
 *
 * - requestedType.signature.value as the primary semantic surface;
 * - requestedType.id.value and requestedType.cycleKey.value as coherence axes.
 *
 * Same signature with different id/cycleKey is upstream metamodel drift and must
 * fail closed. It is not a deterministic ordering tie-breaker.
 *
 * Selection law:
 *
 * STRICT_POLYMORPHIC_LOWERING:
 *
 * - requestedType and selectedImplementation.type must be semantically distinct.
 *
 * IDENTITY_MATERIALIZATION:
 *
 * - requestedType and selectedImplementation.type must be the same semantic type;
 * - bindingKind must be IDENTITY_MATERIALIZATION.
 *
 * Performance law:
 *
 * Frequently used requested-type identity strings and hashCode are captured at
 * issuance time. equals/hashCode must not call validation helpers or rescan
 * canonical text.
 *
 * Hash law:
 *
 * hashCode is precomputed for in-memory equality collections only.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - route key;
 * - cross-runtime order hash;
 * - serialized order digest.
 */
class ImplementationSelection private constructor(
    val requestedType: TypeReference,
    val selectedImplementation: ConcreteImplementationReference,
    val bindingKind: BindingKind,
    val selectionMode: ImplementationSelectionMode,
    private val requestedSignatureValue: String,
    private val requestedIdValue: String,
    private val requestedCycleKeyValue: String,
    private val precomputedHashCode: Int,
) {
    val materializationKind: ImplementationMaterializationKind
        get() = selectedImplementation.materializationKind

    fun renderSummary(): String {
        return "ImplementationSelection(" +
                "requested=$requestedSignatureValue, " +
                "selected=${selectedImplementation.canonicalIdentifier}, " +
                "bindingKind=${bindingKind.protocolToken}, " +
                "selectionMode=${selectionMode.protocolToken}, " +
                "materialization=${materializationKind.protocolToken}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is ImplementationSelection) return false

        /*
         * Cheap negative filter.
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        if (requestedSignatureValue != other.requestedSignatureValue) {
            return false
        }

        /*
         * Same requested signature with different identity axes is not a
         * tie-breaker. It is metamodel drift.
         */
        if (
            requestedIdValue != other.requestedIdValue ||
            requestedCycleKeyValue != other.requestedCycleKeyValue
        ) {
            throw TypeExpansionContractViolationException(
                reason = "ImplementationSelection drift: same requested signature " +
                        "but different TypeReference identity axes. " +
                        "signature=$requestedSignatureValue, " +
                        "leftId=$requestedIdValue, rightId=${other.requestedIdValue}, " +
                        "leftCycleKey=$requestedCycleKeyValue, rightCycleKey=${other.requestedCycleKeyValue}",
            )
        }

        return selectedImplementation == other.selectedImplementation &&
                bindingKind == other.bindingKind &&
                selectionMode == other.selectionMode
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

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
        ): ImplementationSelection {
            return strictPolymorphicLowering(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = BindingKind.DISCOVERED_SINGLE_IMPLEMENTATION,
            )
        }

        private fun issueUnchecked(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
            selectionMode: ImplementationSelectionMode,
        ): ImplementationSelection {
            requireBindingKindSelectionModeCoherence(
                bindingKind = bindingKind,
                selectionMode = selectionMode,
            )

            val requestedSignatureValue = requestedType.signature.value
            val requestedIdValue = requestedType.id.value
            val requestedCycleKeyValue = requestedType.cycleKey.value

            return ImplementationSelection(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = bindingKind,
                selectionMode = selectionMode,
                requestedSignatureValue = requestedSignatureValue,
                requestedIdValue = requestedIdValue,
                requestedCycleKeyValue = requestedCycleKeyValue,
                precomputedHashCode =
                    computeHashCode(
                        requestedSignatureValue = requestedSignatureValue,
                        selectedImplementation = selectedImplementation,
                        bindingKind = bindingKind,
                        selectionMode = selectionMode,
                    ),
            )
        }

        private fun requireBindingKindSelectionModeCoherence(
            bindingKind: BindingKind,
            selectionMode: ImplementationSelectionMode,
        ) {
            if (
                selectionMode == ImplementationSelectionMode.IDENTITY_MATERIALIZATION &&
                bindingKind != BindingKind.IDENTITY_MATERIALIZATION
            ) {
                throw TypeExpansionContractViolationException(
                    reason = "IDENTITY_MATERIALIZATION selection mode requires " +
                            "IDENTITY_MATERIALIZATION binding kind: actual=${bindingKind.protocolToken}",
                )
            }

            if (
                bindingKind == BindingKind.IDENTITY_MATERIALIZATION &&
                selectionMode != ImplementationSelectionMode.IDENTITY_MATERIALIZATION
            ) {
                throw TypeExpansionContractViolationException(
                    reason = "IDENTITY_MATERIALIZATION binding kind requires " +
                            "IDENTITY_MATERIALIZATION selection mode: actual=${selectionMode.protocolToken}",
                )
            }
        }

        private fun computeHashCode(
            requestedSignatureValue: String,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
            selectionMode: ImplementationSelectionMode,
        ): Int {
            var result = requestedSignatureValue.hashCode()
            result = 31 * result + selectedImplementation.hashCode()

            /*
             * Do not use enum ordinal.
             *
             * protocolOrder is the stable order ordering surface and avoids
             * repeated protocolToken.hashCode() work.
             */
            result = 31 * result + bindingKind.protocolOrder
            result = 31 * result + selectionMode.protocolOrder

            return result
        }
    }
}
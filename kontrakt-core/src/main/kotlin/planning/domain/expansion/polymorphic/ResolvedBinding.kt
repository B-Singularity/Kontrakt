package planning.domain.expansion.polymorphic

import metamodel.domain.vo.TypeReference
import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Frozen host-project runtime binding fact.
 *
 * This is not:
 *
 * - a mutable runtime binding;
 * - a DI container handle;
 * - a reflection/KSP handle;
 * - an execution object;
 * - a selected implementation plan;
 * - or a cache/interner key by itself.
 *
 * This value is supplied by the run-ratified RuntimeBindingSnapshot.
 * It is not resolved during planning.
 *
 * Runtime binding law:
 *
 * ResolvedBinding represents a binding that lowers a requested type surface to a
 * distinct concrete/materializable implementation.
 *
 * Therefore:
 *
 * - requestedType and selectedImplementation.type must be semantically distinct;
 * - BindingKind.IDENTITY_MATERIALIZATION is not valid here.
 *
 * Identity materialization is handled by ImplementationSelection.identityMaterialization(...)
 * without entering RuntimeBindingSnapshot as a runtime binding fact.
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
 * For runtime binding equality, requested type identity is compared by:
 *
 * - requestedType.signature.value as the primary semantic surface;
 * - requestedType.id.value and requestedType.cycleKey.value as coherence axes.
 *
 * Same signature with different id/cycleKey is upstream metamodel drift and must
 * fail closed. It is not a deterministic ordering tie-breaker.
 *
 * Performance law:
 *
 * RuntimeBindingSnapshot may contain many ResolvedBinding values and may use
 * them in duplicate detection, maps, or deterministic lookup structures.
 *
 * Therefore frequently used requested-type identity strings and hashCode are
 * captured at issuance time. equals/hashCode must not call validation helpers or
 * rescan canonical text.
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
 * - cross-runtime protocol hash;
 * - serialized protocol digest.
 */
class ResolvedBinding private constructor(
    val requestedType: TypeReference,
    val selectedImplementation: ConcreteImplementationReference,
    val bindingKind: BindingKind,
    private val requestedSignatureValue: String,
    private val requestedIdValue: String,
    private val requestedCycleKeyValue: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "ResolvedBinding(" +
                "requested=$requestedSignatureValue, " +
                "selected=${selectedImplementation.canonicalIdentifier}, " +
                "bindingKind=${bindingKind.protocolToken}, " +
                "materialization=${selectedImplementation.materializationKind.protocolToken}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is ResolvedBinding) return false

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
                reason = "ResolvedBinding drift: same requested signature but different " +
                        "TypeReference identity axes. " +
                        "signature=$requestedSignatureValue, " +
                        "leftId=$requestedIdValue, rightId=${other.requestedIdValue}, " +
                        "leftCycleKey=$requestedCycleKeyValue, rightCycleKey=${other.requestedCycleKeyValue}",
            )
        }

        return selectedImplementation == other.selectedImplementation &&
                bindingKind == other.bindingKind
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        fun issue(
            requestedType: TypeReference,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
        ): ResolvedBinding {
            requireBindingKindAllowed(
                bindingKind = bindingKind,
            )

            TypeReferenceIdentity.requireDistinctSemanticType(
                left = requestedType,
                right = selectedImplementation.type,
                reason = "ResolvedBinding must lower a request to a distinct concrete implementation",
            )

            val requestedSignatureValue = requestedType.signature.value
            val requestedIdValue = requestedType.id.value
            val requestedCycleKeyValue = requestedType.cycleKey.value

            return ResolvedBinding(
                requestedType = requestedType,
                selectedImplementation = selectedImplementation,
                bindingKind = bindingKind,
                requestedSignatureValue = requestedSignatureValue,
                requestedIdValue = requestedIdValue,
                requestedCycleKeyValue = requestedCycleKeyValue,
                precomputedHashCode =
                    computeHashCode(
                        requestedSignatureValue = requestedSignatureValue,
                        selectedImplementation = selectedImplementation,
                        bindingKind = bindingKind,
                    ),
            )
        }

        private fun requireBindingKindAllowed(
            bindingKind: BindingKind,
        ) {
            if (bindingKind == BindingKind.IDENTITY_MATERIALIZATION) {
                throw TypeExpansionContractViolationException(
                    reason = "ResolvedBinding must not use IDENTITY_MATERIALIZATION binding kind. " +
                            "Use ImplementationSelection.identityMaterialization(...) for identity materialization.",
                )
            }
        }

        private fun computeHashCode(
            requestedSignatureValue: String,
            selectedImplementation: ConcreteImplementationReference,
            bindingKind: BindingKind,
        ): Int {
            var result = requestedSignatureValue.hashCode()
            result = 31 * result + selectedImplementation.hashCode()

            /*
             * Do not use enum ordinal.
             *
             * protocolOrder is the stable protocol ordering surface and avoids
             * repeated protocolToken.hashCode() work.
             */
            result = 31 * result + bindingKind.protocolOrder

            return result
        }
    }
}
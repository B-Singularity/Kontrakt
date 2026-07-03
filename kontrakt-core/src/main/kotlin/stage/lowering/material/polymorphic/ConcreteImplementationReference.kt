package stage.lowering.material.polymorphic

import stage.canonicalization.contract.representative.MetamodelProtocolTextGuards
import stage.canonicalization.material.representation.TypeReference
import stage.lowering.diagnostics.TypeExpansionContractViolationException

/**
 * Provider-issued concrete implementation reference.
 *
 * This is the concreteness boundary for polymorphic expansion.
 *
 * A plain TypeReference is not enough because TypeReference describes a ratified
 * type identity, not whether that type is concrete, instantiable, proxy-backed,
 * factory-backed, or otherwise materializable.
 *
 * This value must be issued only after the provider/classifier/ratifier has
 * proven that the implementation is concrete or otherwise materializable under
 * ImplementationMaterializationKind.
 *
 * This is not:
 *
 * - a reflection KClass;
 * - a KSP declaration;
 * - a runtime object handle;
 * - a DI container binding;
 * - a cache key;
 * - or a canonical byte encoding.
 *
 * TypeReference trust law:
 *
 * TypeReference is a final domain-issued VO. This class must not revalidate:
 *
 * - type.id.value;
 * - type.signature.value;
 * - type.cycleKey.value;
 * - type.useSiteAnnotations;
 * - type.coherenceProof.
 *
 * Their integrity is already enforced by TypeReference.issue(...).
 *
 * Semantic identity law:
 *
 * For polymorphic expansion, implementation type identity is compared by the
 * same semantic axes used by TypeReferenceIdentity:
 *
 * - signature.value is the primary semantic surface;
 * - same signature with different id/cycleKey is upstream metamodel drift and
 *   must fail closed;
 * - canonicalIdentifier and materializationKind are additional implementation
 *   reference axes.
 *
 * Performance law:
 *
 * The frequently used semantic strings and hash code are captured at issuance
 * time. equals/hashCode must not rescan canonical text or call validation
 * helpers.
 *
 * Coherence law:
 *
 * canonicalIdentifier must be a bounded order text surface. This class does
 * not currently require canonicalIdentifier == type.id.value because different
 * adapters may choose a provider-specific implementation identifier surface.
 *
 * Strong binding between type and canonicalIdentifier belongs to the
 * implementation issuer/ratifier that knows the identifier policy.
 *
 * Hash law:
 *
 * hashCode is precomputed for in-memory equality collections only.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime order hash.
 */
class ConcreteImplementationReference private constructor(
    val type: TypeReference,
    val canonicalIdentifier: String,
    val materializationKind: ImplementationMaterializationKind,
    private val typeSignatureValue: String,
    private val typeIdValue: String,
    private val typeCycleKeyValue: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "ConcreteImplementationReference(" +
                "type=$typeSignatureValue, " +
                "canonicalIdentifier=$canonicalIdentifier, " +
                "materializationKind=${materializationKind.protocolToken}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is ConcreteImplementationReference) return false

        /*
         * Cheap negative filter.
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        if (canonicalIdentifier != other.canonicalIdentifier) {
            return false
        }

        if (materializationKind != other.materializationKind) {
            return false
        }

        if (typeSignatureValue != other.typeSignatureValue) {
            return false
        }

        /*
         * Same signature with different identity axes is not a tie-breaker.
         * It is upstream metamodel drift.
         */
        if (
            typeIdValue != other.typeIdValue ||
            typeCycleKeyValue != other.typeCycleKeyValue
        ) {
            throw TypeExpansionContractViolationException(
                reason = "ConcreteImplementationReference drift: same type signature " +
                        "but different TypeReference identity axes. " +
                        "signature=$typeSignatureValue, " +
                        "leftId=$typeIdValue, rightId=${other.typeIdValue}, " +
                        "leftCycleKey=$typeCycleKeyValue, rightCycleKey=${other.typeCycleKeyValue}",
            )
        }

        return true
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        const val MAX_CANONICAL_IDENTIFIER_CHARS: Int = 512

        @JvmStatic
        fun issue(
            type: TypeReference,
            canonicalIdentifier: String,
            materializationKind: ImplementationMaterializationKind,
        ): ConcreteImplementationReference {
            requireCanonicalIdentifierSurface(
                value = canonicalIdentifier,
            )

            val typeSignatureValue = type.signature.value
            val typeIdValue = type.id.value
            val typeCycleKeyValue = type.cycleKey.value

            return ConcreteImplementationReference(
                type = type,
                canonicalIdentifier = canonicalIdentifier,
                materializationKind = materializationKind,
                typeSignatureValue = typeSignatureValue,
                typeIdValue = typeIdValue,
                typeCycleKeyValue = typeCycleKeyValue,
                precomputedHashCode =
                    computeHashCode(
                        typeSignatureValue = typeSignatureValue,
                        canonicalIdentifier = canonicalIdentifier,
                        materializationKind = materializationKind,
                    ),
            )
        }

        private fun requireCanonicalIdentifierSurface(
            value: String,
        ) {
            if (value.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "ConcreteImplementationReference.canonicalIdentifier must not be empty.",
                )
            }

            if (value.length > MAX_CANONICAL_IDENTIFIER_CHARS) {
                throw TypeExpansionContractViolationException(
                    reason = "ConcreteImplementationReference.canonicalIdentifier exceeds order cap=" +
                            "$MAX_CANONICAL_IDENTIFIER_CHARS.",
                )
            }

            var index = 0
            while (index < value.length) {
                val c = value[index]

                if (MetamodelProtocolTextGuards.isReservedProtocolOrControl(c)) {
                    throw TypeExpansionContractViolationException(
                        reason = "ConcreteImplementationReference.canonicalIdentifier contains " +
                                "reserved order/control material at index=$index.",
                    )
                }

                if (MetamodelProtocolTextGuards.isAsciiWhitespace(c)) {
                    throw TypeExpansionContractViolationException(
                        reason = "ConcreteImplementationReference.canonicalIdentifier must not " +
                                "contain ASCII whitespace: index=$index.",
                    )
                }

                index += 1
            }
        }

        private fun computeHashCode(
            typeSignatureValue: String,
            canonicalIdentifier: String,
            materializationKind: ImplementationMaterializationKind,
        ): Int {
            var result = typeSignatureValue.hashCode()
            result = 31 * result + canonicalIdentifier.hashCode()
            result = 31 * result + materializationKind.protocolToken.hashCode()
            return result
        }
    }
}
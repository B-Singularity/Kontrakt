package stage.lowering.material.polymorphic

import stage.canonicalization.contract.representative.MetamodelProtocolOrdering
import stage.canonicalization.material.representation.TypeReference
import stage.lowering.diagnostics.TypeExpansionContractViolationException

/**
 * Semantic identity helper for TypeReference values inside polymorphic expansion.
 *
 * This is not:
 *
 * - a TypeReference validator;
 * - a canonical text validator;
 * - a metamodel ratifier;
 * - a TypeReference factory;
 * - a cache;
 * - or an interner.
 *
 * TypeReference trust law:
 *
 * TypeReference is a final domain-issued VO.
 *
 * It has already passed:
 *
 * - CanonicalTypeId issuance;
 * - TypeCycleKey issuance;
 * - CanonicalTypeSignature issuance;
 * - OrderedUseSiteAnnotations issuance;
 * - TypeIdentityCoherenceProof verification;
 * - TypeReference.issue(...).
 *
 * Therefore this helper must not re-scan or revalidate:
 *
 * - id.value;
 * - cycleKey.value;
 * - signature.value;
 * - use-site annotation surfaces.
 *
 * Doing so would duplicate metamodel-domain work and could drift from the
 * TypeReference issuance law.
 *
 * Semantic identity law:
 *
 * For polymorphic expansion, CanonicalTypeSignature.value is the primary
 * semantic identity surface.
 *
 * If two references have the same signature value but different id, cycleKey, or
 * signature metadata, that is not a stable ordering tie-breaker situation. It is
 * upstream metamodel drift and must fail closed.
 *
 * Ordering law:
 *
 * compareBySignature(...) compares signature.value using the metamodel order
 * UTF-16 code-unit ordering primitive.
 *
 * It does not use id as a tie-breaker. Same signature with incoherent identity
 * axes is rejected, not sorted.
 *
 * Hash law:
 *
 * semanticHash(...) is for in-memory semantic hash buckets only.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime order hash.
 *
 * This object does not maintain a global hash cache. If semantic keys become a
 * measured hotspot, add a dedicated scope-local TypeReferenceSemanticKey or
 * precomputed hash field to the owning VO in the allocation/interner phase.
 */
internal object TypeReferenceIdentity {
    fun compareBySignature(
        left: TypeReference,
        right: TypeReference,
    ): Int {
        val result =
            MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left.signature.value,
                right = right.signature.value,
            )

        if (result == 0) {
            requireCoherentSameSignature(
                left = left,
                right = right,
            )
        }

        return result
    }

    fun sameSemanticType(
        left: TypeReference,
        right: TypeReference,
    ): Boolean {
        return compareBySignature(
            left = left,
            right = right,
        ) == 0
    }

    fun requireSameSemanticType(
        left: TypeReference,
        right: TypeReference,
        reason: String,
    ) {
        if (!sameSemanticType(left, right)) {
            throw TypeExpansionContractViolationException(
                reason = "$reason: " +
                        "left=${left.signature.value}, right=${right.signature.value}",
            )
        }
    }

    fun requireDistinctSemanticType(
        left: TypeReference,
        right: TypeReference,
        reason: String,
    ) {
        if (sameSemanticType(left, right)) {
            throw TypeExpansionContractViolationException(
                reason = "$reason: type=${left.signature.value}",
            )
        }
    }

    /**
     * In-memory semantic hash for hash-bucket preselection.
     *
     * This intentionally uses the same semantic surface as compareBySignature:
     *
     *     signature.value
     *
     * Do not persist this value and do not use it as a canonical order hash.
     */
    fun semanticHash(
        reference: TypeReference,
    ): Int {
        return reference.signature.value.hashCode()
    }

    /**
     * Scope-local semantic key with precomputed hash.
     *
     * Use this when a local map/set needs semantic TypeReference identity and the
     * same keys will be probed repeatedly.
     *
     * This is not global interning.
     * This is not canonical state.
     * This is not persisted.
     */
    fun semanticKey(
        reference: TypeReference,
    ): SemanticKey {
        return SemanticKey.issue(reference)
    }

    private fun requireCoherentSameSignature(
        left: TypeReference,
        right: TypeReference,
    ) {
        /*
         * Same semantic signature must imply the same already-ratified identity
         * axes. If not, the metamodel emitted a chimera or drifted across
         * classifier/ratifier paths.
         */
        if (
            left.signature != right.signature ||
            left.id != right.id ||
            left.cycleKey != right.cycleKey
        ) {
            throw TypeExpansionContractViolationException(
                reason =
                    "TypeReference drift: same signature value but different identity axes. " +
                            "signature=${left.signature.value}, " +
                            "leftId=${left.id.value}, rightId=${right.id.value}, " +
                            "leftCycleKey=${left.cycleKey.value}, rightCycleKey=${right.cycleKey.value}, " +
                            "leftShape=${left.shapeSummary.kind.protocolToken}, " +
                            "rightShape=${right.shapeSummary.kind.protocolToken}",
            )
        }
    }

    /**
     * Local semantic TypeReference key.
     *
     * Equality is based on the same semantic law as TypeReferenceIdentity:
     *
     * - signature.value is primary;
     * - same signature value with incoherent identity axes fails closed.
     *
     * This key precomputes the semantic hash at construction time so local
     * hash-based work buffers do not repeatedly hash long generic signatures.
     */
    internal class SemanticKey private constructor(
        private val reference: TypeReference,
        private val signatureValue: String,
        private val precomputedHashCode: Int,
    ) {
        override fun equals(
            other: Any?,
        ): Boolean {
            if (this === other) return true
            if (other !is SemanticKey) return false

            if (precomputedHashCode != other.precomputedHashCode) {
                return false
            }

            val result =
                MetamodelProtocolOrdering.compareUtf16CodeUnits(
                    left = signatureValue,
                    right = other.signatureValue,
                )

            if (result != 0) {
                return false
            }

            requireCoherentSameSignature(
                left = reference,
                right = other.reference,
            )

            return true
        }

        override fun hashCode(): Int {
            return precomputedHashCode
        }

        override fun toString(): String {
            return "TypeReferenceSemanticKey(signature=$signatureValue)"
        }

        companion object {
            fun issue(
                reference: TypeReference,
            ): SemanticKey {
                val signatureValue = reference.signature.value

                return SemanticKey(
                    reference = reference,
                    signatureValue = signatureValue,
                    precomputedHashCode = signatureValue.hashCode(),
                )
            }
        }
    }
}
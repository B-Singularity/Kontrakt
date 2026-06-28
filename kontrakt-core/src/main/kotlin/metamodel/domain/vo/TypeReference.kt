package metamodel.domain.vo

import stage.input.diagnostics.MetamodelFactContractViolationException
import stage.input.material.OrderedUseSiteAnnotations
import stage.input.material.TypeCycleKey
import stage.input.material.TypeIdentityCoherenceProof
import stage.input.material.TypeNestingDepthLaw
import stage.input.material.TypeShapeSummary

/**
 * Domain-issued type reference.
 *
 * This is not:
 *
 * - a reflection KType;
 * - a KClass;
 * - a JVM descriptor;
 * - a KSP symbol;
 * - a source type node;
 * - a cache key;
 * - or a canonical byte encoding.
 *
 * TypeReference is the safe carrier that crosses from metamodel identity into
 * planning/type-expansion workflows.
 *
 * Issuance law:
 *
 * Adapters must not implement or manually assemble TypeReference. Reflection,
 * KSP, bytecode, and static-source adapters provide already-lowered candidate
 * material; the metamodel domain factory issues TypeReference after enforcing
 * identity coherence.
 *
 * Coherence law:
 *
 * The following axes must describe the same semantic type:
 *
 * - CanonicalTypeId;
 * - TypeCycleKey;
 * - CanonicalTypeSignature;
 * - OrderedUseSiteAnnotations;
 * - type nesting depth.
 *
 * TypeIdentityCoherenceProof is required at issuance time, but it is not a
 * semantic equality axis.
 *
 * Why proof is not an equality axis:
 *
 * The proof protects the construction boundary from chimera references such as:
 *
 * ```text
 * id from A + cycle key from B + signature from C + proof from D
 * ```
 *
 * Once issue(...) has verified proof coverage, semantic equality must be defined
 * by the actual ratified identity axes. A future proof schema/version change
 * must not change equality for the same semantic TypeReference.
 *
 * Shape law:
 *
 * id.shapeSummary and signature.shapeSummary must be equal.
 * cycleKey.shapeSummary.kind must agree with signature.shapeSummary.kind.
 *
 * Depth law:
 *
 * TypeReference carries a bounded structural type nesting depth. The value is
 * supplied by the issuing factory, which is the only component that knows the
 * fully lowered source graph.
 *
 * Hash law:
 *
 * hashCode() is precomputed for in-memory collections only.
 *
 * Do not use hashCode() as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime stable key;
 * - serialized order hash;
 * - L2 route64.
 *
 * Stable canonical hash / digest / route-key derivation belongs to the later
 * canonical encoding and interning phase.
 */
class TypeReference private constructor(
    val id: CanonicalTypeId,
    val cycleKey: TypeCycleKey,
    val signature: CanonicalTypeSignature,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val coherenceProof: TypeIdentityCoherenceProof,
    val typeNestingDepth: Int,
    private val precomputedHashCode: Int,
) {
    val shapeSummary: TypeShapeSummary
        get() = signature.shapeSummary

    /**
     * Returns a bounded diagnostic summary for error messages.
     *
     * Diagnostic law:
     *
     * - must be side-effect-free;
     * - must not perform lazy backend loading;
     * - must not access KType/KClass/KSType/KSDeclaration;
     * - must not consult adapter registries;
     * - must not consult source adapter provenance;
     * - must be derived only from already-ratified TypeReference value fields;
     * - must be safe after backend-handle erasure and frozen-image publication.
     *
     * This method is diagnostic material only.
     * It must not participate in semantic identity, ordering, route64,
     * PlanCacheKey, or canonical IR lowering.
     */
    fun renderSummary(): String =
        "TypeReference(" +
                "id=${id.value}, " +
                "shape=${shapeSummary.kind.protocolToken}, " +
                "arrayRank=${shapeSummary.arrayRank}, " +
                "genericArity=${shapeSummary.genericArity}, " +
                "typeDepth=$typeNestingDepth, " +
                "annotations=${useSiteAnnotations.size}" +
                ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeReference) return false

        /*
         * Keep id first.
         *
         * It is the most selective semantic axis and already includes:
         *
         * - canonical type text;
         * - shape summary;
         * - classifier law;
         * - ratification fingerprint.
         *
         * TypeIdentityCoherenceProof is intentionally excluded. It is an
         * issuance-time validation artifact, not part of semantic equality.
         */
        return id == other.id &&
                cycleKey == other.cycleKey &&
                signature == other.signature &&
                useSiteAnnotations == other.useSiteAnnotations &&
                typeNestingDepth == other.typeNestingDepth
    }

    override fun hashCode(): Int = precomputedHashCode

    override fun toString(): String = renderSummary()

    companion object {
        @JvmStatic
        fun issue(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            coherenceProof: TypeIdentityCoherenceProof,
            typeNestingDepth: Int,
        ): TypeReference {
            TypeNestingDepthLaw.requireWithinLimit(
                field = "TypeReference.typeNestingDepth",
                depth = typeNestingDepth,
            )

            requireShapeCoherence(
                id = id,
                cycleKey = cycleKey,
                signature = signature,
            )

            /*
             * Proof coverage is mandatory, but the proof does not become part of
             * equality/hash semantics after this point.
             */
            coherenceProof.requireCovers(
                id = id,
                cycleKey = cycleKey,
                signature = signature,
                useSiteAnnotations = useSiteAnnotations,
                typeNestingDepth = typeNestingDepth,
            )

            return TypeReference(
                id = id,
                cycleKey = cycleKey,
                signature = signature,
                useSiteAnnotations = useSiteAnnotations,
                coherenceProof = coherenceProof,
                typeNestingDepth = typeNestingDepth,
                precomputedHashCode =
                    computeSemanticHashCode(
                        id = id,
                        cycleKey = cycleKey,
                        signature = signature,
                        useSiteAnnotations = useSiteAnnotations,
                        typeNestingDepth = typeNestingDepth,
                    ),
            )
        }

        private fun computeSemanticHashCode(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            typeNestingDepth: Int,
        ): Int {
            var result = id.hashCode()
            result = 31 * result + cycleKey.hashCode()
            result = 31 * result + signature.hashCode()
            result = 31 * result + useSiteAnnotations.hashCode()
            result = 31 * result + typeNestingDepth
            return result
        }

        private fun requireShapeCoherence(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
        ) {
            if (id.shapeSummary != signature.shapeSummary) {
                throw MetamodelFactContractViolationException(
                    "TypeReference incoherent shape: " +
                            "idShape=${id.shapeSummary}, " +
                            "signatureShape=${signature.shapeSummary}, " +
                            "id=${id.value}",
                )
            }

            if (cycleKey.shapeSummary.kind != signature.shapeSummary.kind) {
                throw MetamodelFactContractViolationException(
                    "TypeReference incoherent cycle/signature kind: " +
                            "cycleShape=${cycleKey.shapeSummary}, " +
                            "signatureShape=${signature.shapeSummary}, " +
                            "id=${id.value}",
                )
            }
        }
    }
}
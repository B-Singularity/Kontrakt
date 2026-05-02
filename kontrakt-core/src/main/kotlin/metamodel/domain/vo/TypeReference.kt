package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

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
 * KSP, bytecode, and static-source adapters provide normalized materials; the
 * domain factory issues TypeReference after enforcing identity coherence.
 *
 * Coherence law:
 *
 * The following axes must describe the same semantic type:
 *
 * - CanonicalTypeId;
 * - TypeCycleKey;
 * - CanonicalTypeSignature;
 * - OrderedUseSiteAnnotations;
 * - TypeIdentityCoherenceProof.
 *
 * The proof is not ornamental. issue(...) must verify that the supplied
 * TypeIdentityCoherenceProof covers this exact tuple. This prevents chimera
 * references such as:
 *
 *     id from A + signature from B + proof from C
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
 * fully lowered source graph. This VO validates the cap and stores the depth so
 * downstream code can avoid recursive explosion.
 *
 * Hash law:
 *
 * hashCode() is intentionally not precomputed in this version. It may use
 * field hashCode values for in-memory HashMap/HashSet behavior only.
 *
 * Do not use hashCode() as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime stable key;
 * - serialized protocol hash.
 *
 * Hash caching/interner work is deferred to the canonical encoding / interning /
 * L2 integration phase.
 */
class TypeReference private constructor(
    val id: CanonicalTypeId,
    val cycleKey: TypeCycleKey,
    val signature: CanonicalTypeSignature,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val coherenceProof: TypeIdentityCoherenceProof,
    val typeNestingDepth: Int,
) {
    val shapeSummary: TypeShapeSummary
        get() = signature.shapeSummary

    fun renderSummary(): String {
        return "TypeReference(" +
                "id=${id.value}, " +
                "shape=${shapeSummary.kind.protocolToken}, " +
                "arrayRank=${shapeSummary.arrayRank}, " +
                "genericArity=${shapeSummary.genericArity}, " +
                "typeDepth=$typeNestingDepth, " +
                "annotations=${useSiteAnnotations.size}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is TypeReference) return false

        /*
         * Keep id first.
         *
         * It is the most selective axis and includes text, shape, classifier
         * law, and ratification fingerprint.
         */
        return id == other.id &&
                cycleKey == other.cycleKey &&
                signature == other.signature &&
                useSiteAnnotations == other.useSiteAnnotations &&
                coherenceProof == other.coherenceProof &&
                typeNestingDepth == other.typeNestingDepth
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cycleKey.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + useSiteAnnotations.hashCode()
        result = 31 * result + coherenceProof.hashCode()
        result = 31 * result + typeNestingDepth
        return result
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        /**
         * Structural type-depth cap for a single TypeReference.
         *
         * This protects downstream recursive comparison, expansion, diagnostics,
         * and adapter lowering paths from deeply nested malicious types such as:
         *
         *     List<List<List<...>>>
         *
         * The concrete factory is responsible for computing this depth while it
         * lowers the source type graph. This VO only validates the published
         * value.
         */
        const val MAX_TYPE_NESTING_DEPTH: Int = 64

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
            )
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
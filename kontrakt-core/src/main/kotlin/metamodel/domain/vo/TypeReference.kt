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
 * TypeReference is a ratified normalized fact that crosses from the metamodel
 * bounded context into planning/type-expansion workflows.
 *
 * Issuance law:
 *
 * Adapters must not implement, subclass, or manually assemble TypeReference.
 * They provide candidate material. The metamodel domain issuer creates the
 * final TypeReference after enforcing identity coherence.
 *
 * Proof law:
 *
 * [TypeIdentityCoherenceProof] is required during issuance to prevent chimera
 * references such as:
 *
 * ```text
 * id from A + cycle key from B + signature from C
 * ```
 *
 * However, the proof is not a semantic equality axis. Equality is defined by
 * the ratified type identity material itself.
 *
 * This prevents a future proof implementation change from changing semantic
 * TypeReference equality for the same type.
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
         * - canonical text;
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

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cycleKey.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + useSiteAnnotations.hashCode()
        result = 31 * result + typeNestingDepth
        return result
    }

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
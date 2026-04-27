package metamodel.domain.vo

/**
 * Domain-issued type reference.
 *
 * Adapters must not implement this type.
 * Reflection/KSP/bytecode/static-source adapters provide normalized materials;
 * the domain factory issues TypeReference after enforcing identity coherence.
 */
class TypeReference private constructor(
    val id: CanonicalTypeId,
    val cycleKey: TypeCycleKey,
    val signature: CanonicalTypeSignature,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val coherenceProof: TypeIdentityCoherenceProof,
) {
    val shapeSummary: TypeShapeSummary
        get() = signature.shapeSummary

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeReference) return false

        return id == other.id &&
                cycleKey == other.cycleKey &&
                signature == other.signature &&
                useSiteAnnotations == other.useSiteAnnotations &&
                coherenceProof == other.coherenceProof
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cycleKey.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + useSiteAnnotations.hashCode()
        result = 31 * result + coherenceProof.hashCode()
        return result
    }

    override fun toString(): String {
        return "TypeReference(id=$id, cycleKey=$cycleKey, signature=$signature, shape=$shapeSummary, annotations=${useSiteAnnotations.size})"
    }

    companion object {
        @JvmStatic
        fun issue(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            coherenceProof: TypeIdentityCoherenceProof,
        ): TypeReference {
            requireShapeCoherence(id, cycleKey, signature)

            return TypeReference(
                id = id,
                cycleKey = cycleKey,
                signature = signature,
                useSiteAnnotations = useSiteAnnotations,
                coherenceProof = coherenceProof,
            )
        }

        private fun requireShapeCoherence(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
        ) {
            if (id.shapeSummary != signature.shapeSummary) {
                throw IllegalArgumentException(
                    "TypeReference incoherent shape: id=${id.shapeSummary}, signature=${signature.shapeSummary}",
                )
            }

            if (cycleKey.shapeSummary.kind != signature.shapeSummary.kind) {
                throw IllegalArgumentException(
                    "TypeReference incoherent cycle/signature kind: cycle=${cycleKey.shapeSummary}, signature=${signature.shapeSummary}",
                )
            }
        }
    }
}
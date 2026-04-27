package metamodel.domain.vo

/**
 * Pre-lowered shape summary attached to canonical type identity surfaces.
 *
 * This prevents repeated string parsing in planning hot paths.
 */
class TypeShapeSummary private constructor(
    val kind: CanonicalTypeShapeKind,
    val genericArity: Int,
    val arrayRank: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeSummary) return false

        return kind == other.kind &&
                genericArity == other.genericArity &&
                arrayRank == other.arrayRank
    }

    override fun hashCode(): Int {
        var result = kind.protocolToken.hashCode()
        result = 31 * result + genericArity
        result = 31 * result + arrayRank
        return result
    }

    override fun toString(): String {
        return "TypeShapeSummary(kind=${kind.protocolToken}, genericArity=$genericArity, arrayRank=$arrayRank)"
    }

    companion object {
        @JvmStatic
        fun issue(
            kind: CanonicalTypeShapeKind,
            genericArity: Int,
            arrayRank: Int,
        ): TypeShapeSummary {
            require(genericArity >= 0) {
                "genericArity must be >= 0: $genericArity"
            }

            require(arrayRank >= 0) {
                "arrayRank must be >= 0: $arrayRank"
            }

            return TypeShapeSummary(
                kind = kind,
                genericArity = genericArity,
                arrayRank = arrayRank,
            )
        }
    }
}
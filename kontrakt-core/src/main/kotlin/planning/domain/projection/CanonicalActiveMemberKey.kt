package planning.domain.projection

/**
 * Canonical active-member ordering / uniqueness key.
 *
 * This key is semantic planning material.
 * It is not route64, not PlanCacheKey, and not an interner key.
 */
class CanonicalActiveMemberKey private constructor(
    val memberKindRank: Int,
    val name: String,
    val typeSignature: String,
    val declarationOrdinalLowered: Int,
) {
    fun render(): String =
        "kind=$memberKindRank;" +
            "name=${ProjectionOrderingPrimitives.renderField(name)};" +
            "typeSignature=${ProjectionOrderingPrimitives.renderField(typeSignature)};" +
            "declarationOrdinal=$declarationOrdinalLowered"

    companion object {
        private val COMPARATOR: Comparator<CanonicalActiveMemberKey> =
            Comparator { left, right ->
                ProjectionOrderingPrimitives
                    .compareInts(
                        left.memberKindRank,
                        right.memberKindRank,
                    ).takeIfNonZero()
                    ?: ProjectionOrderingPrimitives
                        .compareStrings(left.name, right.name)
                        .takeIfNonZero()
                    ?: ProjectionOrderingPrimitives
                        .compareStrings(left.typeSignature, right.typeSignature)
                        .takeIfNonZero()
                    ?: ProjectionOrderingPrimitives.compareInts(
                        left.declarationOrdinalLowered,
                        right.declarationOrdinalLowered,
                    )
            }

        @JvmStatic
        fun issue(member: ProjectedActiveMember): CanonicalActiveMemberKey =
            CanonicalActiveMemberKey(
                memberKindRank = ProjectionOrderingPrimitives.memberKindRank(member.memberKind),
                name = member.name,
                typeSignature = member.typeReference.signature,
                declarationOrdinalLowered = member.declarationOrdinal.lowerForPrimitiveOrdering(),
            )

        @JvmStatic
        fun comparator(): Comparator<CanonicalActiveMemberKey> = COMPARATOR

        private fun Int.takeIfNonZero(): Int? = ProjectionOrderingPrimitives.takeIfNonZero(this)
    }
}

package planning.domain.projection

/**
 * Entropy target key for projected active-member uniqueness.
 *
 * This is intentionally separate from CanonicalActiveMemberKey.
 *
 * CanonicalActiveMemberKey governs canonical ordering / edge-key uniqueness.
 * EntropyTargetKey guards entropy-target ambiguity.
 */
class EntropyTargetKey private constructor(
    val memberKindRank: Int,
    val name: String,
    val typeId: String,
) {
    fun render(): String {
        return "kind=$memberKindRank;" +
                "name=${ProjectionOrderingPrimitives.renderField(name)};" +
                "typeId=${ProjectionOrderingPrimitives.renderField(typeId)}"
    }

    companion object {
        private val COMPARATOR: Comparator<EntropyTargetKey> =
            Comparator { left, right ->
                ProjectionOrderingPrimitives.compareInts(
                    left.memberKindRank,
                    right.memberKindRank,
                ).takeIfNonZero()
                    ?: ProjectionOrderingPrimitives.compareStrings(left.name, right.name)
                        .takeIfNonZero()
                    ?: ProjectionOrderingPrimitives.compareStrings(left.typeId, right.typeId)
            }

        @JvmStatic
        fun issue(
            member: ProjectedActiveMember,
        ): EntropyTargetKey {
            return EntropyTargetKey(
                memberKindRank = ProjectionOrderingPrimitives.memberKindRank(member.memberKind),
                name = member.name,
                typeId = member.typeReference.id,
            )
        }

        @JvmStatic
        fun comparator(): Comparator<EntropyTargetKey> {
            return COMPARATOR
        }

        private fun Int.takeIfNonZero(): Int? {
            return ProjectionOrderingPrimitives.takeIfNonZero(this)
        }
    }
}
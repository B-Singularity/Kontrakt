package stage.lowering.material.projection

import stage.lowering.contract.PlanningProtocolTextGuards

/**
 * Canonical active-member ordering / uniqueness key.
 *
 * This key is semantic planning material.
 *
 * This is not:
 *
 * - route64;
 * - PlanCacheKey;
 * - an interner key;
 * - canonical byte encoding;
 * - adapter/source/reflection order.
 *
 * Identity law:
 *
 * Equality includes:
 *
 * - memberKindRank;
 * - name;
 * - typeSignature;
 * - declarationOrdinalLowered.
 *
 * Ordering law:
 *
 * Comparator order is order-defined and must not depend on reflection order,
 * source iteration order, enum ordinal, locale, or String.compareTo as an
 * implicit domain law.
 *
 * Hash law:
 *
 * hashCode is precomputed because this object is explicitly a uniqueness key.
 *
 * The precomputed hash is only for in-memory equality collections. It must not
 * be used as a persisted fingerprint, route key, cache key, or canonical digest.
 *
 * Resource law:
 *
 * name and typeSignature are length-bounded at issuance time.
 */
class CanonicalActiveMemberKey private constructor(
    val memberKindRank: Int,
    val name: String,
    val typeSignature: String,
    val declarationOrdinalLowered: Int,
    private val precomputedHashCode: Int,
) {
    fun render(): String {
        return "kind=$memberKindRank;" +
                "name=${ProjectionOrderingPrimitives.renderField(name)};" +
                "typeSignature=${ProjectionOrderingPrimitives.renderField(typeSignature)};" +
                "declarationOrdinal=$declarationOrdinalLowered"
    }

    fun renderSummary(): String {
        return "CanonicalActiveMemberKey(" +
                "kind=$memberKindRank, " +
                "name=${ProjectionOrderingPrimitives.renderField(name)}, " +
                "typeSignature=${ProjectionOrderingPrimitives.renderField(typeSignature)}, " +
                "declarationOrdinal=$declarationOrdinalLowered" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is CanonicalActiveMemberKey) return false

        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return memberKindRank == other.memberKindRank &&
                declarationOrdinalLowered == other.declarationOrdinalLowered &&
                name == other.name &&
                typeSignature == other.typeSignature
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        const val MAX_ACTIVE_MEMBER_NAME_CHARS: Int = 256
        const val MAX_ACTIVE_MEMBER_TYPE_SIGNATURE_CHARS: Int = 2_048

        private val COMPARATOR: Comparator<CanonicalActiveMemberKey> =
            Comparator { left, right ->
                ProjectionOrderingPrimitives
                    .compareInts(
                        left.memberKindRank,
                        right.memberKindRank,
                    ).takeIfNonZero()
                    ?: ProjectionOrderingPrimitives
                        .compareStrings(
                            left.name,
                            right.name,
                        ).takeIfNonZero()
                    ?: ProjectionOrderingPrimitives
                        .compareStrings(
                            left.typeSignature,
                            right.typeSignature,
                        ).takeIfNonZero()
                    ?: ProjectionOrderingPrimitives.compareInts(
                        left.declarationOrdinalLowered,
                        right.declarationOrdinalLowered,
                    )
            }

        @JvmStatic
        fun issue(
            member: ProjectedActiveMember,
        ): CanonicalActiveMemberKey {
            val name = member.name
            val typeSignature = member.typeReference.signature.value

            PlanningProtocolTextGuards.requireBoundedProtocolText(
                field = "CanonicalActiveMemberKey.name",
                value = name,
                maxChars = MAX_ACTIVE_MEMBER_NAME_CHARS,
            )

            PlanningProtocolTextGuards.requireBoundedProtocolText(
                field = "CanonicalActiveMemberKey.typeSignature",
                value = typeSignature,
                maxChars = MAX_ACTIVE_MEMBER_TYPE_SIGNATURE_CHARS,
            )

            val memberKindRank =
                ProjectionOrderingPrimitives.memberKindRank(
                    member.memberKind,
                )

            val declarationOrdinalLowered =
                member.declarationOrdinal.lowerForPrimitiveOrdering()

            return CanonicalActiveMemberKey(
                memberKindRank = memberKindRank,
                name = name,
                typeSignature = typeSignature,
                declarationOrdinalLowered = declarationOrdinalLowered,
                precomputedHashCode =
                    computeHashCode(
                        memberKindRank = memberKindRank,
                        name = name,
                        typeSignature = typeSignature,
                        declarationOrdinalLowered = declarationOrdinalLowered,
                    ),
            )
        }

        @JvmStatic
        fun comparator(): Comparator<CanonicalActiveMemberKey> {
            return COMPARATOR
        }

        private fun computeHashCode(
            memberKindRank: Int,
            name: String,
            typeSignature: String,
            declarationOrdinalLowered: Int,
        ): Int {
            var result = memberKindRank
            result = 31 * result + name.hashCode()
            result = 31 * result + typeSignature.hashCode()
            result = 31 * result + declarationOrdinalLowered
            return result
        }

        private fun Int.takeIfNonZero(): Int? {
            return ProjectionOrderingPrimitives.takeIfNonZero(this)
        }
    }
}
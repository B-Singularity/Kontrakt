package stage.lowering.material.projection

import stage.lowering.contract.PlanningProtocolTextGuards

/**
 * Entropy target key for projected active-member uniqueness.
 *
 * This is intentionally separate from CanonicalActiveMemberKey.
 *
 * CanonicalActiveMemberKey governs canonical ordering / edge-key uniqueness.
 * EntropyTargetKey guards entropy-target ambiguity.
 *
 * This is not:
 *
 * - HID output;
 * - canonical byte encoding;
 * - route64;
 * - PlanCacheKey;
 * - source-order material;
 * - reflection-order material;
 * - or an interner key.
 *
 * Identity law:
 *
 * Equality includes:
 *
 * - memberKindRank;
 * - name;
 * - typeId.
 *
 * This object is explicitly a uniqueness key. Therefore it must implement
 * structural equals/hashCode and must not fall back to JVM reference equality.
 *
 * Ordering law:
 *
 * Ordering is deterministic and order-defined:
 *
 * - memberKindRank;
 * - name;
 * - typeId.
 *
 * Do not insert hash values into the comparator. Hashes are for equality bucket
 * selection only, not for canonical ordering.
 *
 * Resource law:
 *
 * name and typeId are length-bounded at issue time. This prevents malformed
 * projected member material from forcing unbounded comparison, hashing, or
 * diagnostic rendering work.
 *
 * Rendering law:
 *
 * render() is diagnostic/planning rendering, not canonical byte encoding.
 *
 * Field rendering must be handled by ProjectionOrderingPrimitives.renderField(...)
 * using a boundary-safe representation, preferably length-prefixed.
 *
 * Hash law:
 *
 * hashCode is precomputed because this object is a uniqueness key and may be
 * used in duplicate-detection sets/maps during projection.
 *
 * The precomputed hash is only for in-memory equality collections.
 *
 * It must not be used as:
 *
 * - HID digest;
 * - canonical fingerprint;
 * - persisted identity;
 * - route key;
 * - cross-runtime order hash.
 */
class EntropyTargetKey private constructor(
    val memberKindRank: Int,
    val name: String,
    val typeId: String,
    private val precomputedHashCode: Int,
) {
    fun render(): String {
        return "kind=$memberKindRank;" +
                "name=${ProjectionOrderingPrimitives.renderField(name)};" +
                "typeId=${ProjectionOrderingPrimitives.renderField(typeId)}"
    }

    fun renderSummary(): String {
        return "EntropyTargetKey(" +
                "kind=$memberKindRank, " +
                "name=${ProjectionOrderingPrimitives.renderField(name)}, " +
                "typeId=${ProjectionOrderingPrimitives.renderField(typeId)}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is EntropyTargetKey) return false

        /*
         * Cheap negative filter.
         * Equality remains field-complete below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return memberKindRank == other.memberKindRank &&
                name == other.name &&
                typeId == other.typeId
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        /**
         * Projected member names should be small.
         *
         * 256 is intentionally generous for Kotlin/JVM property/function-like
         * names while bounding entropy-key work.
         */
        const val MAX_ENTROPY_TARGET_NAME_CHARS: Int = 256

        /**
         * Type ids may include nested generic structure, so this cap is larger
         * than the member-name cap.
         *
         * Keep this aligned with the canonical type-id/signature cap if that law
         * is centralized later.
         */
        const val MAX_ENTROPY_TARGET_TYPE_ID_CHARS: Int = 2_048

        private val COMPARATOR: Comparator<EntropyTargetKey> =
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
                    ?: ProjectionOrderingPrimitives.compareStrings(
                        left.typeId,
                        right.typeId,
                    )
            }

        @JvmStatic
        fun issue(
            member: ProjectedActiveMember,
        ): EntropyTargetKey {
            val name = member.name
            val typeId = member.typeReference.id.value

            PlanningProtocolTextGuards.requireBoundedProtocolText(
                field = "EntropyTargetKey.name",
                value = name,
                maxChars = MAX_ENTROPY_TARGET_NAME_CHARS,
            )

            PlanningProtocolTextGuards.requireBoundedProtocolText(
                field = "EntropyTargetKey.typeId",
                value = typeId,
                maxChars = MAX_ENTROPY_TARGET_TYPE_ID_CHARS,
            )

            val memberKindRank =
                ProjectionOrderingPrimitives.memberKindRank(
                    member.memberKind,
                )

            return EntropyTargetKey(
                memberKindRank = memberKindRank,
                name = name,
                typeId = typeId,
                precomputedHashCode =
                    computeHashCode(
                        memberKindRank = memberKindRank,
                        name = name,
                        typeId = typeId,
                    ),
            )
        }

        @JvmStatic
        fun comparator(): Comparator<EntropyTargetKey> {
            return COMPARATOR
        }

        private fun computeHashCode(
            memberKindRank: Int,
            name: String,
            typeId: String,
        ): Int {
            var result = memberKindRank
            result = 31 * result + name.hashCode()
            result = 31 * result + typeId.hashCode()
            return result
        }

        private fun Int.takeIfNonZero(): Int? {
            return ProjectionOrderingPrimitives.takeIfNonZero(this)
        }
    }
}
package planning.domain.expansion.seed

import metamodel.domain.protocol.MetamodelProtocolTextGuards
import metamodel.domain.vo.TypeReference
import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Local HID selector tuple.
 *
 * This is not:
 *
 * - canonical IR;
 * - path material;
 * - source-order material;
 * - reflection/KSP enumeration material;
 * - parent pointer material;
 * - absolute node path material;
 * - mutable collection index material;
 * - or a cache/interner key by itself.
 *
 * This tuple is created at the Local IR assembly boundary when projected
 * semantic material becomes a concrete expansion obligation.
 *
 * Examples:
 *
 * - projected active member -> local expansion edge;
 * - collection element -> local element materialization edge;
 * - map key/value -> local synthetic edge;
 * - polymorphic implementation -> local concrete implementation edge;
 * - atomic value -> local seed-governed materialization edge.
 *
 * HID law:
 *
 * LocalSelectorTuple participates in Hierarchical Identity Derivation as local
 * selector input.
 *
 * A deterministic child materialization choice must be derived from:
 *
 * - parent deterministic entropy or parent semantic identity;
 * - this local selector tuple;
 * - explicit version tuple;
 * - deterministic keyed derivation.
 *
 * This object deliberately exposes typed fields only.
 *
 * It does not expose canonicalComponentAt(...), byte rendering, or string
 * concatenation APIs because canonical byte encoding must be handled later by
 * the HID encoder / CanonicalSignatureProvider using tagged, length-prefixed,
 * version-bound encoding.
 *
 * TypeReference trust law:
 *
 * subjectType is a final domain-issued TypeReference VO.
 *
 * This class must not revalidate:
 *
 * - subjectType.id.value;
 * - subjectType.signature.value;
 * - subjectType.cycleKey.value;
 * - subjectType.useSiteAnnotations;
 * - subjectType.coherenceProof.
 *
 * Their integrity is already enforced by TypeReference.issue(...).
 *
 * Semantic member identity law:
 *
 * semanticMemberIdentity is local semantic identity material, not display text.
 *
 * It must be:
 *
 * - non-empty;
 * - length-bounded;
 * - free of reserved protocol delimiter '|';
 * - free of C0/C1 control characters;
 * - free of ASCII whitespace.
 *
 * The concrete meaning of semanticMemberIdentity depends on label:
 *
 * - ACTIVE_MEMBER: projected member identity;
 * - COLLECTION_ELEMENT: stable element selector;
 * - ARRAY_COMPONENT: stable component selector;
 * - MAP_KEY / MAP_VALUE: synthetic map edge identity;
 * - POLYMORPHIC_IMPLEMENTATION: implementation candidate identity;
 * - ATOMIC_VALUE: atomic materialization identity.
 *
 * Equality law:
 *
 * subjectType is compared by semantic type identity:
 *
 * - signature.value is the primary semantic surface;
 * - id.value and cycleKey.value are coherence axes.
 *
 * Same signature with different id/cycleKey is upstream metamodel drift and must
 * fail closed. It is not a deterministic ordering tie-breaker.
 *
 * Performance law:
 *
 * LocalSelectorTuple may be created in large numbers during Local IR assembly
 * and may be used in HID derivation work buffers or duplicate checks.
 *
 * Therefore frequently used subject type identity strings and hashCode are
 * captured at issuance time. equals/hashCode must not call validation helpers or
 * rescan canonical type text.
 *
 * Hash law:
 *
 * hashCode is precomputed for in-memory equality collections only.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - HID digest;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime protocol hash;
 * - serialized protocol digest.
 */
class LocalSelectorTuple private constructor(
    val label: LocalSelectorLabel,
    val semanticMemberIdentity: String,
    val localOrdinal: CanonicalLocalOrdinal,
    val slotPhase: LocalSelectorSlotPhase,
    val subjectType: TypeReference,
    private val subjectSignatureValue: String,
    private val subjectIdValue: String,
    private val subjectCycleKeyValue: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "LocalSelectorTuple(" +
                "label=${label.protocolToken}, " +
                "semanticMemberIdentity=$semanticMemberIdentity, " +
                "localOrdinal=${localOrdinal.value}, " +
                "slotPhase=${slotPhase.protocolToken}, " +
                "subjectType=$subjectSignatureValue" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is LocalSelectorTuple) return false

        /*
         * Cheap negative filter.
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        if (label != other.label) {
            return false
        }

        if (semanticMemberIdentity != other.semanticMemberIdentity) {
            return false
        }

        if (localOrdinal != other.localOrdinal) {
            return false
        }

        if (slotPhase != other.slotPhase) {
            return false
        }

        if (subjectSignatureValue != other.subjectSignatureValue) {
            return false
        }

        /*
         * Same subject signature with different identity axes is not a tie-breaker.
         * It is metamodel drift.
         */
        if (
            subjectIdValue != other.subjectIdValue ||
            subjectCycleKeyValue != other.subjectCycleKeyValue
        ) {
            throw TypeExpansionContractViolationException(
                reason = "LocalSelectorTuple drift: same subject signature but different " +
                        "TypeReference identity axes. " +
                        "signature=$subjectSignatureValue, " +
                        "leftId=$subjectIdValue, rightId=${other.subjectIdValue}, " +
                        "leftCycleKey=$subjectCycleKeyValue, rightCycleKey=${other.subjectCycleKeyValue}",
            )
        }

        return true
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        const val MAX_SEMANTIC_MEMBER_IDENTITY_CHARS: Int = 512

        @JvmStatic
        fun issue(
            label: LocalSelectorLabel,
            semanticMemberIdentity: String,
            localOrdinal: CanonicalLocalOrdinal,
            slotPhase: LocalSelectorSlotPhase,
            subjectType: TypeReference,
        ): LocalSelectorTuple {
            requireSemanticMemberIdentitySurface(
                semanticMemberIdentity = semanticMemberIdentity,
            )

            val subjectSignatureValue = subjectType.signature.value
            val subjectIdValue = subjectType.id.value
            val subjectCycleKeyValue = subjectType.cycleKey.value

            return LocalSelectorTuple(
                label = label,
                semanticMemberIdentity = semanticMemberIdentity,
                localOrdinal = localOrdinal,
                slotPhase = slotPhase,
                subjectType = subjectType,
                subjectSignatureValue = subjectSignatureValue,
                subjectIdValue = subjectIdValue,
                subjectCycleKeyValue = subjectCycleKeyValue,
                precomputedHashCode =
                    computeHashCode(
                        label = label,
                        semanticMemberIdentity = semanticMemberIdentity,
                        localOrdinal = localOrdinal,
                        slotPhase = slotPhase,
                        subjectSignatureValue = subjectSignatureValue,
                    ),
            )
        }

        private fun requireSemanticMemberIdentitySurface(
            semanticMemberIdentity: String,
        ) {
            if (semanticMemberIdentity.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "LocalSelectorTuple.semanticMemberIdentity must not be empty.",
                )
            }

            if (semanticMemberIdentity.length > MAX_SEMANTIC_MEMBER_IDENTITY_CHARS) {
                throw TypeExpansionContractViolationException(
                    reason = "LocalSelectorTuple.semanticMemberIdentity exceeds protocol cap=" +
                            "$MAX_SEMANTIC_MEMBER_IDENTITY_CHARS.",
                )
            }

            var index = 0
            while (index < semanticMemberIdentity.length) {
                val c = semanticMemberIdentity[index]

                if (MetamodelProtocolTextGuards.isReservedProtocolOrControl(c)) {
                    throw TypeExpansionContractViolationException(
                        reason = "LocalSelectorTuple.semanticMemberIdentity contains " +
                                "reserved protocol/control material at index=$index.",
                    )
                }

                if (MetamodelProtocolTextGuards.isAsciiWhitespace(c)) {
                    throw TypeExpansionContractViolationException(
                        reason = "LocalSelectorTuple.semanticMemberIdentity must not contain " +
                                "ASCII whitespace: index=$index.",
                    )
                }

                index += 1
            }
        }

        private fun computeHashCode(
            label: LocalSelectorLabel,
            semanticMemberIdentity: String,
            localOrdinal: CanonicalLocalOrdinal,
            slotPhase: LocalSelectorSlotPhase,
            subjectSignatureValue: String,
        ): Int {
            /*
             * Do not use enum ordinal.
             *
             * LocalSelectorLabel and LocalSelectorSlotPhase currently expose
             * protocolToken rather than protocolOrder, so token hash is computed
             * once at issuance and cached in this tuple's precomputed hash.
             */
            var result = label.protocolToken.hashCode()
            result = 31 * result + semanticMemberIdentity.hashCode()
            result = 31 * result + localOrdinal.value
            result = 31 * result + slotPhase.protocolToken.hashCode()
            result = 31 * result + subjectSignatureValue.hashCode()
            return result
        }
    }
}
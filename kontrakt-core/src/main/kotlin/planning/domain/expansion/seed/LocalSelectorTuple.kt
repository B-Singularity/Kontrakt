package planning.domain.expansion.seed

import metamodel.domain.vo.TypeReference
import planning.domain.canonical.text.CanonicalTextLaw
import planning.domain.expansion.polymorphic.TypeReferenceIdentity

/**
 * Local HID selector tuple.
 *
 * This tuple is created at the Local IR assembly boundary when projected semantic
 * material becomes a concrete expansion obligation.
 *
 * It is not path material.
 * It must not encode parent path, absolute node path, reflection order, or
 * mutable collection index.
 *
 * This value deliberately exposes typed fields only.
 * It does not provide canonicalComponentAt(...) string rendering because canonical
 * byte encoding must be handled later by CanonicalSignatureProvider using tagged /
 * length-prefixed encoding.
 */
class LocalSelectorTuple private constructor(
    val label: LocalSelectorLabel,
    val semanticMemberIdentity: String,
    val localOrdinal: CanonicalLocalOrdinal,
    val slotPhase: LocalSelectorSlotPhase,
    val subjectType: TypeReference,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalSelectorTuple) return false

        return label == other.label &&
                semanticMemberIdentity == other.semanticMemberIdentity &&
                localOrdinal == other.localOrdinal &&
                slotPhase == other.slotPhase &&
                TypeReferenceIdentity.sameSemanticType(subjectType, other.subjectType)
    }

    override fun hashCode(): Int {
        var result = label.protocolToken.hashCode()
        result = 31 * result + semanticMemberIdentity.hashCode()
        result = 31 * result + localOrdinal.value
        result = 31 * result + slotPhase.protocolToken.hashCode()
        result = 31 * result + TypeReferenceIdentity.semanticHash(subjectType)
        return result
    }

    override fun toString(): String {
        return buildString {
            append("LocalSelectorTuple(")
            append("label=")
            append(label.protocolToken)
            append(", semanticMemberIdentity=")
            append(semanticMemberIdentity)
            append(", localOrdinal=")
            append(localOrdinal.value)
            append(", slotPhase=")
            append(slotPhase.protocolToken)
            append(", subjectType=")
            append(subjectType.signature)
            append(')')
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            label: LocalSelectorLabel,
            semanticMemberIdentity: String,
            localOrdinal: CanonicalLocalOrdinal,
            slotPhase: LocalSelectorSlotPhase,
            subjectType: TypeReference,
        ): LocalSelectorTuple {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "LocalSelectorTuple.semanticMemberIdentity",
                value = semanticMemberIdentity,
            )

            TypeReferenceIdentity.requireValid(subjectType)

            return LocalSelectorTuple(
                label = label,
                semanticMemberIdentity = semanticMemberIdentity,
                localOrdinal = localOrdinal,
                slotPhase = slotPhase,
                subjectType = subjectType,
            )
        }
    }
}
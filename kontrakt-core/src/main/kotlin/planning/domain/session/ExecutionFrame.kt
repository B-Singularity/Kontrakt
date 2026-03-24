package planning.domain.session

import ir.identity.CanonicalSignature
import metamodel.domain.vo.TypeReference
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.port.outgoing.OrderedActiveMembers

/**
 * Explicit execution frame for the iterative DFS machine.
 *
 * Frames are immutable descriptors.
 * Rollback-relevant mutable state belongs to session-owned primitive structures.
 */
internal sealed interface ExecutionFrame {
    val typeReference: TypeReference
    val tx: TransactionalFrame
}

internal class PlanNodeFrame private constructor(
    override val typeReference: TypeReference,
    val incomingEdgeRank: Long,
    val incomingEdgeStageTag: Byte,
    val incomingExpandExecutionIndex: Int,
    val incomingMemberIndex: Int,
    override val tx: TransactionalFrame,
) : ExecutionFrame {

    fun hasIncomingEdge(): Boolean = incomingExpandExecutionIndex >= 0

    companion object {
        private const val NO_EXECUTION_INDEX: Int = -1
        private const val NO_MEMBER_INDEX: Int = -1
        private const val NO_STAGE_TAG: Byte = 0

        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            incomingEdgeRank: Long = -1L,
            incomingEdgeStageTag: Byte = NO_STAGE_TAG,
            incomingExpandExecutionIndex: Int = NO_EXECUTION_INDEX,
            incomingMemberIndex: Int = NO_MEMBER_INDEX,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): PlanNodeFrame {
            return PlanNodeFrame(
                typeReference = typeReference,
                incomingEdgeRank = incomingEdgeRank,
                incomingEdgeStageTag = incomingEdgeStageTag,
                incomingExpandExecutionIndex = incomingExpandExecutionIndex,
                incomingMemberIndex = incomingMemberIndex,
                tx = tx,
            )
        }
    }
}

internal class IterateMembersFrame private constructor(
    override val typeReference: TypeReference,
    val orderedMembers: OrderedActiveMembers,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            orderedMembers: OrderedActiveMembers,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): IterateMembersFrame {
            return IterateMembersFrame(
                typeReference = typeReference,
                orderedMembers = orderedMembers,
                tx = tx,
            )
        }
    }
}

internal class ExpandEdgeFrame private constructor(
    override val typeReference: TypeReference,
    val orderedMembers: OrderedActiveMembers,
    val memberCursorSlot: Int,
    val memberCount: Int,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            orderedMembers: OrderedActiveMembers,
            memberCursorSlot: Int,
            memberCount: Int,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): ExpandEdgeFrame {
            if (memberCursorSlot < 0) {
                throw PlanningProtocolIntegrityException(
                    "ExpandEdgeFrame.memberCursorSlot must be >= 0: $memberCursorSlot"
                )
            }
            if (memberCount < 0) {
                throw PlanningProtocolIntegrityException(
                    "ExpandEdgeFrame.memberCount must be >= 0: $memberCount"
                )
            }
            return ExpandEdgeFrame(
                typeReference = typeReference,
                orderedMembers = orderedMembers,
                memberCursorSlot = memberCursorSlot,
                memberCount = memberCount,
                tx = tx,
            )
        }
    }
}

internal class AllocateFrame private constructor(
    override val typeReference: TypeReference,
    val orderedMembers: OrderedActiveMembers,
    val signature: CanonicalSignature,
    val childResultStart: Int,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            orderedMembers: OrderedActiveMembers,
            signature: CanonicalSignature,
            childResultStart: Int,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): AllocateFrame {
            if (childResultStart < 0) {
                throw PlanningProtocolIntegrityException(
                    "AllocateFrame.childResultStart must be >= 0: $childResultStart"
                )
            }
            return AllocateFrame(
                typeReference = typeReference,
                orderedMembers = orderedMembers,
                signature = signature,
                childResultStart = childResultStart,
                tx = tx,
            )
        }
    }
}
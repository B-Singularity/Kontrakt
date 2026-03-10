package planning.domain.session

import ir.identity.CanonicalSignature
import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO
import metamodel.domain.vo.TypeReference
import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Explicit execution frame for the iterative DFS machine.
 *
 * Transactional rollback state is intentionally delegated to [TransactionalFrame].
 */
internal sealed interface ExecutionFrame {
    val typeReference: TypeReference
    val tx: TransactionalFrame
}

internal class PlanNodeFrame private constructor(
    override val typeReference: TypeReference,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): PlanNodeFrame = PlanNodeFrame(typeReference, tx)
    }
}

internal class IterateMembersFrame private constructor(
    override val typeReference: TypeReference,
    val facts: TypeFactsDTO,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            facts: TypeFactsDTO,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): IterateMembersFrame = IterateMembersFrame(typeReference, facts, tx)
    }
}

internal class ExpandEdgeFrame private constructor(
    override val typeReference: TypeReference,
    val facts: TypeFactsDTO,
    val member: MemberFact?,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            facts: TypeFactsDTO,
            member: MemberFact? = null,
            tx: TransactionalFrame = TransactionalFrame.issue(),
        ): ExpandEdgeFrame = ExpandEdgeFrame(typeReference, facts, member, tx)
    }
}

internal class AllocateFrame private constructor(
    override val typeReference: TypeReference,
    val facts: TypeFactsDTO,
    val signature: CanonicalSignature,
    val childResultStart: Int,
    override val tx: TransactionalFrame,
) : ExecutionFrame {
    companion object {
        @JvmStatic
        fun issue(
            typeReference: TypeReference,
            facts: TypeFactsDTO,
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
                facts = facts,
                signature = signature,
                childResultStart = childResultStart,
                tx = tx,
            )
        }
    }
}
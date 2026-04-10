package planning.domain.vo

import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO
import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Boundary object representing the protocol-ratified active member set.
 *
 * Invariants:
 * - members belong to exactly one deterministically selected constructor + eligible properties
 * - order is already ratified by the protocol comparator
 * - planner traversal MUST consume this view, not the raw facts.members list
 */
class OrderedActiveMembers private constructor(
    private val ownerFacts: TypeFactsDTO,
    private val orderedMembers: Array<MemberFact>,
) {
    fun ownerFacts(): TypeFactsDTO = ownerFacts

    fun size(): Int = orderedMembers.size

    fun memberAt(index: Int): MemberFact {
        if (index < 0 || index >= orderedMembers.size) {
            throw PlanningProtocolIntegrityException(
                "OrderedActiveMembers.memberAt index out of bounds: $index"
            )
        }
        return orderedMembers[index]
    }

    companion object {
        @JvmStatic
        fun issue(
            ownerFacts: TypeFactsDTO,
            orderedMembers: Array<MemberFact>,
        ): OrderedActiveMembers {
            return OrderedActiveMembers(
                ownerFacts = ownerFacts,
                orderedMembers = orderedMembers.copyOf(),
            )
        }
    }
}
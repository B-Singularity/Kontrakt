package planning.domain.service.derivation

import metamodel.domain.dto.TypeFactsDTO
import planning.domain.vo.OrderedActiveMembers

/**
 * Port that ratifies order ordering for active members.
 *
 * The planner core MUST NOT trust arbitrary adapter list order.
 */
interface ActiveMemberOrdering {
    fun ratify(facts: TypeFactsDTO): OrderedActiveMembers
}

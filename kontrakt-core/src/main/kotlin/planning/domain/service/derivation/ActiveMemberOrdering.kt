package planning.domain.service.derivation

import planning.domain.vo.OrderedActiveMembers
import stage.input.material.TypeFactsDTO

/**
 * Port that ratifies order ordering for active members.
 *
 * The planner core MUST NOT trust arbitrary adapter list order.
 */
interface ActiveMemberOrdering {
    fun ratify(facts: TypeFactsDTO): OrderedActiveMembers
}

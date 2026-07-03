package realization.identity.derivation

import stage.input.presentation.dto.TypeFactsDTO
import stage.lowering.material.OrderedActiveMembers

/**
 * Port that ratifies order ordering for active members.
 *
 * The planner core MUST NOT trust arbitrary adapter list order.
 */
interface ActiveMemberOrdering {
    fun ratify(facts: TypeFactsDTO): OrderedActiveMembers
}

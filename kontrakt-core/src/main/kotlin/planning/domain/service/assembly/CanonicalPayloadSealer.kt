package planning.domain.service.assembly

import ir.plan.node.CanonicalPlanNode
import ir.plan.node.RawCycleBreakPayload
import ir.plan.node.RawPayloadNode

/**
 * Adapter-local canonical sealing boundary.
 *
 * Important:
 * - RawPayloadNode remains the original raw order type
 * - cycle truncation uses a separate concrete raw payload type
 * - no synthetic common supertype is introduced here
 */
interface CanonicalPayloadSealer {
    fun seal(payload: RawPayloadNode): CanonicalPlanNode

    fun sealCycleBreak(payload: RawCycleBreakPayload): CanonicalPlanNode
}

package planning.domain.service.assembly

import stage.lowering.material.CanonicalPlanNode
import stage.lowering.material.RawCycleBreakPayload
import stage.lowering.material.RawPayloadNode

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

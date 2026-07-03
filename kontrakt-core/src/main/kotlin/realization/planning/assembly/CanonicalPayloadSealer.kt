package realization.planning.assembly

import stage.lowering.material.candidate.CanonicalPlanNode
import stage.lowering.material.candidate.RawCycleBreakPayload
import stage.lowering.material.candidate.RawPayloadNode

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

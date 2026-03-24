package ir.plan.node

import ir.exception.IrProtocolViolationException
import planning.domain.port.outgoing.BreakpointStage

/**
 * Raw planner-originated payload for deterministic cycle truncation.
 *
 * This is a separate concrete raw type.
 * It does NOT replace or weaken RawPayloadNode.
 */
class RawCycleBreakPayload private constructor(
    val ownerTypeId: RawTypeId,
    val edgeName: String,
    val stage: BreakpointStage,
    val structuralPath: String,
    val reason: String?,
) {
    init {
        if (edgeName.isEmpty()) {
            throw IrProtocolViolationException("RawCycleBreakPayload.edgeName must not be empty.")
        }
        if (structuralPath.isEmpty()) {
            throw IrProtocolViolationException("RawCycleBreakPayload.structuralPath must not be empty.")
        }
    }

    override fun toString(): String =
        "RawCycleBreakPayload(ownerTypeId=$ownerTypeId, edgeName=$edgeName, stage=$stage, structuralPath=$structuralPath)"

    companion object {
        @JvmStatic
        fun issue(
            ownerTypeId: RawTypeId,
            edgeName: String,
            stage: BreakpointStage,
            structuralPath: String,
            reason: String?,
        ): RawCycleBreakPayload {
            return RawCycleBreakPayload(
                ownerTypeId = ownerTypeId,
                edgeName = edgeName,
                stage = stage,
                structuralPath = structuralPath,
                reason = reason,
            )
        }
    }
}
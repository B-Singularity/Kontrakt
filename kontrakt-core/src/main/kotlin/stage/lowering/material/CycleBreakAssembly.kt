package stage.lowering.material

import stage.canonicalization.material.CanonicalSignature
import stage.lowering.contract.BreakpointStage
import stage.lowering.diagnostics.FaultKind
import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Deterministic cycle-break assembly below canonical sealing.
 */
class CycleBreakAssembly private constructor(
    val payload: RawCycleBreakPayload,
    val equalityKey: CanonicalSignature,
    val stage: BreakpointStage,
    val structuralPath: String,
    val reason: String?,
    val faultKind: FaultKind?,
    val semanticCostUpperBound: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            payload: RawCycleBreakPayload,
            equalityKey: CanonicalSignature,
            stage: BreakpointStage,
            structuralPath: String,
            reason: String?,
            faultKind: FaultKind?,
            semanticCostUpperBound: Long,
        ): CycleBreakAssembly {
            if (structuralPath.isEmpty()) {
                throw PlanningProtocolIntegrityException(
                    "CycleBreakAssembly.structuralPath must not be empty.",
                )
            }
            if (semanticCostUpperBound < 0L) {
                throw PlanningProtocolIntegrityException(
                    "CycleBreakAssembly.semanticCostUpperBound must be >= 0.",
                )
            }
            return CycleBreakAssembly(
                payload = payload,
                equalityKey = equalityKey,
                stage = stage,
                structuralPath = structuralPath,
                reason = reason,
                faultKind = faultKind,
                semanticCostUpperBound = semanticCostUpperBound,
            )
        }
    }
}
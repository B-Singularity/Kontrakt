package realization.planning.assembly

import stage.input.presentation.dto.MemberFact
import stage.input.presentation.dto.TypeFactsDTO
import stage.lowering.contract.BreakpointStage
import stage.lowering.material.candidate.CycleBreakAssembly

interface CycleBreakPayloadAssembler {
    fun assemble(
        ownerFacts: TypeFactsDTO,
        member: MemberFact,
        stage: BreakpointStage,
    ): CycleBreakAssembly
}

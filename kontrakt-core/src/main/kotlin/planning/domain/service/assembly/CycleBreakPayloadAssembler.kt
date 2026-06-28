package planning.domain.service.assembly

import stage.lowering.contract.BreakpointStage
import stage.lowering.material.CycleBreakAssembly
import stage.input.material.MemberFact
import stage.input.material.TypeFactsDTO

interface CycleBreakPayloadAssembler {
    fun assemble(
        ownerFacts: TypeFactsDTO,
        member: MemberFact,
        stage: BreakpointStage,
    ): CycleBreakAssembly
}

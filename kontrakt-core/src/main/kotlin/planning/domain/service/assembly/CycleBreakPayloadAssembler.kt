package planning.domain.service.assembly

import planning.domain.protocol.BreakpointStage
import planning.domain.vo.CycleBreakAssembly
import stage.input.material.MemberFact
import stage.input.material.TypeFactsDTO

interface CycleBreakPayloadAssembler {
    fun assemble(
        ownerFacts: TypeFactsDTO,
        member: MemberFact,
        stage: BreakpointStage,
    ): CycleBreakAssembly
}

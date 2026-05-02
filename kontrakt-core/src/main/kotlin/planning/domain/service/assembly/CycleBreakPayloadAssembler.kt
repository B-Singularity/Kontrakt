package planning.domain.service.assembly

import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO
import planning.domain.protocol.BreakpointStage
import planning.domain.vo.CycleBreakAssembly

interface CycleBreakPayloadAssembler {
    fun assemble(
        ownerFacts: TypeFactsDTO,
        member: MemberFact,
        stage: BreakpointStage,
    ): CycleBreakAssembly
}

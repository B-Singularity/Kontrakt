package planning.domain.port.outgoing

import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.TypeFactsDTO

interface CycleBreakPayloadAssembler {
    fun assemble(
        ownerFacts: TypeFactsDTO,
        member: MemberFact,
        stage: BreakpointStage,
    ): CycleBreakAssembly
}
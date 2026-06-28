package planning.domain.service.assembly

import planning.domain.vo.PassiveIrAssembly
import stage.input.material.TypeFactsDTO

/**
 * Outbound port for passive assembly.
 *
 * Final-form contract:
 * - returns RawPayloadNode, never CanonicalPlanNode
 * - keeps canonical sealing inside the intern boundary
 */
interface PassiveIrAssembler {
    fun assemble(
        facts: TypeFactsDTO,
        children: ChildResultSlice,
    ): PassiveIrAssembly
}

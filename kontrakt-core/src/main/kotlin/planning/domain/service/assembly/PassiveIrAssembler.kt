package planning.domain.service.assembly

import metamodel.domain.dto.TypeFactsDTO
import planning.domain.vo.PassiveIrAssembly

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
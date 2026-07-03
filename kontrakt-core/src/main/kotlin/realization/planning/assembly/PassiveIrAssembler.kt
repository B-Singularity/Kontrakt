package realization.planning.assembly

import stage.input.presentation.dto.TypeFactsDTO
import stage.lowering.material.candidate.PassiveIrAssembly

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

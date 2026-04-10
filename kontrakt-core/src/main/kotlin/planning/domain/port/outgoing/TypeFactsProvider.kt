package planning.domain.port.outgoing

import metamodel.domain.dto.TypeFactsDTO
import metamodel.domain.vo.TypeReference

/**
 * Outbound fact port.
 *
 * The core MUST consume normalized fact DTOs rather than raw reflection
 * or bytecode APIs.
 */
interface TypeFactsProvider {
    fun resolveFacts(reference: TypeReference): TypeFactsDTO
}

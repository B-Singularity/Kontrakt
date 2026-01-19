package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationRequest

interface TypeGenerator {
    fun supports(request: GenerationRequest): Boolean
}

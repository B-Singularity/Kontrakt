package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.context.generation.GenerationRequest

interface RecursiveGenerator : TypeGenerator {
    fun generator(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): Any?

    fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> = emptyList()

    fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> = emptyList()
}

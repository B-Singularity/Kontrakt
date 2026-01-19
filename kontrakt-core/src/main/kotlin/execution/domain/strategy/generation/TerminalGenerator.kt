package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.context.generation.GenerationRequest

interface TerminalGenerator : TypeGenerator {
    fun generate(
        request: GenerationRequest,
        context: GenerationContext,
    ): Any?

    fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
    ): List<Any?> = emptyList()

    fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
    ): List<Any?> = emptyList()
}

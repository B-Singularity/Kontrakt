package execution.domain.generator

interface TerminalGenerator : TypeGenerator {
    fun generate(request: GenerationRequest, context: GenerationContext): Any?

    fun generateValidBoundaries(request: GenerationRequest, context: GenerationContext): List<Any?> = emptyList()

    fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext
    ): List<Any?> = emptyList()
}
package execution.domain.generator

interface RecursiveGenerator : TypeGenerator {

    fun generator(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Any?

    fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?> = emptyList()

    fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?> = emptyList()
}
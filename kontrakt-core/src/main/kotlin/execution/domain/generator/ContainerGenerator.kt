package execution.domain.generator

interface ContainerGenerator : TypeGenerator {
    override fun generate(request: GenerationRequest, context: GenerationContext): Any? {
        throw UnsupportedOperationException("ContainerGenerator needs a regenerator callback.")
    }

    fun generate(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Any?

    fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?>
}
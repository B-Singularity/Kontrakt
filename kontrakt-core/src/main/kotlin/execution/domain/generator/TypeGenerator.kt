package execution.domain.generator

interface TypeGenerator {
    fun supports(request: GenerationRequest): Boolean
}

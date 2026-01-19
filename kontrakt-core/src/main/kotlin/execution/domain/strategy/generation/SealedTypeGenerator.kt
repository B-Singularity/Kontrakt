package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.context.generation.GenerationRequest
import execution.exception.SealedClassHasNoSubclassesException
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

class SealedTypeGenerator : RecursiveGenerator {
    override fun supports(request: GenerationRequest): Boolean {
        val kClass = request.type.classifier as? KClass<*> ?: return false
        return kClass.isSealed
    }

    override fun generator(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): Any? {
        val kClass = request.type.classifier as KClass<*>
        val subclasses = kClass.sealedSubclasses

        if (subclasses.isEmpty()) {
            throw SealedClassHasNoSubclassesException(request.type)
        }

        val targetClass = subclasses.random(context.seededRandom)
        val delegateRequest = GenerationRequest.from(targetClass.starProjectedType, request.name)

        return regenerator(delegateRequest, context)
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> {
        val kClass = request.type.classifier as KClass<*>

        return kClass.sealedSubclasses.mapNotNull { subclass ->
            val delegateRequest = GenerationRequest.from(subclass.starProjectedType, request.name)
            regenerator(delegateRequest, context)
        }
    }
}

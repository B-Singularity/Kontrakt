package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.context.generation.GenerationRequest
import execution.exception.GenerationFailedException
import execution.exception.RecursiveGenerationFailedException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ObjectGenerator : RecursiveGenerator {
    override fun supports(request: GenerationRequest): Boolean {
        val kClass = request.type.classifier as? KClass<*> ?: return false

        return !kClass.isAbstract &&
            !kClass.isSealed &&
            !kClass.java.isEnum &&
            kClass.constructors.isNotEmpty()
    }

    override fun generator(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): Any? {
        val type = request.type
        val kClass = type.classifier as KClass<*>

        if (context.history.contains(kClass)) {
            if (type.isMarkedNullable) return null

            throw RecursiveGenerationFailedException(
                type = type,
                path = context.history.map { it.simpleName ?: "Unknown" } + (kClass.simpleName ?: "Unknown"),
                cause = IllegalStateException("Infinite recursion detected in object graph."),
            )
        }

        val newContext = context.copy(history = context.history + kClass)

        val constructor = kClass.primaryConstructor ?: kClass.constructors.first()

        return try {
            val args =
                constructor.parameters.map { param ->
                    val paramRequest =
                        GenerationRequest(
                            type = param.type,
                            name = param.name ?: "arg",
                            annotations = param.annotations,
                        )

                    regenerator(paramRequest, newContext)
                }

            constructor.call(*args.toTypedArray())
        } catch (e: Exception) {
            throw GenerationFailedException(
                type = type,
                part = "Constructor(${constructor.parameters.joinToString { it.name ?: "?" }})",
                cause = e,
            )
        }
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> = listOf(generator(request, context, regenerator))

    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> = emptyList()
}

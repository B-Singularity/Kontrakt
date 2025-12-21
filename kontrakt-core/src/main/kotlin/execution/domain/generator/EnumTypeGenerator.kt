package execution.domain.generator

import execution.exception.GenerationFailedException
import kotlin.reflect.KClass

class EnumTypeGenerator : TerminalGenerator {

    override fun supports(request: GenerationRequest): Boolean {
        val kClass = request.type.classifier as? KClass<*> ?: return false
        return kClass.java.isEnum
    }

    override fun generate(request: GenerationRequest, context: GenerationContext): Any? {
        val kClass = request.type.classifier as KClass<*>
        val constants = kClass.java.enumConstants

        if (constants.isNullOrEmpty()) {
            throw GenerationFailedException(
                request.type,
                "Enum '${kClass.simpleName}' defines no constants, so it cannot be instantiated."
            )
        }

        return constants.random(context.seededRandom)
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext
    ): List<Any?> {
        val kClass = request.type.classifier as KClass<*>
        return kClass.java.enumConstants?.toList() ?: emptyList()
    }
}
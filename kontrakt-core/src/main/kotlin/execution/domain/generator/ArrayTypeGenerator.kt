package execution.domain.generator

import discovery.api.Size
import execution.exception.CollectionSizeLimitExceededException
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType
import java.lang.reflect.Array as JavaArray

class ArrayTypeGenerator : RecursiveGenerator {
    private companion object {
        const val DEFAULT_SIZE = 5
        const val GLOBAL_LIMIT = 1_000
    }

    override fun supports(request: GenerationRequest): Boolean {
        val kClass = request.type.classifier as? KClass<*> ?: return false
        return kClass.java.isArray
    }

    override fun generator(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): Any {
        val sizeAnnotation = request.find<Size>()
        val (min, max, isMaxExplicit) = parseSizeBoundaries(sizeAnnotation)

        val targetSize =
            when {
                isMaxExplicit -> context.seededRandom.nextInt(min, max + 1)
                else -> maxOf(min, DEFAULT_SIZE)
            }

        validateSafetyLimit(targetSize, min, sizeAnnotation?.ignoreLimit == true)

        return createArray(request, context, targetSize, regenerator)
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> =
        buildList {
            val sizeAnnotation = request.find<Size>()
            val (min, max, _) = parseSizeBoundaries(sizeAnnotation)
            val ignoreLimit = sizeAnnotation?.ignoreLimit == true

            if (isValidSize(min, ignoreLimit)) {
                add(createArray(request, context, min, regenerator))
            }
            if (max != min && max != Int.MAX_VALUE && isValidSize(max, ignoreLimit)) {
                add(createArray(request, context, max, regenerator))
            }
        }

    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): List<Any?> =
        buildList {
            val sizeAnnotation = request.find<Size>() ?: return@buildList
            val (min, max, _) = parseSizeBoundaries(sizeAnnotation)
            val ignoreLimit = sizeAnnotation.ignoreLimit

            if (min > 0) {
                add(createArray(request, context, min - 1, regenerator))
            }

            if (max != Int.MAX_VALUE) {
                val invalidSize = max + 1
                if (isValidSize(invalidSize, ignoreLimit)) {
                    add(createArray(request, context, invalidSize, regenerator))
                }
            }
        }

    private fun createArray(
        request: GenerationRequest,
        context: GenerationContext,
        size: Int,
        regenerator: (GenerationRequest, GenerationContext) -> Any?,
    ): Any {
        val kClass = request.type.classifier as KClass<*>
        val javaClass = kClass.java
        val componentType = javaClass.componentType

        val array = JavaArray.newInstance(componentType, size)

        val elementType =
            if (componentType.isPrimitive) {
                componentType.kotlin.starProjectedType
            } else {
                request.type.arguments
                    .firstOrNull()
                    ?.type ?: componentType.kotlin.starProjectedType
            }

        val elementRequest = GenerationRequest.from(elementType, "${request.name}[$componentType]")

        for (i in 0 until size) {
            val value = regenerator(elementRequest, context)
            JavaArray.set(array, i, value)
        }

        return array
    }

    private fun validateSafetyLimit(
        targetSize: Int,
        minSize: Int,
        ignoreLimit: Boolean,
    ) {
        val isExplicitIntent = ignoreLimit || (minSize > GLOBAL_LIMIT)
        if (!isExplicitIntent && targetSize > GLOBAL_LIMIT) {
            throw CollectionSizeLimitExceededException(targetSize, GLOBAL_LIMIT)
        }
    }

    private fun isValidSize(
        size: Int,
        ignoreLimit: Boolean,
    ): Boolean = ignoreLimit || size <= GLOBAL_LIMIT

    private fun parseSizeBoundaries(annotation: Size?): Triple<Int, Int, Boolean> {
        if (annotation == null) return Triple(0, Int.MAX_VALUE, false)
        val min = annotation.min.coerceAtLeast(0)
        val max = annotation.max.coerceAtLeast(min)
        val isExplicit = annotation.max != Int.MAX_VALUE
        return Triple(min, max, isExplicit)
    }
}

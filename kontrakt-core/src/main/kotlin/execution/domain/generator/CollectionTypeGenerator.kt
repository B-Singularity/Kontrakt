package execution.domain.generator

import discovery.api.Size
import execution.exception.CollectionSizeLimitExceededException
import kotlin.reflect.KClass

class CollectionTypeGenerator : RecursiveGenerator {

    private companion object {
        private const val DEFAULT_SIZE = 5
        private const val GLOBAL_LIMIT = 1_000
    }

    override fun supports(request: GenerationRequest): Boolean {
        val type = request.type.classifier as? KClass<*> ?: return false
        return Collection::class.java.isAssignableFrom(type.java) ||
                Map::class.java.isAssignableFrom(type.java)
    }

    override fun generator(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Any {
        val sizeAnnotation = request.find<Size>()

        val (min, max, isMaxExplicit) = parseSizeBoundaries(sizeAnnotation)

        val targetSize = when {
            isMaxExplicit -> context.seededRandom.nextInt(min, max + 1)
            else -> maxOf(min, DEFAULT_SIZE)
        }

        validateSafetyLimit(targetSize, min, sizeAnnotation?.ignoreLimit == true)

        return createInstance(request, context, targetSize, regenerator)
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?> {
        val sizeAnnotation = request.find<Size>()
        val (min, max, _) = parseSizeBoundaries(sizeAnnotation)
        val ignoreLimit = sizeAnnotation?.ignoreLimit == true

        return buildList {
            if (isValidSize(min, ignoreLimit)) {
                add(createInstance(request, context, min, regenerator))
            }
            if (max != min && max != Int.MAX_VALUE && isValidSize(max, ignoreLimit)) {
                add(createInstance(request, context, max, regenerator))
            }
        }
    }


    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?> = buildList {
        val sizeAnnotation = request.find<Size>() ?: return@buildList
        val (min, max, _) = parseSizeBoundaries(sizeAnnotation)
        val ignoreLimit = sizeAnnotation.ignoreLimit

        if (min > 0) {
            add(createInstance(request, context, min - 1, regenerator))
        }

        if (max != Int.MAX_VALUE) {
            val invalidSize = max + 1
            if (isValidSize(invalidSize, ignoreLimit)) {
                add(createInstance(request, context, invalidSize, regenerator))
            }
        }
    }

    private fun validateSafetyLimit(targetSize: Int, minSize: Int, ignoreLimit: Boolean) {
        val isExplicitIntent = ignoreLimit || (minSize > GLOBAL_LIMIT)

        if (!isExplicitIntent && targetSize > GLOBAL_LIMIT) {
            throw CollectionSizeLimitExceededException(targetSize, GLOBAL_LIMIT)
        }
    }

    private fun isValidSize(size: Int, ignoreLimit: Boolean): Boolean {
        return ignoreLimit || size <= GLOBAL_LIMIT
    }

    private fun createInstance(
        request: GenerationRequest,
        context: GenerationContext,
        size: Int,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Any {
        val kClass = request.type.classifier as? KClass<*>
            ?: throw IllegalArgumentException("Unresolved classifier for ${request.type}")

        val javaClass = kClass.java

        return when {
            Map::class.java.isAssignableFrom(javaClass) ->
                generateMap(request, context, size, regenerator)

            Set::class.java.isAssignableFrom(javaClass) ->
                generateList(request, context, size, regenerator).toSet()

            java.util.Queue::class.java.isAssignableFrom(javaClass) ||
                    java.util.Deque::class.java.isAssignableFrom(javaClass) ->
                java.util.LinkedList(generateList(request, context, size, regenerator))
            
            else ->
                generateList(request, context, size, regenerator)
        }
    }

    private fun generateList(
        request: GenerationRequest,
        context: GenerationContext,
        size: Int,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?> {
        val elementType = request.type.arguments.firstOrNull()?.type ?: return emptyList()
        val elementRequest = GenerationRequest.from(elementType, "${request.name}[element]")

        return List(size) {
            regenerator(elementRequest, context)
        }
    }

    private fun generateMap(
        request: GenerationRequest,
        context: GenerationContext,
        size: Int,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Map<Any?, Any?> {
        val keyType = request.type.arguments.getOrNull(0)?.type
        val valueType = request.type.arguments.getOrNull(1)?.type

        if (keyType == null || valueType == null) return emptyMap()

        val keyRequest = GenerationRequest.from(keyType, "${request.name}[key]")
        val valueRequest = GenerationRequest.from(valueType, "${request.name}[value]")

        return buildMap(size) {
            repeat(size) {
                put(
                    regenerator(keyRequest, context),
                    regenerator(valueRequest, context)
                )
            }
        }
    }

    private fun parseSizeBoundaries(annotation: Size?): Triple<Int, Int, Boolean> {
        if (annotation == null) return Triple(0, Int.MAX_VALUE, false)

        val min = annotation.min.coerceAtLeast(0)
        val max = annotation.max.coerceAtLeast(min)
        val isExplicit = annotation.max != Int.MAX_VALUE

        return Triple(min, max, isExplicit)
    }
}
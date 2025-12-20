package execution.domain.generator

import discovery.api.NotEmpty
import discovery.api.Size
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class CollectionTypeGenerator : ContainerGenerator{

    override fun supports(request: GenerationRequest): Boolean {
        val type = request.type.classifier as? KClass<*> ?: return false
        return Collection::class.java.isAssignableFrom(type.java) ||
                Map::class.java.isAssignableFrom(type.java)
    }

    override fun generate(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Any? {
        val (minSize, maxSize) = calculateEffectiveSize(request)

        val defaultCeiling = if (minSize > Int.MAX_VALUE - 3) Int.MAX_VALUE else minSize + 3
        val actualMax = if (defaultCeiling > maxSize) maxSize else defaultCeiling

        val size = if (minSize == actualMax) {
            minSize
        } else {
            context.seededRandom.nextInt(minSize, actualMax + 1)
        }

        return createCollection(request, context, size, regenerator)
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): List<Any?> = buildList {
        val type = param.type.classifier as KClass<*>
        val (minSize, maxSize) = calculateEffectiveSize(param)

        if (List::class.java.isAssignableFrom(type.java) || Collection::class.java.isAssignableFrom(type.java) || Iterable::class.java.isAssignableFrom(
                type.java
            )
        ) {
            add(List(minSize) { "item" })
            if (maxSize < 100 && maxSize != minSize) add(List(maxSize) { "item" })
        }
        if (Map::class.java.isAssignableFrom(type.java)) {
            add((1..minSize).associate { "k$it" to "v$it" })
        }
    }


    override fun generateInvalid(param: KParameter): List<Any?> = emptyList()

    private fun createCollection(
        request: GenerationRequest,
        context: GenerationContext,
        size: Int,
        regenerator: (GenerationRequest, GenerationContext) -> Any?
    ): Any? {
        val kClass = request.type.classifier as KClass<*>

        if (Map::class.java.isAssignableFrom(kClass.java)) {
            val keyType = request.type.arguments.getOrNull(0)?.type
            val valueType = request.type.arguments.getOrNull(1)?.type

            if (keyType == null || valueType == null) return emptyMap<Any, Any>()

            val keyRequest = GenerationRequest.from(keyType, name = "key")
            val valueRequest = GenerationRequest.from(valueType, name = "value")

            return (0 until size).associate {
                val
            }
        }
    }

    private fun calculateEffectiveSize(request: GenerationRequest): Pair<Int, Int> {
        var minSize = 0
        var maxSize = Int.MAX_VALUE

        request.find<Size>()?.let {
            minSize = max(minSize, it.min)
            maxSize = min(maxSize, it.max)
        }

        if (request.has<NotEmpty>()) {
            minSize = max(minSize, 1)
        }

        return minSize to maxSize
    }
}
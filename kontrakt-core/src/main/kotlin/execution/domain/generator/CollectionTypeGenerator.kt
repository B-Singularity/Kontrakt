package execution.domain.generator

import discovery.api.NotEmpty
import discovery.api.Size
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class CollectionTypeGenerator : TypeGenerator {
    override fun supports(param: KParameter): Boolean {
        val type = param.type.classifier as? KClass<*> ?: return false

        return Collection::class.java.isAssignableFrom(type.java) ||
                Map::class.java.isAssignableFrom(type.java)
    }

    override fun generateValidBoundaries(param: KParameter): List<Any?> = buildList {
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

    override fun generate(param: KParameter): Any? = null // Delegated to recursion in FixtureGenerator

    override fun generateInvalid(param: KParameter): List<Any?> = emptyList()

    private fun calculateEffectiveSize(param: KParameter): Pair<Int, Int> {
        var minSize = 0
        var maxSize = Int.MAX_VALUE
        param.findAnnotation<Size>()?.let {
            minSize = max(minSize, it.min)
            maxSize = min(maxSize, it.max)
        }
        if (param.findAnnotation<NotEmpty>() != null) minSize = max(minSize, 1)
        return minSize to maxSize
    }
}
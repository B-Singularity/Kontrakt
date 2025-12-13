package execution.domain.generator

import discovery.api.AssertFalse
import discovery.api.AssertTrue
import kotlin.random.Random
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class BooleanTypeGenerator : TypeGenerator {
    override fun supports(param: KParameter): Boolean = param.type.classifier == Boolean::class

    override fun generateValidBoundaries(param: KParameter): List<Any?> = buildList {
        if (param.has<AssertTrue>()) add(true)
        if (param.has<AssertFalse>()) add(false)
        if (isEmpty()) {
            add(true); add(false)
        }
    }

    override fun generate(param: KParameter): Any? = when {
        param.has<AssertTrue>() -> true
        param.has<AssertFalse>() -> false
        else -> Random.nextBoolean()
    }

    override fun generateInvalid(param: KParameter): List<Any?> = buildList {
        if (param.has<AssertTrue>()) add(false)
        if (param.has<AssertFalse>()) add(true)
    }

    private inline fun <reified T : Annotation> KParameter.has(): Boolean = findAnnotation<T>() != null
}
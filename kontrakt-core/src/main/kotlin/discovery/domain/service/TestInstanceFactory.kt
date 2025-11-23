package discovery.domain.service

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata.MockingStrategy
import execution.domain.entity.EphemeralTestContext
import execution.spi.MockingEngine
import kotlin.reflect.KClass

class TestInstanceFactory(
    private val mockingEngine: MockingEngine
) {
    fun create(spec: TestSpecification): EphemeralTestContext {
        val context = EphemeralTestContext(spec, mockingEngine)
        val targetInstance = resolve(spec.target.kClass, context, mutableSetOf())
        context.registerTarget(targetInstance)
        return context
    }

    private fun resolve(
        type: KClass<*>,
        context: EphemeralTestContext,
        dependencyPath: MutableSet<KClass<*>>
    ): Any {
        context.getDependency(type)?.let { return it }

        if (type in dependencyPath) {
            throw IllegalStateException("Circular dependency detected: ${type.simpleName}")
        }

        dependencyPath.add(type)
        try {
            if (isValueType(type)) return createDefaultValue(type)

            val definedDependency = context.specification.requiredDependencies.find { it.type == type }
            val strategy = definedDependency?.strategy ?: MockingStrategy.StatelessMock

            val instance = when (strategy) {
                is MockingStrategy.StatefulFake -> mockingEngine.createFake(type)
                is MockingStrategy.StatelessMock -> mockingEngine.createMock(type)

                else -> mockingEngine.createMock(type)
            }

            context.registerDependency(type, instance)
            return instance
        } finally {
            dependencyPath.remove(type)
        }
    }

    private fun isValueType(type: KClass<*>): Boolean {
        return type == String::class || type == Int::class || type == Boolean::class || type == Long::class
    }

    private fun createDefaultValue(type: KClass<*>): Any {
        return when (type) {
            String::class -> ""
            Int::class -> 0
            Long::class -> 0L
            Boolean::class -> false
            else -> 0
        }
    }
}



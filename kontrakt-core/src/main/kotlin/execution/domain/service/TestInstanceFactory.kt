package execution.domain.service

import common.reflection.unwrapped
import discovery.api.KontraktConfigurationException
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import execution.domain.entity.EphemeralTestContext
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import kotlin.reflect.KClass

class TestInstanceFactory(
    private val mockingEngine: MockingEngine,
    private val scenarioControl: ScenarioControl
) {
    fun create(spec: TestSpecification): EphemeralTestContext {
        val context = EphemeralTestContext(spec, mockingEngine, scenarioControl)
        try {
            val targetInstance = resolve(spec.target.kClass, context, mutableSetOf())
            context.registerTarget(targetInstance)
        } catch (e: Throwable) {
            val cause = e.unwrapped
            throw KontraktConfigurationException(
                "Failed to create test target '${spec.target.displayName}': ${cause.message}",
                cause
            )
        }
        return context
    }

    private fun resolve(
        type: KClass<*>,
        context: EphemeralTestContext,
        dependencyPath: MutableSet<KClass<*>>
    ): Any {
        context.getDependency(type)?.let { return it }

        if (type in dependencyPath) {
            throw KontraktConfigurationException(
                "Circular dependency detected: ${dependencyPath.joinToString(" -> ") { it.simpleName.toString() }} -> ${type.simpleName}"
            )
        }

        dependencyPath.add(type)
        try {
            if (isValueType(type)) return createDefaultValue(type)

            val strategy = context.specification.requiredDependencies.find { it.type == type }?.strategy

            val instance = if (strategy != null) {
                when (strategy) {
                    is DependencyMetadata.MockingStrategy.StatefulFake -> mockingEngine.createFake(type)
                    is DependencyMetadata.MockingStrategy.StatelessMock -> mockingEngine.createMock(type)
                    is DependencyMetadata.MockingStrategy.Environment -> mockingEngine.createMock(type)
                    is DependencyMetadata.MockingStrategy.Real -> createByConstructor(type, context, dependencyPath)
                }
            } else {
                createByConstructor(type, context, dependencyPath)
            }

            context.registerDependency(type, instance)
            return instance

        } finally {
            dependencyPath.remove(type)
        }
    }

    private fun createByConstructor(
        type: KClass<*>,
        context: EphemeralTestContext,
        path: MutableSet<KClass<*>>
    ): Any {
        val constructor = type.constructors.firstOrNull()
            ?: return mockingEngine.createMock(type)

        try {
            val args = constructor.parameters.map { param ->
                val paramType = param.type.classifier as KClass<*>
                resolve(paramType, context, path)
            }.toTypedArray()

            return constructor.call(*args)

        } catch (e: Throwable) {
            val cause = e.unwrapped
            throw KontraktConfigurationException(
                "Failed to instantiate class [${type.qualifiedName}]: ${cause.message}",
                cause
            )
        }
    }

    private fun isValueType(type: KClass<*>): Boolean {
        return type == String::class || type == Int::class || type == Boolean::class || type == Long::class || type == Double::class
    }

    private fun createDefaultValue(type: KClass<*>): Any {
        return when (type) {
            String::class -> ""
            Int::class -> 0
            Long::class -> 0L
            Boolean::class -> false
            Double::class -> 0.0
            else -> 0
        }
    }
}
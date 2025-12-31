package execution.domain.service

import common.reflection.unwrapped
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import exception.KontraktConfigurationException
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.generator.GenerationRequest
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import java.time.Clock
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

class TestInstanceFactory(
    private val mockingEngine: MockingEngine,
    private val scenarioControl: ScenarioControl,
) {
    fun create(spec: TestSpecification): EphemeralTestContext {
        val context = EphemeralTestContext(spec, mockingEngine, scenarioControl)
        try {
            val seed = spec.seed ?: System.currentTimeMillis()
            val clock = Clock.systemUTC()

            val fixtureGenerator = FixtureGenerator(mockingEngine, clock, seed)

            val generationContext =
                GenerationContext(
                    seededRandom = Random(seed),
                    clock = clock,
                )

            val targetInstance = resolve(spec.target.kClass, context, generationContext, fixtureGenerator)
            context.registerTarget(targetInstance)
        } catch (e: Throwable) {
            val cause = e.unwrapped
            throw KontraktConfigurationException(
                "Failed to create test target '${spec.target.displayName}': ${cause.message}",
                cause,
            )
        }
        return context
    }

    private fun resolve(
        type: KClass<*>,
        context: EphemeralTestContext,
        generationContext: GenerationContext,
        fixtureGenerator: FixtureGenerator,
    ): Any {
        context.getDependency(type)?.let { return it }

        if (type in generationContext.history) {
            throw KontraktConfigurationException(
                "Circular dependency detected: ${generationContext.history.joinToString(
                    " -> ",
                ) { it.simpleName.toString() }} -> ${type.simpleName}",
            )
        }

        val nextGenerationContext =
            generationContext.copy(
                history = generationContext.history + type,
            )

        try {
            val explicitStrategy =
                context.specification.requiredDependencies
                    .find { it.type == type }
                    ?.strategy

            if (explicitStrategy == null) {
                if (isBasicValueType(type)) {
                    runCatching {
                        val request =
                            GenerationRequest.from(
                                type.starProjectedType,
                                name = type.simpleName ?: "dependency",
                            )
                        fixtureGenerator.generate(request, generationContext)!!
                    }.getOrNull()?.let { return it }
                }

                return createByConstructor(type, context, nextGenerationContext, fixtureGenerator)
                    .also { context.registerDependency(type, it) }
            }

            val instance =
                when (explicitStrategy) {
                    is DependencyMetadata.MockingStrategy.StatefulFake -> mockingEngine.createFake(type)
                    is DependencyMetadata.MockingStrategy.StatelessMock -> mockingEngine.createMock(type)
                    is DependencyMetadata.MockingStrategy.Environment -> mockingEngine.createMock(type)
                    is DependencyMetadata.MockingStrategy.Real -> {
                        createByConstructor(
                            explicitStrategy.implementation,
                            context,
                            nextGenerationContext,
                            fixtureGenerator,
                        )
                    }
                }.also { context.registerDependency(type, it) }

            return instance
        } catch (e: Throwable) {
            throw e
        }
    }

    private fun createByConstructor(
        type: KClass<*>,
        context: EphemeralTestContext,
        generationContext: GenerationContext,
        fixtureGenerator: FixtureGenerator,
    ): Any {
        val constructor =
            type.constructors.firstOrNull()
                ?: return mockingEngine.createMock(type)

        try {
            val args =
                constructor.parameters
                    .map { param ->
                        val paramType = param.type.classifier as KClass<*>

                        if (isBasicValueType(paramType)) {
                            fixtureGenerator.generate(param, generationContext)
                                ?: resolve(paramType, context, generationContext, fixtureGenerator)
                        } else {
                            resolve(paramType, context, generationContext, fixtureGenerator)
                        }
                    }.toTypedArray()

            return constructor.call(*args)
        } catch (e: Throwable) {
            val cause = e.unwrapped
            throw KontraktConfigurationException(
                "Failed to instantiate class [${type.qualifiedName}]: ${cause.message}",
                cause,
            )
        }
    }

    private fun isBasicValueType(type: KClass<*>): Boolean =
        type == String::class ||
            type == Int::class ||
            type == Long::class ||
            type == Double::class ||
            type == Boolean::class ||
            type == List::class ||
            type == Map::class ||
            type == Set::class
}

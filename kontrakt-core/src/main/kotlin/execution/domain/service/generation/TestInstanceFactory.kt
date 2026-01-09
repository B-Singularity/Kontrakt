package execution.domain.service.generation

import common.util.unwrapped
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
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

/**
 * [Domain Service] Test Instance Factory.
 *
 * Responsible for constructing the **Test Target** (SUT) and its entire dependency graph.
 * It combines:
 * 1. **Dependency Injection**: Resolves dependencies based on [TestSpecification] (Mock, Fake, Real).
 * 2. **Fixture Generation**: Generates primitive values and value objects via [FixtureGenerator].
 * 3. **Recursion Safety**: Detects and prevents circular dependencies during instantiation.
 */
class TestInstanceFactory(
    private val mockingEngine: MockingEngine,
    private val scenarioControl: ScenarioControl,
    private val clock: Clock = Clock.systemUTC()
) {
    /**
     * Creates a fully initialized test context containing the Target Instance.
     *
     * @param spec The test specification defining the target and mocking strategies.
     * @return A context holding the instantiated target and its dependencies.
     * @throws KontraktConfigurationException If instantiation fails due to config or runtime errors.
     */
    fun create(spec: TestSpecification): EphemeralTestContext {
        val context = EphemeralTestContext(spec, mockingEngine, scenarioControl)
        try {
            val seed = spec.seed ?: System.currentTimeMillis()

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


    /**
     * Recursively resolves an instance of the given [type].
     *
     * Resolution Strategy:
     * 1. **Cache**: Check if already resolved in the context.
     * 2. **Cycle Check**: Ensure we aren't in an infinite recursion loop.
     * 3. **Strategy Lookup**: Check if the user defined a specific strategy (Mock/Fake/Real).
     * 4. **Generator**: Try to generate simple values (String, Int, Data Classes).
     * 5. **Constructor**: Attempt to instantiate via constructor injection.
     * 6. **Fallback**: Create a Mock if no other way exists (e.g., Interfaces).
     */
    private fun resolve(
        type: KClass<*>,
        context: EphemeralTestContext,
        generationContext: GenerationContext,
        fixtureGenerator: FixtureGenerator,
    ): Any {
        context.getDependency(type)?.let { return it }

        if (type in generationContext.history) {
            throw KontraktConfigurationException(
                "Circular dependency detected: ${
                    generationContext.history.joinToString(
                        " -> ",
                    ) { it.simpleName.toString() }
                } -> ${type.simpleName}",
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
        val constructor = type.primaryConstructor ?: type.constructors.firstOrNull()

        if (constructor == null) {
            return mockingEngine.createMock(type)
        }

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
                type == Set::class ||
                type.isData
}

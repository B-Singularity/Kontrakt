package execution.domain.service.generation

import common.util.unwrapped
import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.domain.entity.EphemeralTestContext
import execution.domain.trace.InMemoryScenarioTrace
import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.context.generation.GenerationRequest
import execution.port.outgoing.MockingContext
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioControl
import java.time.Clock
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaMethod

/**
 * [Domain Service] Test Instance Factory.
 *
 * Responsible for constructing the **Test Target** (SUT) and its entire dependency graph.
 * It combines:
 * 1. **Dependency Injection**: Resolves dependencies based on [TestSpecification] (Mock, Fake, Real).
 * 2. **Fixture Generation**: Generates primitive values and value objects via [FixtureGenerator].
 * 3. **Recursion Safety**: Detects and prevents circular dependencies during instantiation.
 * 4. **Lifecycle Management**: Binds the generation to the mocking engine context.
 */
class TestInstanceFactory(
    private val mockingEngine: MockingEngine,
    private val scenarioControl: ScenarioControl,
) {
    /**
     * Creates a fully initialized test context containing the Target Instance.
     *
     * @param spec The test specification defining the target and mocking strategies.
     * @param clock The fixed clock to ensure temporal determinism across the entire test lifecycle.
     * @return A context holding the instantiated target and its dependencies.
     * @throws KontraktConfigurationException If instantiation fails due to config or runtime errors.
     */
    fun create(
        spec: TestSpecification,
        clock: Clock,
    ): EphemeralTestContext {
        val currentTraceId = UUID.randomUUID().toString()

        val trace = InMemoryScenarioTrace(runId = currentTraceId)

        val context = EphemeralTestContext(spec, mockingEngine, scenarioControl, trace = trace)

        try {
            val seed = spec.seed ?: System.currentTimeMillis()

            val fixtureGenerator = FixtureGenerator(mockingEngine, clock, trace, seed)

            val mockingContext = MockingContext(fixtureGenerator, trace)

            val generationContext =
                GenerationContext(
                    seededRandom = Random(seed),
                    clock = clock,
                )

            val targetInstance = resolve(spec.target.kClass, context, generationContext, mockingContext)
            context.registerTarget(targetInstance)

            // 5. Resolve Entry Point
            val targetMethod = resolveTargetMethod(spec, targetInstance::class)
            context.targetMethod = targetMethod.javaMethod
                ?: throw KontraktInternalException(
                    "Reflection failure: Could not resolve Java method for Kotlin function '${targetMethod.name}' " +
                        "in class '${spec.target.displayName}'.",
                )
        } catch (e: KontraktConfigurationException) {
            throw e
        } catch (e: KontraktInternalException) {
            throw e
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
     * Identifies the primary method to be executed based on the TestMode.
     * This helper was missing in the previous snippet.
     */
    private fun resolveTargetMethod(
        spec: TestSpecification,
        type: KClass<*>,
    ): KFunction<*> =
        when (val mode = spec.modes.first()) {
            is TestSpecification.TestMode.UserScenario -> {
                // Try to find the first @Test method to set as the initial context
                type.functions.find { it.findAnnotation<Test>() != null }
                    ?: type.functions.firstOrNull()
                    ?: throw KontraktConfigurationException("No executable methods found in '${type.simpleName}' for UserScenario.")
            }

            is TestSpecification.TestMode.ContractAuto -> {
                type.functions.firstOrNull()
                    ?: throw KontraktConfigurationException("No methods found in implementation '${type.simpleName}'.")
            }

            is TestSpecification.TestMode.DataCompliance -> {
                // For Data Classes, use constructor or toString as a safe placeholder
                type.primaryConstructor
                    ?: type.functions.find { it.name == "toString" }
                    ?: throw KontraktConfigurationException("Data class '${type.simpleName}' has no accessible members.")
            }
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
        mockingContext: MockingContext,
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
                        mockingContext.generator.generate(request, generationContext)!!
                    }.getOrNull()?.let { return it }
                }

                return createByConstructor(type, context, nextGenerationContext, mockingContext)
                    .also { context.registerDependency(type, it) }
            }

            val instance =
                when (explicitStrategy) {
                    is DependencyMetadata.MockingStrategy.StatefulFake ->
                        mockingEngine.createFake(
                            type,
                            mockingContext,
                        )

                    is DependencyMetadata.MockingStrategy.StatelessMock ->
                        mockingEngine.createMock(
                            type,
                            mockingContext,
                        )

                    is DependencyMetadata.MockingStrategy.Environment ->
                        mockingEngine.createMock(
                            type,
                            mockingContext,
                        )

                    is DependencyMetadata.MockingStrategy.Real -> {
                        createByConstructor(
                            explicitStrategy.implementation,
                            context,
                            nextGenerationContext,
                            mockingContext,
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
        mockingContext: MockingContext,
    ): Any {
        val constructor = type.primaryConstructor ?: type.constructors.firstOrNull()

        if (constructor == null) {
            return mockingEngine.createMock(type, mockingContext)
        }

        try {
            val args =
                constructor.parameters
                    .map { param ->
                        val paramType = param.type.classifier as KClass<*>

                        if (isBasicValueType(paramType)) {
                            mockingContext.generator.generate(param, generationContext)
                                ?: resolve(paramType, context, generationContext, mockingContext)
                        } else {
                            resolve(paramType, context, generationContext, mockingContext)
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

package execution.domain.service.orchestration

import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.service.validation.ContractValidator
import execution.domain.vo.AssertionRecord
import execution.domain.vo.SourceLocation
import execution.domain.vo.StandardAssertion
import execution.domain.vo.SystemErrorRule
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Method
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction


/**
 * [Core Service] Default Scenario Executor.
 *
 * This component orchestrates the **reflection-based invocation** of test scenarios.
 * It handles:
 * 1. **Dependency Generation**: Creates fixtures via [FixtureGenerator].
 * 2. **Mode Switching**: Supports 'UserScenario' (@Test) and 'ContractAuto' (Fuzzing).
 * 3. **Validation**: Enforces contracts via [ContractValidator].
 *
 * This executor is now **Exception Transparent**. It **DOES NOT** catch exceptions.
 * - **Success**: Returns [AssertionStatus.PASSED] records.
 * - **Failure**: Propagates exceptions to the [execution.domain.interceptor.ResultResolverInterceptor]
 * for centralized error handling and source coordinate mining.
 */
class DefaultScenarioExecutor(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val fixtureFactory: (MockingEngine, Clock) -> FixtureGenerator =
        { engine, clock -> FixtureGenerator(engine, clock) },
    private val validatorFactory: (Clock) -> ContractValidator =
        { clock -> ContractValidator(clock) },
) : TestScenarioExecutor {

    private val logger = KotlinLogging.logger {}

    override fun executeScenarios(context: EphemeralTestContext): List<AssertionRecord> {
        val currentInstant = Instant.now(clock)
        val fixedClock = Clock.fixed(currentInstant, ZoneId.systemDefault())
        val seed = context.specification.seed ?: System.currentTimeMillis()

        val fixtureGenerator = fixtureFactory(context.mockingEngine, fixedClock)
        val contractValidator = validatorFactory(fixedClock)

        val generationContext =
            GenerationContext(
                seededRandom = Random(seed),
                clock = fixedClock,
            )

        val records = mutableListOf<AssertionRecord>()

        if (context.specification.modes.contains(TestSpecification.TestMode.UserScenario)) {
            records.addAll(
                executeUserTestMethods(context, fixtureGenerator, generationContext),
            )
        }

        val contractModes = context.specification.modes.filterIsInstance<TestSpecification.TestMode.ContractAuto>()
        contractModes.forEach { mode ->
            records.addAll(
                executeContractFuzzing(
                    context,
                    mode.contractInterface.java,
                    fixtureGenerator,
                    contractValidator,
                    generationContext,
                ),
            )
        }

        return records
    }

    private fun executeUserTestMethods(
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext,
    ): List<AssertionRecord> {
        val testInstance = context.getTestTarget()
        val kClass = testInstance::class

        val testFunctions = kClass.functions.filter { it.findAnnotation<Test>() != null }

        if (testFunctions.isEmpty()) {
            logger.warn { "UserScenario mode active but no methods annotated with @Test found in ${kClass.simpleName}" }
            return emptyList()
        }

        return testFunctions.map { kFunc ->
            context.targetMethod = kFunc.javaMethod
                ?: throw KontraktInternalException(
                    "Failed to resolve Java method for Kotlin function: '${kFunc.name}'. " +
                            "This indicates a reflection issue with the target class '${kClass.simpleName}'."
                )

            val args = createArguments(kFunc, context, fixtureGenerator, generationContext)
            kFunc.callBy(args)

            AssertionRecord(
                status = AssertionStatus.PASSED,
                rule = StandardAssertion,
                message = "Test '${kFunc.name}' passed",
                expected = "Success",
                actual = "Success",
                location = SourceLocation.NotCaptured
            )
        }
    }


    private fun executeContractFuzzing(
        context: EphemeralTestContext,
        contractClass: Class<*>,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext,
    ): List<AssertionRecord> {
        val testTargetInstance = context.getTestTarget()
        val implementationKClass = testTargetInstance::class

        return contractClass.methods
            .filter { !it.isBridge && !it.isSynthetic }
            .map { contractMethod ->
                val implementationFunction =
                    implementationKClass.functions.find { kFunc ->
                        if (kFunc.name != contractMethod.name) return@find false
                        val kFuncAsJava = kFunc.javaMethod ?: return@find false
                        kFuncAsJava.name == contractMethod.name &&
                                kFuncAsJava.parameterTypes.contentEquals(contractMethod.parameterTypes)
                    } ?: return@map AssertionRecord(
                        status = AssertionStatus.FAILED,
                        rule = SystemErrorRule("MethodNotFound"),
                        message = "Method '${contractMethod.name}' not found in implementation",
                        expected = "Method Implementation",
                        actual = "Missing",
                        location = SourceLocation.NotCaptured,
                    )

                val contractKFunc =
                    contractMethod.kotlinFunction
                        ?: return@map AssertionRecord(
                            status = AssertionStatus.FAILED,
                            rule = SystemErrorRule("ReflectionError"),
                            message = "Could not resolve Kotlin function for '${contractMethod.name}'",
                            expected = "KFunction",
                            actual = "null",
                            location = SourceLocation.NotCaptured
                        )

                executeMethod(
                    contractMethod,
                    implementationFunction,
                    contractKFunc,
                    context,
                    fixtureGenerator,
                    contractValidator,
                    generationContext,
                )
            }
    }

    private fun executeMethod(
        contractMethod: Method,
        implFunc: KFunction<*>,
        contractKFunc: KFunction<*>,
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext,
    ): AssertionRecord {
        val args = createArguments(implFunc, context, fixtureGenerator, generationContext)

        val result = implFunc.callBy(args)

        contractValidator.validate(contractKFunc, result)

        return AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = StandardAssertion,
            message = "Method '${contractMethod.name}' executed successfully and satisfied contract.",
            expected = "Contract Compliance",
            actual = "Compliant",
            location = SourceLocation.NotCaptured
        )
    }


    private fun createArguments(
        function: KFunction<*>,
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext,
    ): Map<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        function.parameters.forEach { param ->
            if (param.kind == KParameter.Kind.INSTANCE) {
                arguments[param] = context.getTestTarget()
                return@forEach
            }

            if (param.isOptional) return@forEach
            arguments[param] = fixtureGenerator.generate(param, generationContext)
        }
        return arguments
    }
}

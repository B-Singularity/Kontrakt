package execution.domain.service

import common.reflection.unwrapped
import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import exception.ContractViolationException
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.vo.AssertionRecord
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

        val fixtureGenerator = fixtureFactory(context.mockingEngine, fixedClock)
        val contractValidator = validatorFactory(fixedClock)

        val seed = context.specification.seed ?: System.currentTimeMillis()
        val generationContext = GenerationContext(
            seededRandom = Random(seed),
            clock = fixedClock
        )

        val records = mutableListOf<AssertionRecord>()

        if (context.specification.modes.contains(TestSpecification.TestMode.UserScenario)) {
            records.addAll(
                executeUserTestMethods(context, fixtureGenerator, generationContext)
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
                    generationContext
                )
            )
        }

        return records
    }

    private fun executeUserTestMethods(
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext
    ): List<AssertionRecord> {
        val testInstance = context.getTestTarget()
        val kClass = testInstance::class

        val testFunctions = kClass.functions.filter { it.findAnnotation<Test>() != null }

        if (testFunctions.isEmpty()) {
            logger.warn { "UserScenario mode active but no methods annotated with @Test found in ${kClass.simpleName}" }
            return emptyList()
        }

        return testFunctions.map { kFunc ->
            try {
                val args = createArguments(kFunc, context, fixtureGenerator, generationContext)

                kFunc.callBy(args)

                AssertionRecord(
                    status = AssertionStatus.PASSED,
                    message = "Test '${kFunc.name}' passed",
                    expected = "Success",
                    actual = "Success"
                )
            } catch (e: Throwable) {
                val cause = e.unwrapped
                if (cause is AssertionError) {
                    AssertionRecord(
                        status = AssertionStatus.FAILED,
                        message = "Test '${kFunc.name}' failed: ${cause.message}",
                        expected = "Assertion Pass",
                        actual = "Assertion Fail"
                    )
                } else {
                    logger.error(cause) { "Test '${kFunc.name}' threw unexpected exception" }
                    AssertionRecord(
                        status = AssertionStatus.FAILED,
                        message = "Test '${kFunc.name}' error: ${cause.message}",
                        expected = "Success",
                        actual = cause.javaClass.simpleName
                    )
                }
            }
        }
    }

    private fun executeContractFuzzing(
        context: EphemeralTestContext,
        contractClass: Class<*>,
        fixtureGenerator: FixtureGenerator,
        contractValidator: ContractValidator,
        generationContext: GenerationContext
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
                        AssertionStatus.FAILED,
                        "Method '${contractMethod.name}' not found in implementation",
                        null,
                        null,
                    )

                val contractKFunc =
                    contractMethod.kotlinFunction
                        ?: return@map AssertionRecord(
                            AssertionStatus.FAILED,
                            "Reflection failed: Could not resolve Kotlin function for '${contractMethod.name}'",
                            "KFunction",
                            "null",
                        )

                executeMethod(
                    contractMethod,
                    implementationFunction,
                    contractKFunc,
                    context,
                    fixtureGenerator,
                    contractValidator,
                    generationContext
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
        generationContext: GenerationContext
    ): AssertionRecord =
        try {
            val args = createArguments(implFunc, context, fixtureGenerator, generationContext)

            val result = implFunc.callBy(args)

            contractValidator.validate(contractKFunc, result)

            AssertionRecord(
                status = AssertionStatus.PASSED,
                message = "Method '${contractMethod.name}' executed successfully and satisfied contract.",
                expected = "No Exception & Valid Return",
                actual = "Success",
            )
        } catch (e: Throwable) {
            val rootCause = e.unwrapped

            if (rootCause is ContractViolationException) {
                logger.error { "Contract Violation in ${contractMethod.name}: ${rootCause.message}" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    message = "Contract Violation in method '${contractMethod.name}': ${rootCause.message}",
                    expected = "Constraint Compliance",
                    actual = "Violation",
                )
            } else {
                logger.error(rootCause) { "Unexpected Exception in ${contractMethod.name}" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    message = "Method '${contractMethod.name}' threw an exception: ${rootCause.message}",
                    expected = "No Exception",
                    actual = rootCause.javaClass.simpleName,
                )
            }
        }

    private fun createArguments(
        function: KFunction<*>,
        context: EphemeralTestContext,
        fixtureGenerator: FixtureGenerator,
        generationContext: GenerationContext
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
package execution.domain.service.orchestration

import common.util.unwrapped
import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import exception.ContractViolationException
import exception.KontraktException
import exception.KontraktInternalException
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.generator.GenerationContext
import execution.domain.service.generation.FixtureGenerator
import execution.domain.service.validation.ContractValidator
import execution.domain.vo.AssertionRecord
import execution.domain.vo.SystemErrorRule
import execution.domain.vo.UserAssertionRule
import execution.domain.vo.UserExceptionRule
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

            try {
                val args = createArguments(kFunc, context, fixtureGenerator, generationContext)

                kFunc.callBy(args)

                AssertionRecord(
                    status = AssertionStatus.PASSED,
                    rule = UserAssertionRule,
                    message = "Test '${kFunc.name}' passed",
                    expected = "Success",
                    actual = "Success",
                )
            } catch (e: Throwable) {
                val cause = e.unwrapped
                handleUserTestFailure(kFunc, cause)
            }
        }
    }


    private fun handleUserTestFailure(kFunc: KFunction<*>, cause: Throwable): AssertionRecord {
        return when (cause) {
            is AssertionError -> {
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = UserAssertionRule,
                    message = "Test '${kFunc.name}' failed: ${cause.message}",
                    expected = "Assertion Pass",
                    actual = "Assertion Fail",
                )
            }

            is KontraktException -> {
                logger.error(cause) { "Framework error in test '${kFunc.name}'" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = SystemErrorRule(cause.javaClass.simpleName),
                    message = "Framework Error: ${cause.message}",
                    expected = "Normal Execution",
                    actual = "Framework Crash",
                )
            }

            else -> {
                logger.error(cause) { "User code crashed in test '${kFunc.name}'" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = UserExceptionRule(cause.javaClass.simpleName),
                    message = "Unexpected exception in user code: ${cause.message}",
                    expected = "Normal Execution",
                    actual = cause.javaClass.simpleName,
                )
            }
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
                        actual = "Missing"
                    )

                val contractKFunc =
                    contractMethod.kotlinFunction
                        ?: return@map AssertionRecord(
                            status = AssertionStatus.FAILED,
                            rule = SystemErrorRule("ReflectionError"),
                            message = "Could not resolve Kotlin function for '${contractMethod.name}'",
                            expected = "KFunction",
                            actual = "null"
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
    ): AssertionRecord =
        try {
            val args = createArguments(implFunc, context, fixtureGenerator, generationContext)

            val result = implFunc.callBy(args)

            contractValidator.validate(contractKFunc, result)

            AssertionRecord(
                status = AssertionStatus.PASSED,
                rule = UserAssertionRule,
                message = "Method '${contractMethod.name}' executed successfully and satisfied contract.",
                expected = "No Exception & Valid Return",
                actual = "Success",
            )
        } catch (e: Throwable) {
            val cause = e.unwrapped
            handleExecutionFailure(contractMethod.name, cause)
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

    /**
     * Handles exceptions thrown during test execution and maps them to the appropriate [AssertionRule].
     */
    private fun handleExecutionFailure(methodName: String, cause: Throwable): AssertionRecord {
        return when (cause) {
            // 1. [Failure] Contract Violation (Specific Rule from Validator)
            is ContractViolationException -> {
                logger.warn { "Contract Violation in $methodName: ${cause.message}" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = cause.rule, // Use the specific rule (e.g., NotNull, Size)
                    message = "Contract Violation in method '$methodName': ${cause.message}",
                    expected = "Constraint Compliance",
                    actual = "Violation",
                )
            }

            // 2. [Failure] Simple Assertion Failed (User Logic Error)
            is AssertionError -> {
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = UserAssertionRule,
                    message = "Test '$methodName' failed: ${cause.message}",
                    expected = "Assertion Pass",
                    actual = "Assertion Fail",
                )
            }

            // 3. [System Error] Framework Internal Error (Not User's Fault)
            is KontraktException -> {
                logger.error(cause) { "Framework error in test '$methodName'" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = SystemErrorRule(cause.javaClass.simpleName),
                    message = "Framework Error: ${cause.message}",
                    expected = "Normal Execution",
                    actual = "Framework Crash",
                )
            }

            // 4. [User Exception] Unexpected Runtime Exception in User Code (NPE, OOM, etc.)
            else -> {
                logger.error(cause) { "User code crashed in test '$methodName'" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    rule = UserExceptionRule(cause.javaClass.simpleName),
                    message = "Unexpected exception in user code: ${cause.message}",
                    expected = "Normal Execution",
                    actual = cause.javaClass.simpleName,
                )
            }
        }
    }
}

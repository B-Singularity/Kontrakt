package execution.domain.service

import common.reflection.unwrapped
import exception.ContractViolationException
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.AssertionRecord
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Method
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
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

        val testTargetInstance = context.getTestTarget()
        val specification = context.specification
        val targetClass = specification.target.kClass.java

        val contractInterface =
            targetClass.interfaces.firstOrNull()
                ?: return emptyList()

        val implementationKClass = testTargetInstance::class

        return contractInterface.methods
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
                        "Method '${contractMethod.name}' not found",
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
    ): AssertionRecord =
        try {
            val args = createArguments(implFunc, context, fixtureGenerator)

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
    ): Map<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        function.parameters.forEach { param ->
            if (param.kind == KParameter.Kind.INSTANCE) {
                arguments[param] = context.getTestTarget()
                return@forEach
            }

            if (param.isOptional) return@forEach
            arguments[param] = fixtureGenerator.generate(param)
        }
        return arguments
    }
}

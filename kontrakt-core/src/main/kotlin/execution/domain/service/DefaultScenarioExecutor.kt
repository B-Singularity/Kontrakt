package execution.domain.service

import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.util.ExceptionHelper
import execution.domain.vo.AssertionRecord
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

class DefaultScenarioExecutor : TestScenarioExecutor {

    private val logger = KotlinLogging.logger {}
    private lateinit var fixtureGenerator: FixtureGenerator
    private val contractValidator = ContractValidator()

    override fun executeScenarios(context: EphemeralTestContext): List<AssertionRecord> {
        val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

        this.fixtureGenerator = FixtureGenerator(context.mockingEngine)

        val testTargetInstance = context.getTestTarget()
        val specification = context.specification
        val targetClass = specification.target.kClass.java

        val contractInterface = targetClass.interfaces.firstOrNull()
            ?: return emptyList()

        val implementationKClass = testTargetInstance::class

        return contractInterface.methods
            .filter { !it.isBridge && !it.isSynthetic }
            .map { contractMethod ->
                val implementationFunction = implementationKClass.functions.find { kFunc ->
                    if (kFunc.name != contractMethod.name) return@find false

                    val kFuncAsJava = kFunc.javaMethod ?: return@find false

                    kFuncAsJava.name == contractMethod.name &&
                            kFuncAsJava.parameterTypes.contentEquals(contractMethod.parameterTypes)
                } ?: return@map AssertionRecord(
                    AssertionStatus.FAILED,
                    "Method '${contractMethod.name}' not found",
                    null,
                    null
                )

                val contractKFunc = contractMethod.kotlinFunction
                    ?: return@map AssertionRecord(
                        AssertionStatus.FAILED,
                        "Reflection failed: Could not resolve Kotlin function for '${contractMethod.name}'",
                        "KFunction",
                        "null"
                    )

                executeMethod(contractMethod, implementationFunction, contractKFunc, context)
            }
    }

    private fun executeMethod(
        contractMethod: Method,
        implFunc: KFunction<*>,
        contractKFunc: KFunction<*>,
        context: EphemeralTestContext
    ): AssertionRecord {
        return try {
            val args = createArguments(implFunc, context)

            val result = implFunc.callBy(args)

            contractValidator.validate(contractKFunc, result)

            AssertionRecord(
                status = AssertionStatus.PASSED,
                message = "Method '${contractMethod.name}' executed successfully and satisfied contract.",
                expected = "No Exception & Valid Return",
                actual = "Success"
            )
        } catch (e: Throwable) {
            val rootCause = ExceptionHelper.unwrap(e)

            if (rootCause is ContractValidator.ContractViolationException) {
                logger.error { "Contract Violation in ${contractMethod.name}: ${rootCause.message}" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    message = "Contract Violation: ${rootCause.message}",
                    expected = "Constraint Compliance",
                    actual = "Violation"
                )
            } else {
                logger.error(rootCause) { "Unexpected Exception in ${contractMethod.name}" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    message = "Method '${contractMethod.name}' threw an exception: ${rootCause.message}",
                    expected = "No Exception",
                    actual = rootCause.javaClass.simpleName
                )
            }
        }
    }

    private fun createArguments(
        function: KFunction<*>,
        context: EphemeralTestContext
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
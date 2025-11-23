package execution.domain.service

import discovery.api.LongRange
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.AssertionRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions

class DefaultScenarioExecutor : TestScenarioExecutor {

    private val logger = KotlinLogging.logger {}

    override fun executeScenarios(context: EphemeralTestContext): List<AssertionRecord> {
        val testTargetInstance = context.getTestTarget()

        val specification = context.specification

        val targetClass = specification.target.kClass.java

        val contractInterface = targetClass.interfaces.firstOrNull()
            ?: return emptyList()

        val implementationKClass = testTargetInstance::class

        return contractInterface.methods.map { contractMethod: Method ->
            val implementationFunction = implementationKClass.functions.find { kFunc ->
                kFunc.name == contractMethod.name &&
                        kFunc.parameters.size == contractMethod.parameterCount + 1
            } ?: throw IllegalStateException("Method '${contractMethod.name}' not found in implementation.")

            try {
                val arguments = createArgumentsFor(implementationFunction, context)
                implementationFunction.callBy(arguments)

                AssertionRecord(
                    status = AssertionStatus.PASSED,
                    message = "Method '${implementationFunction.name}' executed successfully.",
                    expected = "No Exception",
                    actual = "No Exception"
                )
            } catch (e: Throwable) {
                val actualException = e.cause ?: e
                logger.error(actualException) { "Exception thrown during scenario execution." }

                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    message = "Method '${contractMethod.name}' threw an exception.",
                    expected = "No Exception",
                    actual = actualException.message ?: actualException.toString()
                )
            }
        }
    }

    private fun createArgumentsFor(
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

            val longRange = param.annotations.filterIsInstance<LongRange>().firstOrNull()
            if (longRange != null) {
                val value = if (param.type.classifier == Int::class) longRange.min.toInt() else longRange.min
                arguments[param] = value
            } else {
                val argumentValue = when (val type = param.type.classifier as? KClass<*>) {
                    Int::class -> 0
                    Long::class -> 0L
                    String::class -> "test"
                    Boolean::class -> false
                    else -> if (type != null) {
                        context.mockingEngine.createMock(type)
                    } else {
                        null
                    }
                }
                arguments[param] = argumentValue
            }
        }
        return arguments
    }
}
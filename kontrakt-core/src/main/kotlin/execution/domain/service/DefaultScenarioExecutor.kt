package execution.domain.service

import discovery.api.LongRange
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.TestContext
import execution.domain.vo.AssertionRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.kotlinFunction

class DefaultScenarioExecutor : TestScenarioExecutor {

    private val logger = KotlinLogging.logger {}

    override fun executeScenarios(context: TestContext): List<AssertionRecord> {
        val testTargetInstance = context.getTestTarget()
        val specification = context.getSpecification()

        val contractInterface = specification.target.kClass.java.interfaces.firstOrNull()
            ?: return emptyList()

        val implementationKClass = testTargetInstance::class

        return contractInterface.methods.map { contractMethod ->
            val implementationFunction = implementationKClass.java.methods.first {
                it.name == contractMethod.name && it.parameterCount == contractMethod.parameterCount
            }.kotlinFunction

            try {
                val arguments = createArgumentsFor(implementationFunction!!, context)
                implementationFunction.callBy(arguments)

                AssertionRecord(
                    status = AssertionStatus.PASSED,
                    message = "Method '${implementationFunction.name}' executed successfully.",
                    expected = "No Exception",
                    actual = "No Exception"
                )
            } catch (e: Throwable) {
                logger.error(e) { "Exception thrown during scenario execution for method '${contractMethod.name}'" }
                AssertionRecord(
                    status = AssertionStatus.FAILED,
                    message = "Method '${contractMethod.name}' threw an exception.",
                    expected = "No Exception",
                    actual = e.cause ?: e
                )
            }
        }
    }

    private fun createArgumentsFor(
        function: KFunction<*>,
        context: TestContext
    ): Map<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()
        arguments[function.parameters.first()] = context.getTestTarget()

        function.parameters.drop(1).forEach { param ->
            val argumentValue = if (param.isOptional) {
                UseDefaultArgument
            } else {
                val intRange = param.annotations.filterIsInstance<LongRange>().firstOrNull()
                if (intRange != null) {
                    intRange.min.toInt()
                }
                else {
                    when (val type = param.type.classifier as? KClass<*>) {
                        Int::class -> 0
                        String::class -> ""
                        Boolean::class -> false
                        else -> if (type != null) context.getMockingEngine().createMock(type) else null
                    }
                }
            }

            if (argumentValue != UseDefaultArgument) {
                arguments[param] = argumentValue
            }
        }
        return arguments
    }

    private object UseDefaultArgument
}
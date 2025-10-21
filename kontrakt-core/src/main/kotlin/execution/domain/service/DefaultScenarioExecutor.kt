package execution.domain.service

import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.entity.TestContext
import execution.domain.vo.AssertionRecord
import kotlin.reflect.full.functions

class DefaultScenarioExecutor : TestScenarioExecutor {
    override fun executeScenarios(context: TestContext): List<AssertionRecord> {
        val testTargetInstance = context.getTestTarget()
        val specification = context.getSpecification()

        val contractKClass = specification.target.kClass.java.interfaces.firstOrNull()?.kotlin ?: return emptyList()

        val implementationKClass = testTargetInstance::class

        return contractKClass.functions.map { contractFunction ->
            val implementationFunction = implementationKClass.functions.first {
                it.name == contractFunction.name && it.parameters.size == contractFunction.parameters.size
            }
            try {
                val arguments = createArgumentsFor(implementationFunction, context)

                implementationFunction.callBy(arguments)

                AssertionRecord(
                    status = AssertionStatus.PASSED,
                    message = "Method '${implementationFunction.name}' executed successfully with default arguments.",
                    expected = "No Exception",
                    actual =
                )
            }

        }

        // 1. 구현체에 기본값 있으면 그걸 먼저 사용
        // 2. 인터페이스에 파라미터 범위 어노테이션 있으면 그걸 사용
        // 3. 비어있는 기본값 사용


    }
}
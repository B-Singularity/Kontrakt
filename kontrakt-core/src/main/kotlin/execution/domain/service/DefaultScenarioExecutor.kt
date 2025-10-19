package execution.domain.service

import execution.api.TestScenarioExecutor
import execution.domain.entity.TestContext
import execution.domain.vo.AssertionRecord

class DefaultScenarioExecutor : TestScenarioExecutor {
    override fun executeScenarios(context: TestContext): List<AssertionRecord> {
        val testTargetInstance = context.getTestTarget()

        val specification = context.getSpecification()
        val contractInterface = specification.target.kClass.java.interfaces.first()

        val methodsToTest = contractInterface.methods


    }
}
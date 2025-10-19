package execution.api

import execution.domain.entity.TestContext
import execution.domain.vo.AssertionRecord

interface TestScenarioExecutor {
    fun executeScenarios(context: TestContext): List<AssertionRecord>
}
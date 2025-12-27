package execution.api

import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.AssertionRecord

interface TestScenarioExecutor {
    fun executeScenarios(context: EphemeralTestContext): List<AssertionRecord>
}

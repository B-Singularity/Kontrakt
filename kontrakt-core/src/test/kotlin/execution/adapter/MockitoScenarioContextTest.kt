package execution.adapter

import execution.api.ScenarioContext
import execution.api.ScenarioContextTest

class MockitoScenarioContextTest : ScenarioContextTest() {

    override fun createScenarioContext(): ScenarioContext {
        return MockitoScenarioContext()
    }
}
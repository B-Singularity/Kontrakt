package execution.spi

import execution.api.ScenarioContext

interface ScenarioControl {
    fun createScenarioContext(): ScenarioContext
}
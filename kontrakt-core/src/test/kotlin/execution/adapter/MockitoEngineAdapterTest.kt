package execution.adapter

import execution.spi.MockingEngine
import execution.spi.MockingEngineTest
import execution.spi.ScenarioControl

class MockitoEngineAdapterTest : MockingEngineTest() {

    private val adapter = MockitoEngineAdapter()

    override val engine: MockingEngine = adapter
    override val control: ScenarioControl = adapter
}
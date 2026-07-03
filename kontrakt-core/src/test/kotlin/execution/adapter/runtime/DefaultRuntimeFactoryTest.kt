package execution.adapter.runtime

import adapter.file.WorkerTraceSinkPool
import adapter.jvm.DefaultRuntimeFactory
import execution.port.incoming.KontraktRuntimeFactoryContract
import io.mockk.mockk
import ir.TestSpecification
import migration.quarantine.ExecutionPolicy
import realization.execution.mocking.MockingEngine
import realization.execution.scenario.ScenarioControl
import realization.reporting.TestResultPublisher
import stage.input.boundary.KontraktRuntimeFactory
import stage.input.boundary.TestScenarioExecutor
import java.time.Clock

class DefaultRuntimeFactoryTest : KontraktRuntimeFactoryContract {
    private val mockingEngine = mockk<MockingEngine>(relaxed = true)
    private val scenarioControl = mockk<ScenarioControl>(relaxed = true)
    private val traceSinkPool = mockk<WorkerTraceSinkPool>(relaxed = true)
    private val resultPublisher = mockk<TestResultPublisher>(relaxed = true)
    private val clock = Clock.systemUTC()
    private val executionPolicy = mockk<ExecutionPolicy>(relaxed = true)

    override fun createSut(): KontraktRuntimeFactory =
        DefaultRuntimeFactory(
            mockingEngine = mockingEngine,
            scenarioControl = scenarioControl,
            traceSinkPool = traceSinkPool,
            resultPublisher = resultPublisher,
            clock = clock,
            executionPolicy = executionPolicy,
        )

    override fun createTestSpecification(): TestSpecification = mockk(relaxed = true)

    override fun createTestScenarioExecutor(): TestScenarioExecutor = mockk(relaxed = true)
}

package execution.adapter.runtime

import adapter.file.WorkerTraceSinkPool
import adapter.jvm.DefaultRuntimeFactory
import execution.domain.vo.config.ExecutionPolicy
import execution.port.incoming.KontraktRuntimeFactory
import execution.port.incoming.KontraktRuntimeFactoryContract
import execution.port.incoming.TestScenarioExecutor
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioControl
import execution.port.outgoing.TestResultPublisher
import io.mockk.mockk
import ir.TestSpecification
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

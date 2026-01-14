package execution.api

import discovery.domain.aggregate.TestSpecification
import execution.adapter.trace.WorkerTraceSinkPool
import execution.domain.aggregate.TestExecution
import execution.domain.service.generation.TestInstanceFactory
import execution.domain.service.orchestration.DefaultScenarioExecutor
import execution.domain.vo.ExecutionPolicy
import execution.port.outgoing.TestResultPublisher
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import java.time.Clock

/**
 * [Default Implementation] Standard Local Runtime Factory.
 *
 * Implements the standard local execution strategy using:
 * - Mockito for mocking
 * - System Clock (or Fixed Clock for testing)
 * - Console Reporting
 * - Sequential Execution
 *
 * This class acts as the **DI Container**, managing the lifecycle of infrastructure components.
 */
class DefaultRuntimeFactory(
    private val mockingEngine: MockingEngine,
    private val scenarioControl: ScenarioControl,
    private val traceSinkPool: WorkerTraceSinkPool,
    private val resultPublisher: TestResultPublisher,
    private val clock: Clock,
    private val executionPolicy: ExecutionPolicy,
) : KontraktRuntimeFactory {
    override fun createExecutor(): TestScenarioExecutor =
        DefaultScenarioExecutor(
            clock = clock,
        )

    override fun createExecution(
        spec: TestSpecification,
        executor: TestScenarioExecutor,
    ): TestExecution {
        val instanceFactory =
            TestInstanceFactory(
                mockingEngine = mockingEngine,
                scenarioControl = scenarioControl,
                clock = clock,
            )

        return TestExecution(
            spec = spec,
            instanceFactory = instanceFactory,
            scenarioExecutor = executor,
            traceSinkPool = traceSinkPool,
            resultPublisher = resultPublisher,
            clock = clock,
            executionPolicy = executionPolicy,
        )
    }
}

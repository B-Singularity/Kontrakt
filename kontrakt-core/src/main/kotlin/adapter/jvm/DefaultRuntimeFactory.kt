package adapter.jvm

import adapter.file.WorkerTraceSinkPool
import ir.TestSpecification
import migration.quarantine.ExecutionPolicy
import realization.execution.aggregate.TestExecution
import realization.execution.generation.TestInstanceFactory
import realization.execution.mocking.MockingEngine
import realization.execution.orchestration.DefaultScenarioExecutor
import realization.execution.scenario.ScenarioControl
import realization.reporting.TestResultPublisher
import stage.input.boundary.KontraktRuntimeFactory
import stage.input.boundary.TestScenarioExecutor
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

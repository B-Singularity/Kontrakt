package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import execution.adapter.MockitoEngineAdapter
import execution.api.TestScenarioExecutor
import execution.domain.aggregate.TestExecution
import execution.domain.service.DefaultScenarioExecutor
import execution.domain.service.TestInstanceFactory

interface KontraktRuntimeFactory {
    fun createExecutor(): TestScenarioExecutor
    fun createExecution(spec: TestSpecification, executor: TestScenarioExecutor): TestExecution
}

class DefaultRuntimeFactory : KontraktRuntimeFactory {
    override fun createExecutor(): TestScenarioExecutor {
        return DefaultScenarioExecutor()
    }

    override fun createExecution(
        spec: TestSpecification,
        executor: TestScenarioExecutor
    ): TestExecution {
        val engineAdapter = MockitoEngineAdapter()
        val instanceFactory = TestInstanceFactory(engineAdapter, engineAdapter)

        return TestExecution(spec, instanceFactory, executor)
    }
}
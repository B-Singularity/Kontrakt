package execution.port.incoming

import discovery.domain.aggregate.TestSpecification
import execution.domain.aggregate.TestExecution

/**
 * [Extension Point] Runtime Factory API
 *
 * Defines the contract for creating the core components of the test runtime.
 * This abstraction allows swapping the entire execution engine (e.g., Local vs Cloud, Serial vs Parallel)
 * without modifying the JUnit adapter.
 *
 * It guarantees **Future Extensibility** by decoupling the Engine from the Implementation.
 */
interface KontraktRuntimeFactory {
    /**
     * Creates a configured Executor.
     * Allows customization of how scenarios are invoked (e.g., Reflection, CodeGen, Remote).
     */
    fun createExecutor(): TestScenarioExecutor

    /**
     * Assembles the full Test Execution Aggregate.
     * Wires together the Factory, Executor, Publisher, and Infrastructure.
     */
    fun createExecution(
        spec: TestSpecification,
        executor: TestScenarioExecutor,
    ): TestExecution
}

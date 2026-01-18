package execution.api

import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.ExecutionResult

/**
 * [API] Test Scenario Executor.
 *
 * Defines the contract for executing a specific test strategy (e.g., User Scenario, Contract Fuzzing).
 * Implementations must return an [ExecutionResult] containing both the assertion records
 * and the arguments used during execution to ensure transparency.
 */
interface TestScenarioExecutor {
    /**
     * Executes the test scenarios defined in the context.
     *
     * @param context The ephemeral test context containing the target and configuration.
     * @return The result of the execution, including records and arguments.
     */
    fun executeScenarios(context: EphemeralTestContext): ExecutionResult
}

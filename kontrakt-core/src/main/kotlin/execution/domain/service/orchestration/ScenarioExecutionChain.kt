package execution.domain.service.orchestration

import execution.api.TestScenarioExecutor
import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.AssertionRecord
import execution.spi.interceptor.ScenarioInterceptor

/**
 * [Infrastructure] Scenario Execution Chain
 *
 * A concrete implementation of [ScenarioInterceptor.Chain] that orchestrates the flow of execution.
 * It manages the recursion through the list of registered interceptors.
 *
 * When the end of the interceptor list is reached, it delegates the actual test execution
 * to the [finalDelegate] (typically the [DefaultScenarioExecutor]).
 *
 * @property interceptors The list of interceptors to be executed.
 * @property index The current index in the interceptor list.
 * @property context The context of the test being executed.
 * @property finalDelegate The core executor that runs the test logic after all interceptors.
 */
class ScenarioExecutionChain(
    private val interceptors: List<ScenarioInterceptor>,
    private val index: Int,
    override val context: EphemeralTestContext,
    private val finalDelegate: TestScenarioExecutor,
) : ScenarioInterceptor.Chain {
    override fun proceed(context: EphemeralTestContext): List<AssertionRecord> {
        // Base Case: If we have iterated through all interceptors, execute the actual test.
        if (index >= interceptors.size) {
            return finalDelegate.executeScenarios(context)
        }

        // Recursive Step: Create the next link in the chain, advancing the index.
        val nextChain =
            ScenarioExecutionChain(
                interceptors = interceptors,
                index = index + 1,
                context = context,
                finalDelegate = finalDelegate,
            )

        // Retrieve the current interceptor and delegate control to it.
        val currentInterceptor = interceptors[index]
        return currentInterceptor.intercept(nextChain)
    }
}

package execution.spi.interceptor

import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.AssertionRecord

/**
 *
 * Intercepts the execution flow of a test scenario to handle cross-cutting concerns.
 * (e.g., Auditing, Metrics, Retry, Timeout, Circuit Breaker, etc.)
 *
 * This interface is defined as a **Functional Interface (SAM)** to support lambda expressions.
 *
 * @see Chain
 */
fun interface ScenarioInterceptor {
    /**
     * Executes the interceptor logic and delegates control to the next step in the chain.
     *
     * Implementations **MUST** call [Chain.proceed] to continue the execution pipeline.
     * Failure to call [Chain.proceed] will block the execution flow.
     *
     * @param chain The chain object to control execution flow.
     * @return A list of assertion records resulting from the execution.
     */
    fun intercept(chain: Chain): List<AssertionRecord>

    /**
     * Manages the chain of interceptors.
     *
     * Provides the execution context to the interceptor and a mechanism to proceed to the next step.
     */
    interface Chain {
        /**
         * The immutable context of the currently running test.
         */
        val context: EphemeralTestContext

        /**
         * Proceeds to the next interceptor or the final execution delegate.
         *
         * @param context The context to be passed to the next step.
         * @return A list of assertion records.
         */
        fun proceed(context: EphemeralTestContext): List<AssertionRecord>
    }
}

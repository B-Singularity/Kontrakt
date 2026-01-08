package execution.domain.interceptor

import common.util.extractSourceLocation
import common.util.unwrapped
import discovery.domain.aggregate.TestSpecification
import exception.ContractViolationException
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.domain.AssertionStatus
import execution.domain.vo.AssertionRecord
import execution.domain.vo.AssertionRule
import execution.domain.vo.ConfigurationErrorRule
import execution.domain.vo.SourceLocation
import execution.domain.vo.StandardAssertion
import execution.domain.vo.SystemErrorRule
import execution.domain.vo.UserExceptionRule
import execution.spi.interceptor.ScenarioInterceptor

/**
 * [ADR-020] Centralized Result Resolution Interceptor.
 *
 * Acting as the "Brain" of the execution pipeline, this interceptor sits at the very top of the chain.
 * It is responsible for transforming raw execution outcomes (including exceptions) into a standardized
 * list of [AssertionRecord]s.
 *
 * Its core responsibilities include:
 * 1. **Error Handling**: Catches all unhandled exceptions thrown by downstream interceptors or the executor.
 * 2. **Blame Assignment**: Distinguishes between User Errors (Logic/Config) and Framework Bugs using semantic rules.
 * 3. **Coordinate Mining**: Extracts precise source locations (File:Line) using [extractSourceLocation].
 * 4. **Result Normalization**: Ensures every outcome, whether success or failure, has a consistent format.
 *
 * @property spec The test specification containing target class metadata.
 * @property traceMode If true, enables expensive location enrichment for successful assertions.
 */
class ResultResolverInterceptor(
    private val spec: TestSpecification,
    private val traceMode: Boolean = false
) : ScenarioInterceptor {

    override fun intercept(chain: ScenarioInterceptor.Chain): List<AssertionRecord> =
        runCatching {
            // 1. Proceed with the execution chain.
            chain.proceed(chain.context)
        }.map { results ->
            // 2. [Success Path] Lazy Optimization:
            // If Trace Mode is ON, we attempt to enrich 'NotCaptured' locations
            // with the Test Target's class location as a fallback context.
            // If OFF, we skip this to save performance.
            if (traceMode) {
                results.map { it.enrichLocationIfMissing(spec) }
            } else {
                results
            }
        }.getOrElse { exception ->
            // 3. [Failure Path] Single point of failure resolution.
            // Any exception bubbling up is caught here and converted into a failure record.
            listOf(exception.toFailureRecord(spec))
        }

    // -------------------------------------------------------------------------
    // Domain Logic Extensions
    // -------------------------------------------------------------------------

    /**
     * Converts a raw Throwable into a structured [AssertionRecord].
     * This method implements the "Blame Assignment" logic using specific [AssertionRule] types.
     */
    private fun Throwable.toFailureRecord(spec: TestSpecification): AssertionRecord {
        val rootCause = this.unwrapped
        val location = rootCause.extractSourceLocation(spec.target.kClass)

        return when (rootCause) {
            // Case A: Contract Violation (Business Logic Error)
            // The user's implementation failed to meet the defined contract (e.g., @Positive).
            is ContractViolationException -> createAssertionRecord(
                status = AssertionStatus.FAILED,
                rule = rootCause.rule,
                message = rootCause.message ?: "Contract violated",
                expected = "Constraint Compliance",
                actual = "Violation",
                location = location
            )

            // Case B: Standard Assertion Failure (JUnit/Kotlin Assertions)
            // A standard assertion (e.g., assert x == y) failed in the user's test code.
            is AssertionError -> createAssertionRecord(
                status = AssertionStatus.FAILED,
                rule = StandardAssertion,
                message = rootCause.message ?: "Assertion failed",
                expected = "True",
                actual = "False",
                location = location
            )

            // Case C: Configuration Failure (User Fault)
            // The user provided invalid input (e.g., ambiguous annotations, invalid dependency).
            is KontraktConfigurationException -> createAssertionRecord(
                status = AssertionStatus.FAILED,
                rule = ConfigurationErrorRule,
                message = "Configuration Error: ${rootCause.message}",
                expected = "Valid Configuration",
                actual = "Invalid Configuration",
                location = location
            )

            // Case D: Internal Framework Error (System Fault)
            // Something went wrong within Kontrakt itself. This is a bug in the framework.
            is KontraktInternalException -> createAssertionRecord(
                status = AssertionStatus.FAILED,
                rule = SystemErrorRule("InternalError"),
                message = "💥 Internal Framework Error: ${rootCause.message}",
                expected = "Framework Stability",
                actual = "Crash",
                location = location
            )

            // Case E: Unexpected Runtime Exception (User Code Crash)
            // The user's code threw an unexpected exception (e.g., NPE, IndexOutOfBounds).
            else -> createAssertionRecord(
                status = AssertionStatus.FAILED,
                rule = UserExceptionRule(rootCause.javaClass.simpleName),
                message = "Unexpected Exception: ${rootCause.message}",
                expected = "Normal Execution",
                actual = rootCause.javaClass.simpleName,
                location = location
            )
        }
    }

    /**
     * Factory helper to create [AssertionRecord].
     * Keeps the main logic clean by hiding the constructor details.
     */
    private fun createAssertionRecord(
        status: AssertionStatus,
        rule: AssertionRule,
        message: String,
        location: SourceLocation,
        expected: String? = null,
        actual: String? = null
    ) = AssertionRecord(
        status = status,
        rule = rule,
        message = message,
        expected = expected,
        actual = actual,
        location = location
    )

    /**
     * Fallback mechanism for successful assertions when Trace Mode is enabled.
     *
     * If an assertion passed but has [SourceLocation.NotCaptured], this attempts to provide
     * at least the [SourceLocation.Approximate] context (Class Level) so the report isn't empty.
     */
    private fun AssertionRecord.enrichLocationIfMissing(spec: TestSpecification): AssertionRecord {
        return if (this.location is SourceLocation.NotCaptured) {
            copy(
                location = SourceLocation.Approximate(
                    className = spec.target.fullyQualifiedName,
                    displayName = spec.target.displayName
                )
            )
        } else {
            this
        }
    }
}
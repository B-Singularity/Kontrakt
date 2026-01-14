package execution.domain.aggregate

import common.util.unwrapped
import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.adapter.trace.WorkerTraceSinkPool
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import execution.domain.interceptor.AuditingInterceptor
import execution.domain.interceptor.ResultResolverInterceptor
import execution.domain.service.generation.TestInstanceFactory
import execution.domain.service.orchestration.ScenarioExecutionChain
import execution.domain.vo.AssertionRecord
import execution.domain.vo.AuditDepth
import execution.domain.vo.ExecutionPolicy
import execution.domain.vo.TestResult
import execution.domain.vo.WorkerId
import execution.port.outgoing.TestResultPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * [Aggregate Root] Test Execution Lifecycle Manager.
 *
 * This class orchestrates the entire lifecycle of a single test specification execution.
 * It is responsible for:
 * 1. **Setup**: Creating the test context and target instance via [TestInstanceFactory].
 * 2. **Pipeline Assembly**: Constructing the Interceptor Chain ([ResultResolverInterceptor] -> [AuditingInterceptor]).
 * 3. **Execution**: Triggering the chain.
 * 4. **Result Aggregation**: Compiling the final [TestResult] for the JUnit Engine.
 */
class TestExecution(
    private val spec: TestSpecification,
    private val instanceFactory: TestInstanceFactory,
    private val scenarioExecutor: TestScenarioExecutor,
    private val traceSinkPool: WorkerTraceSinkPool,
    private val resultPublisher: TestResultPublisher,
    private val clock: Clock,
    private val executionPolicy: ExecutionPolicy,
) {
    private val logger = KotlinLogging.logger {}

    private val executed = AtomicBoolean(false)

    fun execute(): TestResult {
        if (!executed.compareAndSet(false, true)) {
            throw KontraktInternalException("TestExecution instance cannot be reused. It has already been executed.")
        }

        val startTime = Instant.now(clock)

        try {
            val actualSeed = executionPolicy.determinism.seed ?: Random.nextLong()

            // 2. [Setup Phase] Create Test Context & Target
            // If this fails (e.g. DI error), it happens BEFORE the interceptor chain.
            val context = instanceFactory.create(spec)

            // This ensures that parallel JUnit execution gets unique IDs and Log Sinks.
            val currentWorkerId = WorkerId.fromCurrentThread()

            val currentSink = traceSinkPool.getSink(currentWorkerId)

            // 3. [Assembly Phase] Build Interceptor Chain
            // Order matters: Resolver (Outer) -> Auditing (Middle) -> Executor (Inner/Leaf)
            val interceptors =
                listOf(
                    ResultResolverInterceptor(
                        spec,
                        traceMode = executionPolicy.auditing.depth == AuditDepth.EXPLAINABLE,
                    ),
                    AuditingInterceptor(
                        traceSink = currentSink,
                        resultPublisher = resultPublisher,
                        policy = executionPolicy.auditing,
                        seed = actualSeed,
                        clock = clock,
                        workerId = currentWorkerId,
                    ),
                )

            val chain =
                ScenarioExecutionChain(
                    interceptors = interceptors,
                    index = 0,
                    context = context,
                    finalDelegate = scenarioExecutor,
                )

            // 4. [Execution Phase] Run the Pipeline
            // ResultResolverInterceptor ensures this returns records, not exceptions (mostly).
            val records = chain.proceed(context)

            // 5. [Result Phase] Calculate Final Status
            val duration = Duration.between(startTime, Instant.now(clock))
            val finalStatus = determineFinalStatus(records)

            return TestResult(
                target = spec.target,
                finalStatus = finalStatus,
                duration = duration,
                assertionRecords = records,
            )
        } catch (e: Throwable) {
            // [Critical Failure]
            // This catches errors during Context Creation (Setup) or catastrophic framework failures
            // that ResultResolver couldn't catch.
            val cause = e.unwrapped
            val duration = Duration.between(startTime, Instant.now(clock))

            logger.error(cause) { "Test execution crashed during setup phase: ${spec.target.displayName}" }

            return TestResult(
                target = spec.target,
                finalStatus = TestStatus.ExecutionError(cause),
                duration = duration,
                assertionRecords = emptyList(),
            )
        }
    }

    /**
     * Aggregates a list of records into a single TestStatus.
     * Priority: Fail > Pass
     */
    private fun determineFinalStatus(records: List<AssertionRecord>): TestStatus {
        val firstFailure = records.firstOrNull { it.status == AssertionStatus.FAILED }

        return if (firstFailure != null) {
            TestStatus.AssertionFailed(
                message = firstFailure.message,
                expected = firstFailure.expected,
                actual = firstFailure.actual,
                cause = null,
            )
        } else {
            TestStatus.Passed
        }
    }
}

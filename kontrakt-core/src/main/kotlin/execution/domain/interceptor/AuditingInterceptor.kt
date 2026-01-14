package execution.domain.interceptor

import exception.ContractViolationException
import execution.domain.TestStatus
import execution.domain.vo.AssertionRecord
import execution.domain.vo.AuditDepth
import execution.domain.vo.AuditPolicy
import execution.domain.vo.LogRetention
import execution.domain.vo.TestResultEvent
import execution.domain.vo.WorkerId
import execution.domain.vo.trace.ExceptionTrace
import execution.domain.vo.trace.ExecutionTrace
import execution.domain.vo.trace.TestVerdict
import execution.domain.vo.trace.TracePhase
import execution.domain.vo.trace.VerificationTrace
import execution.port.outgoing.TestResultPublisher
import execution.port.outgoing.TraceSink
import execution.spi.interceptor.ScenarioInterceptor
import execution.spi.trace.ScenarioTrace
import java.time.Clock

/**
 * [Domain Service] Auditing & Reporting Interceptor.
 *
 * Implements **ADR-021 (Log Diet & Smart Snapshot)** strategies using the new [AuditPolicy].
 *
 * This interceptor acts as the "Black Box Recorder" for the test execution.
 * It sits between the Result Resolver and the actual execution, capturing every
 * significant event and writing it to the [TraceSink].
 *
 * **Core Responsibilities:**
 * 1. **Log Diet:** Filters out high-volume `DESIGN` logs based on [AuditDepth].
 * 2. **Event Journaling:** Records Execution, Verification, and Exception events in real-time.
 * 3. **Smart Snapshot:** Persists the log file to disk based on [LogRetention].
 * 4. **Result Publishing:** Notifies the [TestResultPublisher] of the final outcome.
 */
class AuditingInterceptor(
    private val traceSink: TraceSink,
    private val resultPublisher: TestResultPublisher,
    private val policy: AuditPolicy,
    private val seed: Long,
    private val clock: Clock,
    private val workerId: WorkerId,
) : ScenarioInterceptor {
    override fun intercept(chain: ScenarioInterceptor.Chain): List<AssertionRecord> =
        with(chain.context) {
            val startTime = clock.millis()
            val testName = targetMethod.name
            var caughtException: Throwable? = null

            // 1. [GIVEN] Log Design Decisions (Applied Log Diet)
            emitDesignDecisions(trace)

            try {
                // 2. [WHEN] Proceed with execution
                val records = chain.proceed(this)
                val executionDurationMs = clock.millis() - startTime

                // 3. [THEN] Log Execution & Verification
                emitExecutionTrace(testName, trace.generatedArguments, executionDurationMs)
                emitVerificationTraces(records)

                return records
            } catch (e: Throwable) {
                caughtException = e
                // 4. [ERROR] Log Exception immediately (Critical)
                emitExceptionTrace(e)
                throw e // Re-throw for ResultResolverInterceptor to handle
            } finally {
                // 5. [FINALLY] Seal the log and publish report
                finalizeSession(trace, testName, caughtException, startTime)
            }
        }

    /**
     * Emits generated fixture data to the sink.
     * Applies strict filtering based on [AuditDepth] to prevent IO saturation.
     */
    private fun emitDesignDecisions(trace: ScenarioTrace) {
        // [Optimization] Fast-exit if no logs to process
        if (trace.decisions.isEmpty()) return

        val includeDesign = policy.depth == AuditDepth.EXPLAINABLE

        trace.decisions.forEach { event ->
            if (includeDesign || event.phase != TracePhase.DESIGN) {
                traceSink.emit(event)
            }
        }
    }

    private fun emitExecutionTrace(
        methodName: String,
        args: List<Any?>,
        durationMs: Long,
    ) {
        traceSink.emit(
            ExecutionTrace(
                methodSignature = methodName,
                arguments = args.map { it.toString() },
                durationMs = durationMs,
                timestamp = clock.millis(),
            ),
        )
    }

    private fun emitVerificationTraces(records: List<AssertionRecord>) {
        records.forEach { record ->
            traceSink.emit(
                VerificationTrace(
                    rule = record.rule.key,
                    status = record.status,
                    detail = record.message,
                    timestamp = clock.millis(),
                ),
            )
        }
    }

    private fun emitExceptionTrace(e: Throwable) {
        traceSink.emit(
            ExceptionTrace(
                exceptionType = e.javaClass.name,
                message = e.message.orEmpty(),
                stackTraceElements = e.stackTrace.take(15),
                timestamp = clock.millis(),
            ),
        )
    }

    // -------------------------------------------------------------------------
    // Finalization & Reporting
    // -------------------------------------------------------------------------

    /**
     * Determines the final verdict, flushes the sink, snapshots if necessary,
     * and publishes the result to the UI layer.
     */
    private fun finalizeSession(
        trace: ScenarioTrace,
        testName: String,
        error: Throwable?,
        startTime: Long,
    ) {
        val totalDurationMs = clock.millis() - startTime

        // 1. Determine Status
        val status =
            when (error) {
                null -> TestStatus.Passed
                is AssertionError,
                is ContractViolationException,
                ->
                    TestStatus.AssertionFailed(
                        message = error.message ?: "Assertion Failed",
                        expected = "Contract Compliance",
                        actual = "Violation",
                    )

                else -> TestStatus.ExecutionError(error)
            }

        // 2. Emit Final Verdict (The Seal)
        traceSink.emit(
            TestVerdict(
                status = status,
                durationTotalMs = totalDurationMs,
                timestamp = clock.millis(),
            ),
        )

        // 3. Snapshot Strategy (Smart Flush via LogRetention Policy)
        val shouldSnapshot =
            when (policy.retention) {
                LogRetention.NONE -> false
                LogRetention.ALWAYS -> true
                LogRetention.ON_FAILURE -> status !is TestStatus.Passed
            }

        val journalPath =
            if (shouldSnapshot) {
                val subDir = if (status !is TestStatus.Passed) "failures" else "traces"
                traceSink.snapshotTo("$subDir/run-${trace.runId}.log")
            } else {
                ""
            }

        // 4. Reset Sink for next run (Recycling)
        traceSink.reset()

        // 5. Publish Result
        resultPublisher.publish(
            TestResultEvent(
                runId = trace.runId,
                testName = testName,
                workerId = workerId,
                status = status,
                durationMs = totalDurationMs,
                journalPath = journalPath,
                timestamp = clock.millis(),
                seed = seed,
            ),
        )
    }
}

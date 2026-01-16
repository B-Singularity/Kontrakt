package execution.domain.interceptor

import execution.domain.service.VerdictDecider
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
import kotlin.math.max

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
    private val verdictDecider: VerdictDecider = VerdictDecider(),
) : ScenarioInterceptor {
    override fun intercept(chain: ScenarioInterceptor.Chain): List<AssertionRecord> =
        with(chain.context) {
            val sessionStartTime = clock.millis()

            val testIdentifier = targetMethod.toString()
            var caughtException: Throwable? = null
            var capturedRecords: List<AssertionRecord> = emptyList()


            try {
                // 2. [WHEN] Proceed with execution
                capturedRecords = chain.proceed(this)
                val executionEndTime = clock.millis()

                // 2.1 Emit Filtered Fixture Logs (Design Decisions)
                // Dump the decision history stored in the trace.
                emitFilteredDesignDecisions(trace)

                // 2.2 Emit Execution Trace
                // [Safety] Defensive copy: Handle cases where arguments might be missing or partial.
                // TODO: (Refactoring) Move argument recording responsibility to Executor for better reliability.
                val safeArgs = trace.generatedArguments.takeIf { it.isNotEmpty() } ?: emptyList()

                // [Time] executionDurationMs: Pure method execution time (excluding interceptor overhead)
                emitExecutionTrace(
                    methodName = testIdentifier,
                    args = safeArgs,
                    durationMs = max(0L, executionEndTime - sessionStartTime)
                )
                emitVerificationTraces(capturedRecords)

                return capturedRecords

            } catch (e: Throwable) {
                caughtException = e
                // 4. [ERROR] Log Exception immediately (Critical)
                emitExceptionTrace(e)
                throw e // Re-throw for ResultResolverInterceptor to handle
            } finally {
                finalizeSession(
                    trace = trace,
                    testName = testIdentifier,
                    error = caughtException,
                    records = capturedRecords,
                    startTime = sessionStartTime
                )
            }
        }

    /**
     * Wrap-up logic: Verdict -> Snapshot -> Publish
     */
    private fun finalizeSession(
        trace: ScenarioTrace,
        testName: String,
        error: Throwable?,
        records: List<AssertionRecord>,
        startTime: Long,
    ) {
        val endTime = clock.millis()
        val totalDurationMs = max(0L, endTime - startTime)

        // 1. Decide Final Verdict
        val status = verdictDecider.decide(error, records)

        // 2. Emit Verdict Trace
        traceSink.emit(
            TestVerdict(
                status = status,
                durationTotalMs = totalDurationMs,
                timestamp = endTime,
            ),
        )

        // 3. Snapshot Strategy (Persistence)
        val shouldSnapshot = when (policy.retention) {
            LogRetention.NONE -> false
            LogRetention.ALWAYS -> true
            LogRetention.ON_FAILURE -> !status.isPassed
        }

        val journalPath = if (shouldSnapshot) {
            val subDir = if (!status.isPassed) "failures" else "traces"
            traceSink.snapshotTo("$subDir/run-${trace.runId}.log")
        } else {
            ""
        }

        // 4. Publish Result
        val resultEvent = TestResultEvent(
            runId = trace.runId,
            testName = testName,
            workerId = workerId,
            status = status,
            durationMs = totalDurationMs,
            journalPath = journalPath,
            timestamp = endTime,
            seed = seed,
        )

        try {
            resultPublisher.publish(resultEvent)
        } finally {
            // [Safety] Always reset the sink to prevent memory leaks and state pollution
            traceSink.reset()
        }
    }


    /**
     * Emits generated fixture data to the sink.
     * Applies strict filtering based on [AuditDepth] to prevent IO saturation.
     */
    private fun emitFilteredDesignDecisions(trace: ScenarioTrace) {
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

}

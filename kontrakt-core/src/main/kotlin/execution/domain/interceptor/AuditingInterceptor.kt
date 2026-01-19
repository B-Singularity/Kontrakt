package execution.domain.interceptor

import execution.domain.service.VerdictDecider
import execution.domain.vo.config.AuditDepth
import execution.domain.vo.config.AuditPolicy
import execution.domain.vo.config.LogRetention
import execution.domain.vo.context.WorkerId
import execution.domain.vo.result.TestResultEvent
import execution.domain.vo.trace.ExceptionTrace
import execution.domain.vo.trace.TestVerdict
import execution.domain.vo.trace.TracePhase
import execution.domain.vo.trace.VerificationTrace
import execution.domain.vo.verification.AssertionRecord
import execution.port.outgoing.ScenarioInterceptor
import execution.port.outgoing.ScenarioTrace
import execution.port.outgoing.TestResultPublisher
import execution.port.outgoing.TraceSink
import java.time.Clock
import kotlin.math.max

/**
 * [Domain Service] Auditing & Reporting Interceptor.
 *
 * Implements **ADR-021 (Log Diet & Smart Snapshot)** strategies.
 *
 * This interceptor acts as the **"Flush Gateway"** for the test execution.
 * Since the [DefaultScenarioExecutor] now handles the accurate recording of execution facts (arguments, duration),
 * this interceptor focuses solely on **Filtering**, **Persisting**, and **Publishing** those events.
 *
 * **Core Responsibilities:**
 * 1. **Event Pipelining:** Flushes events accumulated in [ScenarioTrace] to the [TraceSink].
 * 2. **Log Diet:** Filters out high-volume `DESIGN` logs based on [AuditDepth].
 * 3. **Result Publishing:** Decides the final verdict and notifies the [TestResultPublisher].
 * 4. **Smart Snapshot:** Persists the log file to disk only when necessary (e.g., on failure).
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

            // Safe Test Identification
            val testIdentifier =
                try {
                    targetMethod.toString()
                } catch (e: Exception) {
                    specification.target.fullyQualifiedName
                }

            var caughtException: Throwable? = null
            var capturedRecords: List<AssertionRecord> = emptyList()

            try {
                // 1. [Proceed] Execute the chain.
                // The Executor will record 'ExecutionTrace' events into context.trace internally.
                capturedRecords = chain.proceed(this)

                // 2. [Flush] Pipe all accumulated events (Design, Execution) to the Sink.
                flushTraceEvents(trace)

                // 3. [Verify] Emit verification results derived from the returned records.
                emitVerificationTraces(capturedRecords)

                return capturedRecords
            } catch (e: Throwable) {
                caughtException = e
                // [Error] Emit exception trace immediately for debugging context
                emitExceptionTrace(e)
                throw e
            } finally {
                // 4. [Finalize] Verdict, Snapshot, and Publish
                finalizeSession(
                    trace = trace,
                    testName = testIdentifier,
                    error = caughtException,
                    records = capturedRecords,
                    startTime = sessionStartTime,
                )
            }
        }

    /**
     * Flushes events from the [ScenarioTrace] to the [TraceSink].
     * Applies filtering logic based on [AuditPolicy].
     *
     * Note: This method handles [ExecutionTrace] events that were created and added
     * by the [DefaultScenarioExecutor].
     */
    private fun flushTraceEvents(trace: ScenarioTrace) {
        if (trace.events.isEmpty()) return

        val includeDesign = policy.depth == AuditDepth.EXPLAINABLE

        trace.events.forEach { event ->
            // Filter out Design events if the audit depth is set to SIMPLE
            if (event.phase == TracePhase.DESIGN && !includeDesign) {
                return@forEach
            }

            // Emit everything else (Execution, Custom events, etc.)
            traceSink.emit(event)
        }
    }

    /**
     * Converts AssertionRecords (Domain Objects) into VerificationTrace events (Log Objects)
     * and emits them to the sink.
     */
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
                stackTraceElements = e.stackTrace.take(15).toList(),
                timestamp = clock.millis(),
            ),
        )
    }

    /**
     * Wrap-up logic: Decide Verdict -> Create Snapshot (File) -> Publish Result Event
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

        // 2. Emit Verdict Trace (End of Log)
        traceSink.emit(
            TestVerdict(
                status = status,
                durationTotalMs = totalDurationMs,
                timestamp = endTime,
            ),
        )

        // 3. Snapshot Strategy (Persistence)
        val shouldSnapshot =
            when (policy.retention) {
                LogRetention.NONE -> false
                LogRetention.ALWAYS -> true
                LogRetention.ON_FAILURE -> !status.isPassed
            }

        val journalPath =
            if (shouldSnapshot) {
                val subDir = if (!status.isPassed) "failures" else "traces"
                // The runId is injected by the Orchestrator via Context -> Trace
                traceSink.snapshotTo("$subDir/run-${trace.runId}.log")
            } else {
                ""
            }

        // 4. Publish Result to the world (Reporters, CI/CD)
        val resultEvent =
            TestResultEvent(
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
            // [Safety] Always reset the sink to prevent memory leaks and state pollution across tests
            traceSink.reset()
        }
    }
}

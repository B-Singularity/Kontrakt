package execution.domain.interceptor

import common.config.UserControlOptions
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import execution.domain.vo.AssertionRecord
import execution.domain.vo.TestResultEvent
import execution.domain.vo.trace.ExceptionTrace
import execution.domain.vo.trace.ExecutionTrace
import execution.domain.vo.trace.VerificationTrace
import execution.port.outgoing.TestResultPublisher
import execution.port.outgoing.TraceSink
import execution.spi.interceptor.ScenarioInterceptor
import execution.spi.trace.ScenarioTrace
import java.time.Clock

class AuditingInterceptor(
    private val traceSink: TraceSink,
    private val resultPublisher: TestResultPublisher,
    private val options: UserControlOptions,
    private val clock: Clock,
    private val workerId: Int,
) : ScenarioInterceptor {
    override fun intercept(chain: ScenarioInterceptor.Chain): List<AssertionRecord> =
        with(chain.context) {
            trace.decisions.forEach(traceSink::emit)

            val startTime = clock.millis()
            var executionError: Throwable? = null
            val records = mutableListOf<AssertionRecord>()

            try {
                // 2. [WHEN] Proceed with the execution chain.
                // 'this' refers to 'chain.context' due to the 'with' scope function.
                val result = chain.proceed(this)
                records.addAll(result)

                val durationMs = clock.millis() - startTime

                // Log execution metadata (arguments, duration).
                traceSink.emit(
                    ExecutionTrace(
                        methodSignature = targetMethod.name,
                        arguments = trace.generatedArguments.map { it.toString() },
                        durationMs = durationMs,
                        timestamp = clock.millis(),
                    ),
                )

                // 3. [THEN] Log verification results.
                result.forEach { record ->
                    traceSink.emit(
                        VerificationTrace(
                            rule = record.ruleName,
                            status = record.status,
                            detail = record.message,
                            timestamp = clock.millis(),
                        ),
                    )
                }

                return records
            } catch (e: Throwable) {
                executionError = e
                traceSink.emit(
                    ExceptionTrace(
                        exceptionType = e.javaClass.name,
                        message = e.message.orEmpty(),
                        stackTraceElements = e.stackTrace.take(15),
                        timestamp = clock.millis(),
                    ),
                )
                throw e
            } finally {
                publishFinalReport(trace, records, executionError, startTime)

                traceSink.reset()
            }
        }

    /**
     * Determines the final [TestStatus] (Passed, AssertionFailed, or ExecutionError)
     * and publishes the result event.
     */
    private fun publishFinalReport(
        trace: ScenarioTrace,
        records: List<AssertionRecord>,
        error: Throwable?,
        startTime: Long,
    ) {
        // 1. Determine the rich status object based on priority: Error > Failure > Pass
        val failedRecord = records.firstOrNull { it.status == AssertionStatus.FAILED }

        val status: TestStatus =
            when {
                error != null -> TestStatus.ExecutionError(error)
                failedRecord != null ->
                    TestStatus.AssertionFailed(
                        message = failedRecord.message,
                        expected = failedRecord.expected,
                        actual = failedRecord.actual,
                    )

                else -> TestStatus.Passed
            }

        // 2. Snapshot strategy: Save if not passed OR trace mode is enabled.
        val shouldSnapshot = status !is TestStatus.Passed || options.traceMode

        val journalPath =
            shouldSnapshot
                .takeIf { it }
                ?.let {
                    // "failures" for Error/Failed, "traces" for passed tests with trace mode on
                    val subDir = if (status !is TestStatus.Passed) "failures" else "traces"
                    traceSink.snapshotTo("$subDir/run-${trace.runId}.log")
                }.orEmpty()

        resultPublisher.publish(
            TestResultEvent(
                runId = trace.runId,
                workerId = workerId,
                status = status,
                durationMs = clock.millis() - startTime,
                journalPath = journalPath,
                timestamp = clock.millis(),
            ),
        )
    }
}

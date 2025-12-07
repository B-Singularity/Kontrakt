package execution.domain.aggregate

import common.reflection.unwrapped
import discovery.api.KontraktInternalException
import discovery.domain.aggregate.TestSpecification
import execution.api.TestScenarioExecutor
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import execution.domain.service.TestInstanceFactory
import execution.domain.vo.AssertionRecord
import execution.domain.vo.TestResult
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

class TestExecution(
    private val specification: TestSpecification,
    private val instanceFactory: TestInstanceFactory,
    private val scenarioExecutor: TestScenarioExecutor
) {
    private val logger = KotlinLogging.logger {}

    private val executed = AtomicBoolean(false)


    fun execute(): TestResult {
        if (!executed.compareAndSet(false, true)) {
            throw KontraktInternalException("TestExecution instance cannot be reused. It has already been executed.")
        }

        var finalStatus: TestStatus
        var records: List<AssertionRecord> = emptyList()

        val kotlinDuration = measureTime {
            try {
                val context = instanceFactory.create(specification)
                records = scenarioExecutor.executeScenarios(context)
                val failedRecords = records.filter { it.status == AssertionStatus.FAILED }

                finalStatus = if (failedRecords.isNotEmpty()) {
                    val firstFailure = failedRecords.first()
                    TestStatus.AssertionFailed(
                        message = firstFailure.message,
                        expected = firstFailure.expected,
                        actual = firstFailure.actual,
                    )
                } else {
                    TestStatus.Passed
                }
            } catch (e: Throwable) {
                val cause = e.unwrapped

                logger.error(cause) { "Test execution crashed for target: ${specification.target.displayName}" }
                finalStatus = TestStatus.ExecutionError(cause = cause)
            }
        }


        return TestResult(
            target = specification.target,
            finalStatus = finalStatus,
            duration = kotlinDuration.toJavaDuration(),
            assertionRecords = records,
        )
    }
}
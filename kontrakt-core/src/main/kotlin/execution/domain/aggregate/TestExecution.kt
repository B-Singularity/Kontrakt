package execution.domain.aggregate

import discovery.domain.aggregate.TestSpecification
import execution.domain.AssertionStatus
import execution.domain.TestStatus
import execution.domain.entity.TestContext
import execution.domain.vo.AssertionRecord
import execution.domain.vo.TestResult
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

class TestExecution(
    private val specification: TestSpecification,
    private val mockingEngine: MockingEngine,
    private val scenarioExceutor: TestScenarioExcutor
) {
    private val logger = KotlinLogging.logger {}

    private enum class Lifecycle { PENDING, EXECUTED }
    private var lifecycle: Lifecycle = Lifecycle.PENDING

    fun execute(): TestResult {
        require(lifecycle == Lifecycle.PENDING) { "TestExecution has already been executed." }
        this.lifecycle = Lifecycle.EXECUTED

        var finalStatus: TestStatus
        var records: List<AssertionRecord> = emptyList()
        var duration: Duration

        try {
            val kotlinDuration = measureTime {
                val context = TestContext(specification, mockingEngine)
                context.prepare()

                records = scenarioExecutor.executeScenarios(context)
            }
            duration = kotlinDuration.toJavaDuration()

            val failedRecords = records.filter( it.status == AssertionStatus.FAILED)
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
            duration = Duration.ZERO
            logger.error(e) { "Test execution crashed for target: ${specification.target.displayName}" }

            finalStatus = TestStatus.ExecutionError(cause = e)
        }

        return TestResult(
            target = specification.target,
            finalStatus = finalStatus,
            duration = duration,
            assertionRecords = records,
        )
    }
}
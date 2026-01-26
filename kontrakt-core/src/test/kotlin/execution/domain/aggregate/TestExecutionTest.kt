package execution.domain.aggregate

import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.adapter.trace.WorkerTraceSinkPool
import execution.domain.entity.EphemeralTestContext
import execution.domain.service.generation.TestInstanceFactory
import execution.domain.vo.config.AuditDepth
import execution.domain.vo.config.AuditPolicy
import execution.domain.vo.config.DeterminismPolicy
import execution.domain.vo.config.ExecutionPolicy
import execution.domain.vo.result.ExecutionResult
import execution.domain.vo.result.TestStatus
import execution.domain.vo.verification.AssertionRecord
import execution.domain.vo.verification.AssertionRule
import execution.domain.vo.verification.AssertionStatus
import execution.port.incoming.TestScenarioExecutor
import execution.port.outgoing.TestResultPublisher
import execution.port.outgoing.TraceSink
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TestExecutionTest {

    // Mocks
    private val spec = mockk<TestSpecification>(relaxed = true)
    private val instanceFactory = mockk<TestInstanceFactory>()
    private val scenarioExecutor = mockk<TestScenarioExecutor>()
    private val traceSinkPool = mockk<WorkerTraceSinkPool>()
    private val resultPublisher = mockk<TestResultPublisher>(relaxed = true)
    private val traceSink = mockk<TraceSink>(relaxed = true)
    private val assertionRule = mockk<AssertionRule>(relaxed = true)

    // Clock Mock (Fixed Time)
    private val startTime = Instant.parse("2024-01-01T10:00:00Z")
    private val clock = Clock.fixed(startTime, ZoneId.of("UTC"))

    // Configuration
    private val executionPolicy = ExecutionPolicy(
        determinism = DeterminismPolicy(seed = 1234L),
        auditing = AuditPolicy(depth = AuditDepth.SIMPLE)
    )

    // System Under Test
    private lateinit var sut: TestExecution

    @BeforeEach
    fun setUp() {
        // [Verified] WorkerId 수정으로 인해 any()가 음수 ID를 생성해도
        // private constructor를 통해(검증 없이) 생성되므로 예외가 발생하지 않음.
        every { traceSinkPool.getSink(any()) } returns traceSink

        sut = TestExecution(
            spec = spec,
            instanceFactory = instanceFactory,
            scenarioExecutor = scenarioExecutor,
            traceSinkPool = traceSinkPool,
            resultPublisher = resultPublisher,
            clock = clock,
            executionPolicy = executionPolicy,
        )
    }

    // =========================================================================
    // 1. Happy Path & Pipeline Integration
    // =========================================================================

    @Test
    fun `execute - runs pipeline and returns Passed result when no assertions fail`() {
        // Given
        val context = mockk<EphemeralTestContext>(relaxed = true)
        every { instanceFactory.create(any(), any()) } returns context

        val successRecord = AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = assertionRule,
            message = "Match",
            expected = "A",
            actual = "A"
        )

        val executionResult = ExecutionResult(
            records = listOf(successRecord),
            seed = 1234L
        )
        every { scenarioExecutor.executeScenarios(context) } returns executionResult

        // When
        val result = sut.execute()

        // Then
        assertThat(result.finalStatus).isEqualTo(TestStatus.Passed)
        assertThat(result.assertionRecords).containsExactly(successRecord)

        verify(exactly = 1) { instanceFactory.create(spec, clock) }
        verify(exactly = 1) { traceSinkPool.getSink(any()) }
        verify(exactly = 1) { scenarioExecutor.executeScenarios(context) }
    }

    // =========================================================================
    // 2. Failure Handling (Assertion Failed)
    // =========================================================================

    @Test
    fun `execute - returns AssertionFailed when at least one record fails`() {
        // Given
        val context = mockk<EphemeralTestContext>(relaxed = true)
        every { instanceFactory.create(any(), any()) } returns context

        val passRecord = AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = assertionRule,
            message = "Ok",
            expected = "A",
            actual = "A"
        )

        val failRecord = AssertionRecord(
            status = AssertionStatus.FAILED,
            rule = assertionRule,
            message = "Mismatch",
            expected = "B",
            actual = "C"
        )

        val executionResult = ExecutionResult(
            records = listOf(passRecord, failRecord),
            seed = 1234L
        )
        every { scenarioExecutor.executeScenarios(context) } returns executionResult

        // When
        val result = sut.execute()

        // Then
        assertThat(result.finalStatus).isInstanceOf(TestStatus.AssertionFailed::class.java)

        val status = result.finalStatus as TestStatus.AssertionFailed
        assertThat(status.expected).isEqualTo("B")
        assertThat(status.actual).isEqualTo("C")
        assertThat(status.message).isEqualTo("Mismatch")
    }

    // =========================================================================
    // 3. Setup / Infrastructure Failure (Catch Block Coverage)
    // =========================================================================

    @Test
    fun `execute - catches exception during setup and returns ExecutionError`() {
        // Given: Factory throws an exception (e.g., DI failure)
        val exception = RuntimeException("DI Failed")
        every { instanceFactory.create(any(), any()) } throws exception

        // When
        val result = sut.execute()

        // Then
        assertThat(result.finalStatus).isInstanceOf(TestStatus.ExecutionError::class.java)

        val status = result.finalStatus as TestStatus.ExecutionError
        assertThat(status.cause).isEqualTo(exception)

        verify(exactly = 0) { scenarioExecutor.executeScenarios(any()) }
    }

    // =========================================================================
    // 4. Lifecycle Safety (Double Execution)
    // =========================================================================

    @Test
    fun `execute - prevents double execution`() {
        // Given: A successful first run
        val context = mockk<EphemeralTestContext>(relaxed = true)
        every { instanceFactory.create(any(), any()) } returns context

        val emptyResult = ExecutionResult(emptyList(), seed = 1234L)
        every { scenarioExecutor.executeScenarios(any()) } returns emptyResult

        sut.execute() // First call

        // When & Then: Second call should throw
        assertThatThrownBy {
            sut.execute()
        }.isInstanceOf(KontraktInternalException::class.java)
            .hasMessageContaining("TestExecution instance cannot be reused")
    }

    // =========================================================================
    // 5. Missing Branch Coverage (Seed & TraceMode)
    // =========================================================================

    @Test
    fun `execute - generates random seed when deterministic seed is missing`() {
        // Given: Policy with null seed (triggers Random.nextLong())
        val randomPolicy = ExecutionPolicy(
            determinism = DeterminismPolicy(seed = null),
            auditing = AuditPolicy(depth = AuditDepth.SIMPLE)
        )

        // Re-initialize SUT with the new policy
        sut = TestExecution(
            spec = spec,
            instanceFactory = instanceFactory,
            scenarioExecutor = scenarioExecutor,
            traceSinkPool = traceSinkPool,
            resultPublisher = resultPublisher,
            clock = clock,
            executionPolicy = randomPolicy
        )

        val context = mockk<EphemeralTestContext>(relaxed = true)
        every { instanceFactory.create(any(), any()) } returns context

        val successRecord = AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = assertionRule,
            message = "Match",
            expected = "A",
            actual = "A"
        )
        val executionResult = ExecutionResult(records = listOf(successRecord), seed = 9999L)
        every { scenarioExecutor.executeScenarios(context) } returns executionResult

        // When
        val result = sut.execute()

        // Then: Should execute successfully without NPE
        assertThat(result.finalStatus).isEqualTo(TestStatus.Passed)
    }

    @Test
    fun `execute - enables trace mode when audit depth is EXPLAINABLE`() {
        // Given: Policy with EXPLAINABLE depth (triggers traceMode = true)
        val tracePolicy = ExecutionPolicy(
            determinism = DeterminismPolicy(seed = 1234L),
            auditing = AuditPolicy(depth = AuditDepth.EXPLAINABLE)
        )

        sut = TestExecution(
            spec = spec,
            instanceFactory = instanceFactory,
            scenarioExecutor = scenarioExecutor,
            traceSinkPool = traceSinkPool,
            resultPublisher = resultPublisher,
            clock = clock,
            executionPolicy = tracePolicy
        )

        val context = mockk<EphemeralTestContext>(relaxed = true)
        every { instanceFactory.create(any(), any()) } returns context

        val successRecord = AssertionRecord(
            status = AssertionStatus.PASSED,
            rule = assertionRule,
            message = "Match",
            expected = "A",
            actual = "A"
            // Note: In EXPLAINABLE mode, the system might capture 'SourceLocation',
            // causing a mismatch with this default 'NotCaptured' record.
        )
        val executionResult = ExecutionResult(records = listOf(successRecord), seed = 1234L)
        every { scenarioExecutor.executeScenarios(context) } returns executionResult

        // When
        val result = sut.execute()

        // Then
        assertThat(result.finalStatus).isEqualTo(TestStatus.Passed)

        // Use recursive comparison ignoring 'location' to verify logic
        // while bypassing the side-effect of traceMode (location capturing).
        assertThat(result.assertionRecords)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("location")
            .containsExactly(successRecord)
    }
}
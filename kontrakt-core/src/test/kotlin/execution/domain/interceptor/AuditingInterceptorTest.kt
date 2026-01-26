package execution.domain.interceptor

import execution.domain.entity.EphemeralTestContext
import execution.domain.service.VerdictDecider
import execution.domain.vo.config.AuditDepth
import execution.domain.vo.config.AuditPolicy
import execution.domain.vo.config.LogRetention
import execution.domain.vo.context.WorkerId
import execution.domain.vo.result.TestResultEvent
import execution.domain.vo.result.TestStatus
import execution.domain.vo.trace.DesignDecision
import execution.domain.vo.trace.ExceptionTrace
import execution.domain.vo.trace.ExecutionTrace
import execution.domain.vo.trace.TestVerdict
import execution.domain.vo.trace.TracePhase
import execution.domain.vo.trace.VerificationTrace
import execution.domain.vo.verification.AssertionRecord
import execution.domain.vo.verification.AssertionRule
import execution.domain.vo.verification.AssertionStatus
import execution.port.outgoing.ScenarioInterceptor
import execution.port.outgoing.ScenarioTrace
import execution.port.outgoing.TestResultPublisher
import execution.port.outgoing.TraceSink
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import java.time.Clock

class AuditingInterceptorTest : ScenarioInterceptorContract {

    // Mocks
    private val traceSink = mockk<TraceSink>(relaxed = true)
    private val resultPublisher = mockk<TestResultPublisher>(relaxed = true)
    private val clock = mockk<Clock>()
    private val verdictDecider = mockk<VerdictDecider>()
    private val chain = mockk<ScenarioInterceptor.Chain>()
    private val context = mockk<EphemeralTestContext>(relaxed = true)
    private val trace = mockk<ScenarioTrace>(relaxed = true)

    // Configuration Defaults
    private val defaultPolicy = AuditPolicy(LogRetention.ON_FAILURE, AuditDepth.SIMPLE)
    private val seed = 1234L
    private val workerId = WorkerId.of(1)
    private val currentTime = 1000L

    private lateinit var sut: AuditingInterceptor

    @BeforeEach
    fun setUp() {
        every { clock.millis() } returns currentTime
        every { chain.context } returns context
        every { context.trace } returns trace
        every { context.targetMethod } returns Object::class.java.methods.first() // Safe default
        every { context.specification.target.fullyQualifiedName } returns "com.example.TestTarget"

        // Default Verdict
        every { verdictDecider.decide(any(), any()) } returns TestStatus.Passed

        // SUT Initialization
        sut = AuditingInterceptor(
            traceSink = traceSink,
            resultPublisher = resultPublisher,
            policy = defaultPolicy,
            seed = seed,
            clock = clock,
            workerId = workerId,
            verdictDecider = verdictDecider
        )
    }

    override fun createSut(): ScenarioInterceptor = sut

    // =========================================================================
    // 1. Log Diet & Event Flushing (AuditDepth)
    // =========================================================================

    @Test
    fun `intercept - filters out DESIGN logs when depth is SIMPLE`() {
        // Given: Trace contains both Execution(Keep) and Design(Drop) events
        val execEvent = mockk<ExecutionTrace> { every { phase } returns TracePhase.EXECUTION }
        val designEvent = mockk<DesignDecision> { every { phase } returns TracePhase.DESIGN }

        every { trace.events } returns listOf(execEvent, designEvent)
        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then: Only Execution event should be emitted
        verify(exactly = 1) { traceSink.emit(execEvent) }
        verify(exactly = 0) { traceSink.emit(designEvent) }
    }

    @Test
    fun `intercept - includes DESIGN logs when depth is EXPLAINABLE`() {
        // Given: Policy is EXPLAINABLE
        val explainablePolicy = AuditPolicy(LogRetention.ON_FAILURE, AuditDepth.EXPLAINABLE)
        sut = AuditingInterceptor(traceSink, resultPublisher, explainablePolicy, seed, clock, workerId, verdictDecider)

        val designEvent = mockk<DesignDecision> { every { phase } returns TracePhase.DESIGN }
        every { trace.events } returns listOf(designEvent)
        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then: Design event MUST be emitted
        verify(exactly = 1) { traceSink.emit(designEvent) }
    }

    @Test
    fun `intercept - skips flushing if trace is empty`() {
        // Given: Empty trace events
        every { trace.events } returns emptyList()
        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then
        verify(exactly = 0) {
            traceSink.emit(match { it !is TestVerdict })
        }
    }

    // =========================================================================
    // 2. Verification & Result Publishing
    // =========================================================================

    @Test
    fun `intercept - emits verification traces and final result`() {
        // Given
        val record =
            AssertionRecord(AssertionStatus.PASSED, mockk<AssertionRule> { every { key } returns "Rule" }, "OK")
        every { chain.proceed(any()) } returns listOf(record)

        // When
        sut.intercept(chain)

        // Then 1: Verify VerificationTrace emission
        verify {
            traceSink.emit(match<VerificationTrace> {
                it.status == AssertionStatus.PASSED && it.detail == "OK"
            })
        }

        // Then 2: Verify Final Verdict emission
        verify {
            traceSink.emit(any<TestVerdict>())
        }

        // Then 3: Verify Publisher notification
        verify {
            resultPublisher.publish(match<TestResultEvent> {
                it.status == TestStatus.Passed
            })
        }

        // Then 4: Verify Sink Reset (Cleanup)
        verify { traceSink.reset() }
    }

    // =========================================================================
    // 3. Snapshot Strategy (LogRetention)
    // =========================================================================

    @Test
    fun `intercept - snapshots log on failure when retention is ON_FAILURE`() {
        // Given: Verdict is Failed
        every { verdictDecider.decide(any(), any()) } returns TestStatus.AssertionFailed("Fail", "A", "B")
        every { trace.runId } returns "run-123"
        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then: Should call snapshotTo with failures directory
        verify(exactly = 1) { traceSink.snapshotTo("failures/run-run-123.log") }
    }

    @Test
    fun `intercept - skips snapshot on success when retention is ON_FAILURE`() {
        // Given: Verdict is Passed
        every { verdictDecider.decide(any(), any()) } returns TestStatus.Passed
        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then: Should NOT call snapshotTo
        verify(exactly = 0) { traceSink.snapshotTo(any()) }
    }

    @Test
    fun `intercept - always snapshots when retention is ALWAYS`() {
        // Given: Policy ALWAYS, Verdict Passed
        val alwaysPolicy = AuditPolicy(LogRetention.ALWAYS, AuditDepth.SIMPLE)
        sut = AuditingInterceptor(traceSink, resultPublisher, alwaysPolicy, seed, clock, workerId, verdictDecider)

        every { trace.runId } returns "run-999"
        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then: Should snapshot (to traces directory for success)
        verify(exactly = 1) { traceSink.snapshotTo("traces/run-run-999.log") }
    }

    @Test
    fun `intercept - never snapshots when retention is NONE`() {
        // Given: Policy NONE, Verdict Failed
        val nonePolicy = AuditPolicy(LogRetention.NONE, AuditDepth.SIMPLE)
        sut = AuditingInterceptor(traceSink, resultPublisher, nonePolicy, seed, clock, workerId, verdictDecider)

        every { verdictDecider.decide(any(), any()) } returns TestStatus.ExecutionError(RuntimeException())
        every { chain.proceed(any()) } throws RuntimeException("Error") // Triggers error flow

        // When
        try {
            sut.intercept(chain)
        } catch (e: Exception) {
        }

        // Then
        verify(exactly = 0) { traceSink.snapshotTo(any()) }
    }

    // =========================================================================
    // 4. Exception Handling & Edge Cases
    // =========================================================================

    @Test
    fun `intercept - catches exception, emits ExceptionTrace, and rethrows`() {
        // Given: Chain throws exception
        val error = RuntimeException("Boom")
        every { chain.proceed(any()) } throws error
        every { verdictDecider.decide(error, any()) } returns TestStatus.ExecutionError(error)

        // When & Then
        assertThatThrownBy {
            sut.intercept(chain)
        }.isSameAs(error)

        // Verify: ExceptionTrace emitted BEFORE rethrow
        verify {
            traceSink.emit(match<ExceptionTrace> {
                it.message == "Boom"
            })
        }

        // Verify: Finally block executed (Result published as Error)
        verify {
            resultPublisher.publish(match { it.status is TestStatus.ExecutionError })
        }
    }

    @Test
    fun `intercept - handles broken toString of targetMethod safely`() {
        // Given: targetMethod.toString() throws Exception (Edge Case)
        val brokenMethod = mockk<Method>()
        every { brokenMethod.toString() } throws RuntimeException("Method toString failed")
        every { context.targetMethod } returns brokenMethod
        every { context.specification.target.fullyQualifiedName } returns "Fallback.ClassName"

        every { chain.proceed(any()) } returns emptyList()

        // When
        sut.intercept(chain)

        // Then: Should use fallback name in result event
        verify {
            resultPublisher.publish(match { it.testName == "Fallback.ClassName" })
        }
    }

    @Test
    fun `intercept - handles exception with null message by defaulting to empty string`() {
        val exceptionWithNullMessage = RuntimeException()
        every { chain.proceed(any()) } throws exceptionWithNullMessage

        every { verdictDecider.decide(exceptionWithNullMessage, any()) } returns TestStatus.ExecutionError(
            exceptionWithNullMessage
        )

        assertThatThrownBy {
            sut.intercept(chain)
        }.isSameAs(exceptionWithNullMessage)

        verify {
            traceSink.emit(match<ExceptionTrace> {
                it.message == "" &&
                        it.exceptionType == RuntimeException::class.java.name
            })
        }
    }
}
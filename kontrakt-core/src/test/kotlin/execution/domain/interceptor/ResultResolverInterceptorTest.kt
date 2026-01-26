package execution.domain.interceptor

import common.util.extractSourceLocation
import common.util.sanitizeStackTrace
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveredTestTarget
import exception.ContractViolationException
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.domain.vo.verification.AssertionRecord
import execution.domain.vo.verification.AssertionRule
import execution.domain.vo.verification.AssertionStatus
import execution.domain.vo.verification.ConfigurationErrorRule
import execution.domain.vo.verification.SourceLocation
import execution.domain.vo.verification.StandardAssertion
import execution.domain.vo.verification.SystemErrorRule
import execution.domain.vo.verification.UserExceptionRule
import execution.port.outgoing.ScenarioInterceptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResultResolverInterceptorTest {

    private val spec = mockk<TestSpecification>()
    private val chain = mockk<ScenarioInterceptor.Chain>()
    private val targetClass = SampleTarget::class
    private val targetName = "com.example.SampleTarget"

    @BeforeEach
    fun setUp() {
        // Mocking the Test Target Information
        val testTarget = mockk<DiscoveredTestTarget> {
            every { kClass } returns targetClass
            every { fullyQualifiedName } returns targetName
            every { displayName } returns "Sample Target"
        }
        every { spec.target } returns testTarget

        // Default context mock
        every { chain.context } returns mockk(relaxed = true)

        // [Critical Fix] Mock static extension functions from ExceptionUtils.kt
        // This prevents the real 'sanitizeStackTrace' from calling 'getStackTrace()' on our Mock Exceptions.
        mockkStatic("common.util.ExceptionUtilsKt")

        // Default behavior for Utils:
        // 1. Location is Unknown by default (override in specific tests if needed)
        every { any<Throwable>().extractSourceLocation(any()) } returns SourceLocation.Unknown
        // 2. Sanitize does nothing but return the exception itself (bypassing logic)
        every { any<Throwable>().sanitizeStackTrace(any()) } answers { firstArg() }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    // =========================================================================
    // 1. Success Path Tests (Trace Mode Logic)
    // =========================================================================

    @Test
    fun `intercept - success without traceMode leaves NotCaptured locations as is`() {
        // Given: TraceMode = false
        val sut = ResultResolverInterceptor(spec, traceMode = false)

        val rawRecord = createPassedRecord(SourceLocation.NotCaptured)
        every { chain.proceed(any()) } returns listOf(rawRecord)

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0].location).isInstanceOf(SourceLocation.NotCaptured::class.java)
    }

    @Test
    fun `intercept - success with traceMode enriches NotCaptured to Approximate`() {
        // Given: TraceMode = true
        val sut = ResultResolverInterceptor(spec, traceMode = true)

        val rawRecord = createPassedRecord(SourceLocation.NotCaptured)
        every { chain.proceed(any()) } returns listOf(rawRecord)

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        val location = results[0].location

        assertThat(location).isInstanceOf(SourceLocation.Approximate::class.java)
        val approximate = location as SourceLocation.Approximate
        assertThat(approximate.className).isEqualTo(targetName)
        assertThat(approximate.displayName).isEqualTo("Sample Target")
    }

    @Test
    fun `intercept - success with traceMode preserves existing Exact locations`() {
        // Given: TraceMode = true, but record already has Exact location
        val sut = ResultResolverInterceptor(spec, traceMode = true)

        val exactLocation = SourceLocation.Exact("File.kt", 10, "Class", "Method")
        val rawRecord = createPassedRecord(exactLocation)
        every { chain.proceed(any()) } returns listOf(rawRecord)

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results[0].location).isEqualTo(exactLocation)
    }

    // =========================================================================
    // 2. Failure Path Tests (Exception Mapping & Blame Assignment)
    // =========================================================================

    @Test
    fun `intercept - maps ContractViolationException to FAILED status with constraint rule`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val rule = mockk<AssertionRule>(relaxed = true)
        val exception = ContractViolationException(rule, "Value must be positive")

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        val record = results[0]
        assertThat(record.status).isEqualTo(AssertionStatus.FAILED)
        assertThat(record.rule).isEqualTo(rule)
        assertThat(record.message).contains("Value must be positive")
    }

    @Test
    fun `intercept - maps AssertionError to FAILED status with StandardAssertion rule`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val failureMessage = "Expected 1 but got 2"
        val exception = AssertionError(failureMessage)

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        val record = results[0]
        assertThat(record.status).isEqualTo(AssertionStatus.FAILED)
        assertThat(record.rule).isEqualTo(StandardAssertion)
        assertThat(record.message).contains(failureMessage)
    }

    @Test
    fun `intercept - maps KontraktConfigurationException to ConfigurationErrorRule`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val exception = KontraktConfigurationException("Invalid setup")

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        val record = results[0]
        assertThat(record.rule).isEqualTo(ConfigurationErrorRule)
        assertThat(record.message).contains("Invalid setup")
    }

    @Test
    fun `intercept - maps KontraktInternalException to SystemErrorRule`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val exception = KontraktInternalException("Something broke inside")

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        val record = results[0]
        assertThat(record.rule).isInstanceOf(SystemErrorRule::class.java)
        assertThat(record.message).contains("Internal Framework Error")
    }

    @Test
    fun `intercept - maps generic RuntimeException to UserExceptionRule`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val exception = NullPointerException("Oops")

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results).hasSize(1)
        val record = results[0]
        assertThat(record.rule).isInstanceOf(UserExceptionRule::class.java)
        assertThat((record.rule as UserExceptionRule).exceptionType).isEqualTo("NullPointerException")
        assertThat(record.message).contains("Unexpected Exception: Oops")
    }

    // =========================================================================
    // 3. Missing Branch Coverage (Elvis Operators & Null Messages)
    // =========================================================================

    @Test
    fun `intercept - uses fallback message when ContractViolationException message is null`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val rule = mockk<AssertionRule>(relaxed = true)

        // Mocking exception with NULL message
        val exception = mockk<ContractViolationException>()
        every { exception.rule } returns rule
        every { exception.message } returns null
        every { exception.cause } returns null
        // Important: Mock 'sanitizeStackTrace' was already handled in setUp, preventing the crash.

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results[0].message).isEqualTo("Contract violated")
    }

    @Test
    fun `intercept - uses fallback message when AssertionError message is null`() {
        // Given
        val sut = ResultResolverInterceptor(spec)

        // Mocking exception with NULL message
        val exception = mockk<AssertionError>()
        every { exception.message } returns null
        every { exception.cause } returns null

        every { chain.proceed(any()) } throws exception

        // When
        val results = sut.intercept(chain)

        // Then
        assertThat(results[0].message).isEqualTo("Assertion failed")
    }

    // =========================================================================
    // 4. Location Extraction Tests (Mock Integration)
    // =========================================================================

    @Test
    fun `intercept - extracts exact source location from stack trace`() {
        // Given
        val sut = ResultResolverInterceptor(spec)
        val exception = RuntimeException("Boom")
        every { chain.proceed(any()) } throws exception

        // Override default mock to return a specific Exact location
        val expectedLocation = SourceLocation.Exact(targetName, 10, targetName, "throwError")
        every { any<Throwable>().extractSourceLocation(any()) } returns expectedLocation

        // When
        val results = sut.intercept(chain)

        // Then
        val location = results[0].location
        assertThat(location).isEqualTo(expectedLocation)
    }

    // Helper Class
    class SampleTarget

    private fun createPassedRecord(location: SourceLocation) = AssertionRecord(
        status = AssertionStatus.PASSED,
        rule = StandardAssertion,
        message = "OK",
        location = location
    )
}
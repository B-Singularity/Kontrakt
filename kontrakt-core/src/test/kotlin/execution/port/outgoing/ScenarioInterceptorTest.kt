package execution.domain.interceptor

import execution.domain.entity.EphemeralTestContext
import execution.domain.vo.verification.AssertionRecord
import execution.port.outgoing.ScenarioInterceptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * Defines the behavioral contract for [ScenarioInterceptor] implementations.
 */
interface ScenarioInterceptorContract {

    fun createSut(): ScenarioInterceptor

    // Helper to create a mock chain
    fun createChain(): ScenarioInterceptor.Chain {
        val chain = mockk<ScenarioInterceptor.Chain>()
        val context = mockk<EphemeralTestContext>(relaxed = true)
        every { chain.context } returns context
        every { chain.proceed(any()) } returns emptyList()
        return chain
    }

    @Test
    fun `intercept - must proceed the chain`() {
        val sut = createSut()
        val chain = createChain()

        sut.intercept(chain)

        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `intercept - returns the records from the chain`() {
        val sut = createSut()
        val chain = createChain()
        val expectedRecords = listOf(mockk<AssertionRecord>(relaxed = true))
        every { chain.proceed(any()) } returns expectedRecords

        val result = sut.intercept(chain)

        assertThat(result).isEqualTo(expectedRecords)
    }

    @Test
    fun `intercept - propagates exceptions thrown by the chain`() {
        val sut = createSut()
        val chain = createChain()
        val expectedError = RuntimeException("Chain failed")
        every { chain.proceed(any()) } throws expectedError

        assertThatThrownBy {
            sut.intercept(chain)
        }.isSameAs(expectedError)
    }
}
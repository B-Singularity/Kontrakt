package execution.adapter.reporting

import exception.KontraktInternalException
import execution.domain.vo.result.TestResultEvent
import execution.port.outgoing.TestResultPublisher
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class BroadcastingResultPublisherTest {

    private val publisher1 = mockk<TestResultPublisher>(name = "Publisher1", relaxed = true)
    private val publisher2 = mockk<TestResultPublisher>(name = "Publisher2", relaxed = true)
    private val event = mockk<TestResultEvent>()
    private val errorHandler = mockk<(String, Throwable) -> Unit>(relaxed = true)

    @Test
    fun `initialization - should handle empty list safely without throwing exception`() {
        // When & Then
        assertDoesNotThrow {
            BroadcastingResultPublisher(emptyList())
        }
    }

    @Test
    fun `publish - should fan-out event to all subscribers sequentially`() {
        // Given
        val sut = BroadcastingResultPublisher(listOf(publisher1, publisher2))

        // When
        sut.publish(event)

        // Then
        verify(exactly = 1) { publisher1.publish(event) }
        verify(exactly = 1) { publisher2.publish(event) }
        confirmVerified(publisher1, publisher2)
    }

    @Test
    fun `publish - should isolate failures and continue to next subscriber when one fails`() {
        // Given
        val sut = BroadcastingResultPublisher(listOf(publisher1, publisher2), errorHandler)

        // Fail the first one
        val expectedException = RuntimeException("Connection Refused")
        every { publisher1.publish(any()) } throws expectedException
        // Second one succeeds (relaxed mock)

        // When
        sut.publish(event)

        // Then
        // 1. Verify isolation: Publisher 2 was still called
        verify(exactly = 1) { publisher1.publish(event) }
        verify(exactly = 1) { publisher2.publish(event) }

        // 2. Verify error handling hook
        verify(exactly = 1) {
            errorHandler.invoke(
                any(),
                match { it is KontraktInternalException && it.cause == expectedException }
            )
        }
    }

    @Test
    fun `close - should propagate close signal to all subscribers`() {
        // Given
        val sut = BroadcastingResultPublisher(listOf(publisher1, publisher2))

        // When
        sut.close()

        // Then
        verify(exactly = 1) { publisher1.close() }
        verify(exactly = 1) { publisher2.close() }
    }

    @Test
    fun `close - should isolate failures during shutdown and ensure all resources try to close`() {
        // Given
        val sut = BroadcastingResultPublisher(listOf(publisher1, publisher2), errorHandler)

        // Fail the first close
        val expectedException = IllegalStateException("Already closed")
        every { publisher1.close() } throws expectedException
        every { publisher2.close() } just runs

        // When
        sut.close()

        // Then
        verify(exactly = 1) { publisher1.close() }
        verify(exactly = 1) { publisher2.close() }

        verify(exactly = 1) {
            errorHandler.invoke(
                any(),
                match {
                    it is KontraktInternalException &&
                            it.message!!.contains("failed during 'close'")
                }
            )
        }
    }

    @Test
    fun `error handling - should use system error stream if no handler is provided`() {
        // Given
        val sut = BroadcastingResultPublisher(listOf(publisher1))
        every { publisher1.publish(any()) } throws RuntimeException("Boom")

        // When & Then
        // Should catch internal exception and print to stderr (side effect), avoiding crash
        assertDoesNotThrow {
            sut.publish(event)
        }
    }

    @Test
    fun `handleFailure - should fallback to UnknownPublisher name when class is anonymous`() {
        val anonymousPublisher = object : TestResultPublisher {
            override fun publish(event: TestResultEvent) {
                throw RuntimeException("Anonymous Fail")
            }

            override fun close() {}
        }

        val sut = BroadcastingResultPublisher(listOf(anonymousPublisher), errorHandler)

        sut.publish(event)

        verify(exactly = 1) {
            errorHandler.invoke(
                eq("UnknownPublisher"),
                any()
            )
        }
    }
}
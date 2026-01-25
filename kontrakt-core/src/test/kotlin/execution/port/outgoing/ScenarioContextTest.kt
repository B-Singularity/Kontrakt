package execution.port.outgoing

import exception.KontraktConfigurationException
import execution.adapter.mockito.MockitoScenarioContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito

/**
 * Defines the contract requirements for any [ScenarioContext] implementation.
 *
 * This interface utilizes the "Interface-based Testing" pattern (supported by JUnit 5).
 * All test logic is defined in default methods, allowing different adapters (Mockito, Mockk, etc.)
 * to reuse the same rigorous test suite by simply implementing the abstract factory methods.
 *
 * It ensures 100% branch coverage, including exception translation logic.
 */
interface ScenarioContextContract {

    /**
     * Factory method to create the System Under Test (SUT).
     */
    fun createSut(): ScenarioContext

    /**
     * Factory method to create a mock collaborator used for verification.
     */
    fun createMockDependency(): TestDependency

    /**
     * A dummy interface used purely for testing stubbing capabilities.
     */
    interface TestDependency {
        fun getString(): String
        fun doAction()
    }

    // =================================================================================================
    // 1. Return Value Stubbing Tests
    // =================================================================================================

    @Test
    @DisplayName("every { ... } returns ... should stub a value successfully (Happy Path)")
    fun `returns - happy path`() {
        // Given
        val sut = createSut()
        val mock = createMockDependency()
        val expectedValue = "Stubbed Response"

        // When
        sut.every { mock.getString() } returns expectedValue

        // Then
        assertThat(mock.getString()).isEqualTo(expectedValue)
    }

    @Test
    @DisplayName("every { ... } returns ... should throw KontraktConfigurationException on invalid usage (Safety Path)")
    fun `returns - safety path`() {
        // Given
        val sut = createSut()

        // When & Then
        // We purposefully pass a non-mock invocation to trigger the internal try-catch block
        // in the adapter, ensuring it translates the library-specific exception into our custom exception.
        assertThatThrownBy {
            sut.every { "Not a mock invocation" } returns "Something"
        }.isInstanceOf(KontraktConfigurationException::class.java)
            .hasMessageContaining("Failed to apply stubbing")
    }

    // =================================================================================================
    // 2. Exception Stubbing Tests
    // =================================================================================================

    @Test
    @DisplayName("every { ... } throws ... should stub an exception successfully (Happy Path)")
    fun `throws - happy path`() {
        // Given
        val sut = createSut()
        val mock = createMockDependency()
        val expectedException = IllegalStateException("Boom")

        // When
        sut.every { mock.doAction() } throws expectedException

        // Then
        assertThatThrownBy { mock.doAction() }
            .isEqualTo(expectedException)
    }

    @Test
    @DisplayName("every { ... } throws ... should throw KontraktConfigurationException on invalid usage (Safety Path)")
    fun `throws - safety path`() {
        // Given
        val sut = createSut()
        val exceptionToThrow = RuntimeException("Error")

        // When & Then
        // Trigger the failure path by attempting to stub a non-mock object.
        assertThatThrownBy {
            sut.every { "Not a mock invocation" } throws exceptionToThrow
        }.isInstanceOf(KontraktConfigurationException::class.java)
            .hasMessageContaining("Failed to stub exception")
    }
}


/**
 * Validates the [MockitoScenarioContext] by implementing the [ScenarioContextContract].
 *
 * This class serves as the concrete execution entry point for the contract tests.
 * It injects the Mockito-specific implementation and dependencies into the
 * interface-based test logic.
 */
class MockitoScenarioContextTest : ScenarioContextContract {

    override fun createSut(): ScenarioContext {
        return MockitoScenarioContext()
    }

    override fun createMockDependency(): ScenarioContextContract.TestDependency {
        return Mockito.mock(ScenarioContextContract.TestDependency::class.java)
    }
}
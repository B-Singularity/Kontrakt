package execution.adapter.state

import exception.KontraktInternalException
import execution.domain.vo.context.ExecutionEnvironment
import execution.port.outgoing.ScenarioControl
import execution.port.outgoing.ScenarioControlContract
import io.mockk.mockk
import kotlinx.coroutines.ThreadContextElement
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.EmptyCoroutineContext

internal class ThreadLocalScenarioControlTest : ScenarioControlContract {

    override fun createSut(): ScenarioControl = ThreadLocalScenarioControl()

    @AfterEach
    fun tearDown() {
        ThreadLocalScenarioControl.clear()
    }

    // =================================================================================================
    // Companion Object Logic Tests (Static Methods)
    // =================================================================================================

    @Test
    fun `bind & get - stores and retrieves the environment for the current thread`() {
        // Given
        val env = mockk<ExecutionEnvironment>(relaxed = true)

        // When
        ThreadLocalScenarioControl.bind(env)

        // Then
        assertThat(ThreadLocalScenarioControl.get()).isSameAs(env)
    }

    @Test
    fun `bind - overwrites existing environment if one is already bound`() {
        // Given (Branch: if (STORAGE.get() != null))
        val env1 = mockk<ExecutionEnvironment>(relaxed = true)
        val env2 = mockk<ExecutionEnvironment>(relaxed = true)

        ThreadLocalScenarioControl.bind(env1)

        // When: Binding again triggers the overwrite branch (remove -> set)
        ThreadLocalScenarioControl.bind(env2)

        // Then
        assertThat(ThreadLocalScenarioControl.get()).isSameAs(env2)
        assertThat(ThreadLocalScenarioControl.get()).isNotSameAs(env1)
    }

    @Test
    fun `get - throws KontraktInternalException when no environment is bound`() {
        // Given (Branch: STORAGE.get() ?: throw)
        ThreadLocalScenarioControl.clear()

        // When & Then
        assertThatThrownBy {
            ThreadLocalScenarioControl.get()
        }.isInstanceOf(KontraktInternalException::class.java)
            .hasMessageContaining("No active ExecutionEnvironment found")
    }

    @Test
    fun `clear - removes the bound environment safely`() {
        // Given
        val env = mockk<ExecutionEnvironment>(relaxed = true)
        ThreadLocalScenarioControl.bind(env)

        // When
        ThreadLocalScenarioControl.clear()

        // Then: Accessing after clear should throw exception
        assertThatThrownBy {
            ThreadLocalScenarioControl.get()
        }.isInstanceOf(KontraktInternalException::class.java)
    }

    @Test
    fun `requireCoroutineContext - returns valid context element when environment is bound`() {
        // Given
        val env = mockk<ExecutionEnvironment>(relaxed = true)
        ThreadLocalScenarioControl.bind(env)

        // When
        val context = ThreadLocalScenarioControl.requireCoroutineContext()

        // Then
        assertThat(context).isNotNull()

        assertThat(context).isInstanceOf(ThreadContextElement::class.java)
        assertThat(context).isNotEqualTo(EmptyCoroutineContext)
    }

    @Test
    fun `requireCoroutineContext - propagates exception if environment is missing`() {
        // Given
        ThreadLocalScenarioControl.clear()

        // When & Then
        assertThatThrownBy {
            ThreadLocalScenarioControl.requireCoroutineContext()
        }.isInstanceOf(KontraktInternalException::class.java)
    }
}
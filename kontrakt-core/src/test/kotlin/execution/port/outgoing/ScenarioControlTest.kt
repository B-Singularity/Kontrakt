package execution.port.outgoing

import execution.adapter.mockito.MockitoScenarioContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

interface ScenarioControlContract {

    fun createSut(): ScenarioControl

    @Test
    fun `createScenarioContext - returns a valid MockitoScenarioContext`() {
        val sut = createSut()

        val context = sut.createScenarioContext()

        assertThat(context).isInstanceOf(MockitoScenarioContext::class.java)
    }
}
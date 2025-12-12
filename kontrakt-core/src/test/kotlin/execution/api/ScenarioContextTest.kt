package execution.api

import discovery.api.KontraktConfigurationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertFailsWith

abstract class ScenarioContextTest {

    protected abstract fun createScenarioContext(): ScenarioContext

    interface TestService {
        fun perform(arg: String): String
    }

    @Test
    fun `every-returns should successfully stub return value on a mock`() {

        val context = createScenarioContext()
        val mockService = Mockito.mock(TestService::class.java)

        context every { mockService.perform("input") } returns "stubbed-output"


        assertEquals("stubbed-output", mockService.perform("input"))
        assertEquals(null, mockService.perform("other"))
    }

    @Test
    fun `every-throws should successfully stub exception on a mock`() {

        val context = createScenarioContext()
        val mockService = Mockito.mock(TestService::class.java)
        val expectedError = IllegalStateException("Boom")

        context every { mockService.perform("bomb") } throws expectedError

        val exception = assertFailsWith<IllegalStateException> {
            mockService.perform("bomb")
        }
        assertEquals("Boom", exception.message)
    }

    @Test
    fun `every-returns should throw ConfigurationException when stubbing a real object`() {
        val context = createScenarioContext()
        val realObject = "I am a real string"

        assertFailsWith<KontraktConfigurationException> {
            context every { realObject.length } returns 999
        }
    }
}
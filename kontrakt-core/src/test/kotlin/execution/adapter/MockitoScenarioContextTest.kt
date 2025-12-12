package execution.adapter

import execution.api.ScenarioContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MockitoScenarioContextTest {

    private val context: ScenarioContext = MockitoScenarioContext()

    interface TestInterface {
        fun doAction(): String
    }

    @Test
    fun `every-returns should successfully apply Mockito stubbing`() {

        val mockObject = Mockito.mock(TestInterface::class.java)

        context every { mockObject.doAction() } returns "Stubbed Value"

        assertEquals("Stubbed Value", mockObject.doAction())
    }

    @Test
    fun `every-throws should successfully stub exception`() {

        val mockObject = Mockito.mock(TestInterface::class.java)
        val expectedException = IllegalStateException("Test Exception")

        context every { mockObject.doAction() } throws expectedException

        val actualException = assertFailsWith<IllegalStateException> {
            mockObject.doAction()
        }
        assertEquals("Test Exception", actualException.message)
    }

    @Test
    fun `every-returns should throw RuntimeException when stubbing a non-mock object`() {
        val realObject = "I am Real"

        val exception = assertFailsWith<RuntimeException> {
            context every { realObject.length } returns 100
        }

        assertTrue(exception.message!!.contains("Failed to apply stubbing"), "Should wrap the Mockito error")
        assertTrue(exception.message!!.contains("Ensure you are calling a method on a Mock object"))
    }

    @Test
    fun `every-throws should throw RuntimeException when stubbing fails`() {

        val realObject = "I am Real"
        val targetException = RuntimeException("Boom")

        val exception = assertFailsWith<RuntimeException> {
            context every { realObject.length } throws targetException
        }
        
        assertTrue(exception.message!!.contains("Failed to stub exception"), "Should wrap the Mockito error")
    }
}
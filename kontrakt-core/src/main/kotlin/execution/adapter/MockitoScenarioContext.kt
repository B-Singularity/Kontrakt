package execution.adapter

import execution.api.ScenarioContext
import execution.api.StubbingBuilder
import org.mockito.Mockito

class MockitoScenarioContext : ScenarioContext {

    override infix fun <T> every(methodCall: () -> T): StubbingBuilder<T> {
        return MockitoStubbingBuilder(methodCall)
    }

    private class MockitoStubbingBuilder<T>(
        private val methodCall: () -> T
    ) : StubbingBuilder<T> {

        override infix fun returns(value: T) {
            try {
                Mockito.`when`(methodCall()).thenReturn(value)
            } catch (e: Exception) {
                throw RuntimeException("Failed to apply stubbing. Ensure you are calling a method on a Mock object.", e)
            }
        }

        override infix fun throws(exception: Throwable) {
            try {
                Mockito.`when`(methodCall()).thenThrow(exception)
            } catch (e: Exception) {
                throw RuntimeException("Failed to stub exception.", e)
            }
        }
    }
}
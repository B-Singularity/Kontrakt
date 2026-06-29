package adapter.mockito

import org.mockito.Mockito
import realization.execution.scenario.ScenarioContext
import realization.execution.scenario.StubbingBuilder
import stage.diagnostic.material.KontraktConfigurationException

class MockitoScenarioContext : ScenarioContext {
    override infix fun <T> every(methodCall: () -> T): StubbingBuilder<T> = MockitoStubbingBuilder(methodCall)

    private class MockitoStubbingBuilder<T>(
        private val methodCall: () -> T,
    ) : StubbingBuilder<T> {
        override infix fun returns(value: T) {
            try {
                Mockito.`when`(methodCall()).thenReturn(value)
            } catch (e: Exception) {
                throw KontraktConfigurationException(
                    "Failed to apply stubbing. Ensure you are calling a method on a Mock object within 'every { ... }'.",
                    e,
                )
            }
        }

        override infix fun throws(exception: Throwable) {
            try {
                Mockito.`when`(methodCall()).thenThrow(exception)
            } catch (e: Exception) {
                throw KontraktConfigurationException(
                    "Failed to stub exception. Ensure you are calling a method on a Mock object.",
                    e,
                )
            }
        }
    }
}

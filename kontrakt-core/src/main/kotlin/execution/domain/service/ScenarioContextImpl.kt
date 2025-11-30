package execution.domain.service

import execution.api.ScenarioContext

class ScenarioContextImpl : ScenarioContext {
    override fun <T> every(methodCall: () -> T): StubbingBuilder<T> {
        try {
            methodCall()
        } catch (e: Exception) {
        }
        return MockitoStubbingBuilder()
    }
}
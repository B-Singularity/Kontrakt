package execution.port.outgoing

import kotlin.reflect.KClass

interface MockingEngine {
    fun <T : Any> createMock(
        classToMock: KClass<T>,
        context: MockingContext,
    ): T

    fun <T : Any> createFake(
        classToFake: KClass<T>,
        context: MockingContext,
    ): T

    fun createScenarioContext(): ScenarioContext
}

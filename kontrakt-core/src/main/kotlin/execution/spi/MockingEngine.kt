package execution.spi

import execution.api.ScenarioContext
import execution.domain.service.generation.FixtureGenerator
import kotlin.reflect.KClass

interface MockingEngine {
    fun <T : Any> createMock(
        classToMock: KClass<T>,
        generator: FixtureGenerator,
    ): T

    fun <T : Any> createFake(
        classToFake: KClass<T>,
        generator: FixtureGenerator,
    ): T

    fun createScenarioContext(): ScenarioContext
}

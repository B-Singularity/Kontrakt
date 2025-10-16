package execution.spi

import kotlin.reflect.KClass

interface MockingEngine {

    fun <T : Any> createMock(classToMock: KClass<T>): T
}
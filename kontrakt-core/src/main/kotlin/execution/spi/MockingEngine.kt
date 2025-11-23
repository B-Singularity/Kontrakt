package execution.spi

import kotlin.reflect.KClass

interface MockingEngine {

    fun <T : Any> createMock(classToMock: KClass<T>): T

    fun <T : Any> createFake(classToMock: KClass<T>): T
}
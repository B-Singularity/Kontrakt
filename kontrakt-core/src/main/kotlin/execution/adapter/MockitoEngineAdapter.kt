package execution.adapter

import execution.spi.MockingEngine
import org.mockito.Mockito
import kotlin.reflect.KClass

class MockitoEngineAdapter : MockingEngine {
    override fun <T : Any> createMock(classToMock: KClass<T>) : T {
        return Mockito.mock(classToMock.java)
    }
}
package common.reflection

import java.lang.reflect.InvocationTargetException
import kotlin.test.Test
import kotlin.test.assertSame

class ExceptionUtilsTest {

    @Test
    fun `should return self when exception is not wrapped`() {
        val original = IllegalArgumentException("I am a raw exception")

        val result = original.unwrapped

        assertSame(original, result, "Raw exception should be returned as is")
    }

    @Test
    fun `should unwrap single layer of InvocationTargetException`() {
        val rootCause = NullPointerException("Root cause")
        val wrapper = InvocationTargetException(rootCause)

        val result = wrapper.unwrapped

        assertSame(rootCause, result, "Should extract the target exception from InvocationTargetException")
    }

    @Test
    fun `should unwrap multiple layers of InvocationTargetException recursively`() {

        val realError = IllegalStateException("Deep trouble")
        val level3 = InvocationTargetException(realError)
        val level2 = InvocationTargetException(level3)
        val level1 = InvocationTargetException(level2)

        val result = level1.unwrapped

        assertSame(realError, result, "Should unwrap all layers and return the root cause")
    }

    @Test
    fun `should return wrapper itself if target exception is null`() {

        val emptyWrapper = InvocationTargetException(null)

        val result = emptyWrapper.unwrapped

        assertSame(emptyWrapper, result, "Should return self if there is no target exception to unwrap")
    }
}
package execution.domain.util

import java.lang.reflect.InvocationTargetException

object ExceptionHelper {
    fun unwrap(t: Throwable): Throwable {
        var current = t
        while (current is InvocationTargetException) {
            current = current.targetException ?: break
        }
        return current
    }
}
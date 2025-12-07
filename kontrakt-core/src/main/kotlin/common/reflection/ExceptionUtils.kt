package common.reflection

import java.lang.reflect.InvocationTargetException

val Throwable.unwrapped: Throwable
    get() {
        var current = this
        while (current is InvocationTargetException) {
            current = current.targetException ?: break
        }
        return current
    }
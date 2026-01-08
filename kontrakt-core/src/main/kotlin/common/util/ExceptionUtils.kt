package common.util

import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

data class SourceCoordinate(
    val fileName: String? = null,
    val lineNumber: Int? = null,
    val className: String? = null,
    val methodName: String? = null,
) {
    val isUnknown: Boolean get() = fileName == null
}


val Throwable.unwrapped: Throwable
    get() {
        var current = this
        while (current is InvocationTargetException) {
            current = current.targetException ?: break
        }
        return current
    }

fun Throwable.extractCoordinate(targetClass: KClass<*>? = null): SourceCoordinate {
    val frames = this.stackTrace

    if (targetClass != null) {
        val targetName = targetClass.qualifiedName
        val specificFrame = frames.firstOrNull { it.className == targetName }

        if (specificFrame != null) {
            return specificFrame.toCoordinate()
        }
    }

    val userFrame = frames.firstOrNull { it.isUserCode() }

    return userFrame?.toCoordinate() ?: SourceCoordinate()
}

private fun StackTraceElement.isUserCode(): Boolean {
    val name = this.className
    return IGNORED_PREFIXES.none { name.startsWith(it) }
}

private fun StackTraceElement.toCoordinate() = SourceCoordinate(
    fileName = this.fileName,
    lineNumber = this.lineNumber,
    className = this.className,
    methodName = this.methodName
)

private val IGNORED_PREFIXES = setOf(
    "execution.",
    "discovery.",
    "infrastructure.",
    "common.",
    "java.lang.reflect",
    "jdk.internal",
    "sun.reflect",
    "org.junit",
    "kotlinx.coroutines",
    "kotlin.reflect"
)
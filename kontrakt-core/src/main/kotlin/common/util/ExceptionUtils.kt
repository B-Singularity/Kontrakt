package common.util

import execution.domain.vo.SourceLocation
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

/**
 * Extracts the strict [SourceLocation] from the stack trace.
 */
fun Throwable.extractSourceLocation(targetClass: KClass<*>? = null): SourceLocation {
    val frames = this.stackTrace

    // 1. Priority Search
    if (targetClass != null) {
        val targetName = targetClass.qualifiedName
        val specificFrame = frames.firstOrNull { it.className == targetName }
        if (specificFrame != null) return specificFrame.toDefinedLocation()
    }

    // 2. Smart Filter
    val userFrame = frames.firstOrNull { it.isUserCode() }

    // 3. Return types instead of nulls
    return userFrame?.toDefinedLocation() ?: SourceLocation.Unknown
}

// --- Internal Helper Extensions ---

private fun StackTraceElement.isUserCode(): Boolean {
    val name = this.className
    return IGNORED_PREFIXES.none { name.startsWith(it) }
}

private fun StackTraceElement.toDefinedLocation() =
    SourceLocation.Exact(
        fileName = this.fileName ?: "UnknownSource",
        lineNumber = this.lineNumber,
        className = this.className,
        methodName = this.methodName,
    )

private val IGNORED_PREFIXES =
    setOf(
        "execution.",
        "discovery.",
        "infrastructure.",
        "common.",
        "java.lang.reflect",
        "jdk.internal",
        "sun.reflect",
        "org.junit",
        "kotlinx.coroutines",
        "kotlin.reflect",
    )

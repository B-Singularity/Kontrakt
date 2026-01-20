package common.util

import exception.ContractViolationException
import exception.KontraktConfigurationException
import exception.KontraktException
import execution.domain.vo.verification.SourceLocation
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

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

/**
 * [ADR-020] Stack Trace Filtering.
 * Mutates the stack trace to hide framework internals unless verbose mode is on.
 */
fun Throwable.sanitizeStackTrace(verbose: Boolean = false): Throwable {
    if (verbose) return this

    val originalTrace = this.stackTrace
    val filteredTrace = originalTrace.filter { it.isUserCode() }.toTypedArray()

    // If we filtered everything (rare), keep the top frame to avoid empty trace info
    if (filteredTrace.isEmpty() && originalTrace.isNotEmpty()) {
        this.stackTrace = arrayOf(originalTrace[0])
    } else {
        this.stackTrace = filteredTrace
    }

    // Recursively sanitize cause
    this.cause?.sanitizeStackTrace(verbose)

    return this
}

/**
 * [ADR-020] Blame Assignment.
 * Determines the category of the error for reporting purposes.
 */
fun Throwable.analyzeBlame(): Blame =
    when (this) {
        // Business Logic Failure (Assertion / Contract)
        is AssertionError,
        is ContractViolationException,
            -> Blame.TEST_FAILURE

        // User Configuration Error
        is KontraktConfigurationException -> Blame.SETUP_FAILURE

        // Framework Internal Error (Lifecycle, Bugs)
        is KontraktException -> Blame.INTERNAL_ERROR

        // Unexpected Runtime Exception (NPE, etc.) in User Code
        else -> Blame.EXECUTION_FAILURE
    }

/**
 * [ADR-020] Blame Category.
 * Explicitly categorizes the origin of a failure to determine the reporting strategy.
 *
 * @property description Human-readable description for UI/Logs.
 */
enum class Blame(
    val description: String,
) {
    /**
     * Indicates incorrect usage of the framework by the user.
     * Examples: Invalid annotations, circular dependencies, missing constructors.
     * Action: User must fix the test configuration.
     */
    SETUP_FAILURE("Setup Failed"),

    /**
     * Indicates that the tested business logic violated a contract or assertion.
     * This is a standard "Red" test result.
     * Action: User must fix the business logic or adjust expectations.
     */
    TEST_FAILURE("Test Failed"),

    /**
     * Indicates an unexpected runtime exception within the user's code.
     * Examples: NullPointerException, IndexOutOfBoundsException in the target method.
     * Action: User must debug the application code.
     */
    EXECUTION_FAILURE("Execution Error"),

    /**
     * Indicates a bug or crash within the Kontrakt framework itself.
     * Examples: Lifecycle violations, internal reflection errors.
     * Action: Report issue to framework maintainers.
     */
    INTERNAL_ERROR("Internal Framework Error"),
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

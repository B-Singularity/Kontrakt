package discovery.api

/**
 * The root exception for the Kontrakt framework.
 * All custom exceptions thrown by Kontrakt should inherit from this class.
 *
 * Using a common base exception allows users to catch all framework-related errors
 * with a single try-catch block if needed.
 */
open class KontraktException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Thrown when the user has provided an invalid configuration or usage of the framework.
 * Examples:
 * - A target class annotated with @Contract has no primary constructor.
 * - A parameter type cannot be resolved.
 * - An annotation is used incorrectly.
 *
 * This exception indicates a "User Error" that should be fixed by correcting the test setup.
 */
class KontraktConfigurationException(
    message: String,
    cause: Throwable? = null,
) : KontraktException("[Configuration Error] $message", cause)

/**
 * Thrown when the tested code violates the contract defined in the interface.
 * Examples:
 * - A method returns a negative value when @Positive is required.
 * - A constructor accepts an invalid input without throwing an exception.
 *
 * This exception indicates a "Bug" in the user's implementation code.
 */
class ContractViolationException(
    message: String,
    cause: Throwable? = null,
) : KontraktException("[Contract Violation] $message", cause)

/**
 * Thrown when an unexpected error occurs within the Kontrakt framework itself.
 * Examples:
 * - Reflection failure due to internal logic bugs.
 * - State inconsistency in the framework.
 *
 * This exception indicates a "Framework Bug" and should be reported to the maintainers.
 */
class KontraktInternalException(
    message: String,
    cause: Throwable? = null,
) : KontraktException("[Internal Error] $message", cause)

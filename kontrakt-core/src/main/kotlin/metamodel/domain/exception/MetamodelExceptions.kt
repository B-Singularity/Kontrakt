package metamodel.domain.exception

import exception.KontraktException

/**
 * Base exception for Metamodel domain errors.
 * Extends [KontraktException] to integrate with the framework's error reporting.
 */
open class MetamodelException(message: String, cause: Throwable? = null) : KontraktException(message, cause) {
    // Override domain to distinguish from EXECUTION or INFRASTRUCTURE
    override val domain: String = "METAMODEL"
}

/**
 * Thrown when strict mode policies are violated.
 * Example: Using Unresolved Generics (T, ?) or Wildcards in a TypeReference.
 */
class StrictModeViolationException(message: String) : MetamodelException(message)

/**
 * Thrown when non-deterministic behavior is detected.
 * Example: Duplicate Annotations found via Reflection where order is undefined.
 */
class DeterminismViolationException(message: String) : MetamodelException(message)
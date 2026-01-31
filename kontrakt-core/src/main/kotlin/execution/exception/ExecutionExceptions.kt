package execution.domain.exception

import execution.domain.vo.plan.Attribute
import metamodel.domain.vo.TypeReference

internal class GenerationFailedException(
    val type: TypeReference,
    part: String? = null,
    cause: Throwable? = null,
) : RuntimeException(
    // or KontraktConfigurationException
    "Failed to generate value for type: ${type.name}${part?.let { " (at $it)" } ?: ""}",
    cause,
)

internal class RecursiveGenerationFailedException(
    val type: TypeReference,
    val path: List<String>,
    cause: Throwable,
) : RuntimeException(
    "Failed to generate recursive structure for type: ${type.name} (at field: '${path.joinToString(".")}')",
    cause,
)

internal class ConflictingAnnotationsException(
    fieldName: String,
    annotations: List<String>,
    reason: String,
) : RuntimeException(
    "[Ambiguous Contract] Field '$fieldName' has conflicting annotations: ${annotations.joinToString(", ")}. " +
            "Please use only one. (Reason: $reason)",
)

internal class InvalidAnnotationValueException(
    fieldName: String,
    value: Any?,
    reason: String,
) : RuntimeException(
    "[Invalid Value] Field '$fieldName' has invalid configuration value '$value'. Reason: $reason",
)

internal class CollectionSizeLimitExceededException(
    targetSize: Int,
    limit: Int,
) : RuntimeException(
    "[Safety Limit Exceeded] Collection size ($targetSize) exceeds the global limit ($limit). " +
            "To force execution, use '@Size(..., ignoreLimit = true)'.",
)

internal class SealedClassHasNoSubclassesException(
    type: TypeReference, // 👈 KType -> TypeReference
) : RuntimeException(
    "Sealed class '${type.name}' has no permitted subclasses. Please ensure at least one subclass is defined and accessible.",
)

internal class UnsupportedGeneratorException(
    generatorClassName: String, // 👈 KClass -> String (Name only)
) : RuntimeException(
    "Encountered an unsupported generation type: '$generatorClassName'. " +
            "Generators must implement either 'RecursiveGenerator' or 'TerminalGenerator'.",
)

internal class KontraktLifecycleException(
    val componentName: String,
    val action: String,
    val reason: String,
) : RuntimeException(
    // or KontraktException
    "[Lifecycle Violation] Invalid state in component '$componentName'. " +
            "Cannot perform '$action' because: $reason",
)


sealed class ExecutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown when the Planner fails to analyze a type structure or detects an invalid state.
 * (e.g., Unresolvable cycle, Missing generic type info)
 */
class StructuralPlanningException(
    val type: TypeReference,
    message: String,
    cause: Throwable? = null
) : ExecutionException("Failed to plan structure for type '${type.name}': $message", cause)

/**
 * Thrown when the Linker cannot find a suitable Generator strategy for a given node.
 * Holds the raw set of attributes to assist in debugging strategy mismatches.
 */
class GeneratorNotFoundException(
    val type: TypeReference,
    val attributes: Set<Attribute>
) : ExecutionException(
    "No suitable generator found for type '${type.name}' with attributes: ${attributes.joinToString { it.toString() }}"
)

/**
 * Thrown when the Linker fails to resolve a concrete implementation for an interface.
 * [ADR-025] Interface-First Design requires strict resolution of implementations.
 */
class ImplementationResolutionException(
    val type: TypeReference,
    message: String
) : ExecutionException("Failed to resolve implementation for interface '${type.name}': $message")

/**
 * Thrown during the expansion phase if policies or overrides are invalid.
 * (e.g., Invalid path in user override, collection size policy conflict)
 */
class LinkageException(
    val path: String,
    message: String,
    cause: Throwable? = null
) : ExecutionException("Linkage failed at path '$path': $message", cause)

/**
 * Thrown when the Virtual Machine fails to execute a generator or assemble an object.
 */
class VMExecutionException(
    val type: TypeReference,
    message: String,
    cause: Throwable? = null
) : ExecutionException("VM execution failed for type '${type.name}': $message", cause)
package execution.exception

import exception.KontraktConfigurationException
import kotlin.reflect.KType

internal class GenerationFailedException(
    type: KType,
    part: String? = null,
    cause: Throwable? = null,
) : KontraktConfigurationException(
    "Failed to generate value for type: $type${part?.let { " (at $it)" } ?: ""}",
    cause
)

internal class RecursiveGenerationFailedException(
    type: KType,
    val path: List<String>,
    cause: Throwable
) : KontraktConfigurationException(
    "Failed to generate recursive structure for type: $type (at field: '${path.joinToString(".")}')",
    cause
)

internal class ConflictingAnnotationsException(
    fieldName: String,
    annotations: List<String>,
) : KontraktConfigurationException(
    "[Ambiguous Contract] Field '$fieldName' has conflicting annotations: ${annotations.joinToString(", ")}. Please use only one."
)

internal class InvalidAnnotationValueException(
    fieldName: String,
    value: Any?,
    reason: String
) : KontraktConfigurationException(
    "[Invalid Value] Field '$fieldName' has invalid configuration value '$value'. Reason: $reason"
)

internal class CollectionSizeLimitExceededException(
    targetSize: Int,
    limit: Int
) : KontraktConfigurationException(
    "[Safety Limit Exceeded] Collection size ($targetSize) exceeds the global limit ($limit). " +
            "To force execution, use '@Size(..., ignoreLimit = true)'."
)
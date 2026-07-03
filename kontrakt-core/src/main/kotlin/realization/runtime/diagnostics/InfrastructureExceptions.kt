package realization.runtime.diagnostics

import diagnostic.retention.diagnostics.exception.KontraktException

/**
 * Base exception for all infrastructure-related failures.
 * This separates infrastructure concerns from domain logic errors.
 */
open class InfrastructureException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause)

/**
 * Exceptions specific to the [realization.runtime.registry.RuntimeHandleRegistry].
 */
sealed class RegistryException(
    message: String,
) : InfrastructureException(message) {
    /**
     * Thrown when a cycleId collision occurs.
     * This usually happens when different types or ClassLoaders map to the same ID.
     */
    class Collision(
        val key: String,
        val existing: String,
        val new: String,
    ) : RegistryException(
        "Registry Collision Detected! Key: '$key'.\nExisting: $existing\nNew:      $new",
    )

    /**
     * Thrown when attempting to register a payload after the registry has been sealed.
     */
    class Closed : RegistryException("Registry is sealed (read-only). Registration denied.")

    /**
     * Thrown when a required payload is missing during the execution phase.
     */
    class MissingPayload(
        val key: String,
    ) : RegistryException("Missing payload for key: '$key'. Check Discovery phase.")
}

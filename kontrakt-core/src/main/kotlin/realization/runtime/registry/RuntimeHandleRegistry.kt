package realization.runtime.registry

import realization.runtime.diagnostics.RegistryException
import stage.canonicalization.material.TypeReference
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

/**
 * [Infrastructure] Runtime Payload Registry (Lock-Free / Phased)
 *
 * Acts as a thread-safe bridge between the Metamodel Adapter (Producer) and Execution Adapter (Consumer).
 *
 * ## Lifecycle & Concurrency Model
 * 1. **Discovery Phase (Write-Only)**: Adapters register payloads. Thread-safe via [java.util.concurrent.ConcurrentHashMap].
 * 2. **Sealing Point**: The framework calls [seal], transitioning the registry to read-only mode.
 * 3. **Execution Phase (Read-Only)**: Workers retrieve payloads. Wait-free and lock-free.
 *
 * ## Constraints
 * - **Run-Scope**: This instance MUST be scoped to a single execution run to prevent ClassLoader leaks.
 * - **Isolation**: Do NOT share across different test engines or daemons.
 */
class RuntimeHandleRegistry : AutoCloseable {
    private data class Entry(
        val payload: Any,
        val fingerprint: String,
    )

    private val entries = ConcurrentHashMap<String, Entry>()

    @Volatile
    private var sealed = false

    /**
     * Registers a runtime payload during the Discovery phase.
     *
     * @param type The pure domain type reference.
     * @param payload The platform-specific payload (e.g., Class<*>).
     * @throws realization.runtime.diagnostics.RegistryException.Closed If called after [seal].
     * @throws realization.runtime.diagnostics.RegistryException.Collision If a conflicting payload is detected for the same cycleId.
     */
    fun register(
        type: TypeReference,
        payload: Any,
    ) {
        if (sealed) throw RegistryException.Closed()

        val key = type.cycleId
        val newFingerprint = computeFingerprint(payload)
        val newEntry = Entry(payload, newFingerprint)

        // Atomic insertion to prevent race conditions during parallel discovery
        val existing = entries.putIfAbsent(key, newEntry)

        if (existing != null) {
            // Strict check: Same ID but different content/ClassLoader implies a collision
            if (existing.payload !== payload && existing.fingerprint != newEntry.fingerprint) {
                throw RegistryException.Collision(key, existing.fingerprint, newEntry.fingerprint)
            }
        }
    }

    /**
     * Seals the registry, preventing further registrations.
     * Call this exactly once after the Discovery phase completes.
     */
    fun seal() {
        sealed = true
    }

    /**
     * Retrieves a payload associated with the type.
     * Safe for concurrent access during the Execution phase.
     */
    fun get(type: TypeReference): Any? = entries[type.cycleId]?.payload

    /**
     * Clears the registry resources.
     * Mandatory cleanup to prevent memory leaks in Daemon/IDE environments.
     */
    override fun close() {
        entries.clear()
        sealed = false
    }

    /**
     * Generates a fingerprint including ClassLoader identity to detect conflicts.
     */
    private fun computeFingerprint(payload: Any): String =
        when (payload) {
            is Class<*> -> "Class[${payload.name}]@Loader${System.identityHashCode(payload.classLoader)}"
            is Type -> "Type[${payload.typeName}]"
            else -> "${payload::class.java.name}@${System.identityHashCode(payload)}"
        }
}
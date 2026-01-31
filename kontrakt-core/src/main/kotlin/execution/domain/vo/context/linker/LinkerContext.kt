package execution.domain.vo.context.linker

import execution.domain.strategy.generation.Generator
import execution.domain.vo.config.ExecutionPolicy

/**
 * [Context Firewall]
 * Provides a restricted view of the context required for the Linking phase.
 * Prevents the Linker from accessing runtime state or execution history.
 */
interface LinkerContext {
    val policy: ExecutionPolicy

    /**
     * Retrieves a user-defined override for a specific path.
     * @param path The dot-notation path (e.g., "users[0].name")
     */
    fun getOverride(path: String): Generator<*>?

    /**
     * Generates a deterministic integer for structural decisions (e.g., collection size).
     */
    fun generateStructuralSize(min: Int, max: Int): Int
}
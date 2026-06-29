package stage.lowering.diagnostics

import realization.runtime.diagnostics.sanitization.PayloadSanitizer
import stage.diagnostic.material.KontraktException

/**
 * [Domain Root] Base exception for all errors occurring in the Linking Phase (Step 3).
 * Represents the boundary of the Linking Domain.
 */
abstract class LinkingException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "LINKING"

    companion object {
        private const val MAX_REPORT_ENTRIES = 256
        private const val STRINGIFY_LIMIT = 50
        private const val KEY_PREVIEW_LIMIT = 64
        private const val ERROR_MSG_LIMIT = 256
    }

    /**
     * Shared logic for "Deep Freeze" strategy.
     * Guaranteed:
     * 1. No OOM (Input Capping via Sequence)
     * 2. No Hang (O(1) Identity Hash)
     * 3. No Collision (Index-based Uniqueness)
     */
    protected fun freezeContext(originalContext: Map<String, Any?>): Map<String, Any?> {
        // 1. Pre-emptive Cap Snapshot:
        // NEVER trust originalContext.size. ALWAYS use sequence to take fixed amount.
        // This avoids copying massive maps or triggering unsafe iteration on the full set.
        val cappedSnapshot: Map<String, Any?> =
            runCatching {
                originalContext.entries
                    .asSequence()
                    .take(MAX_REPORT_ENTRIES)
                    .associate { (k, v) -> k to v }
            }.getOrElse { e ->
                return mapOf(
                    "error" to "Critical: Failed to access context entries",
                    "cause" to (e.message?.take(ERROR_MSG_LIMIT) ?: "Unknown"),
                )
            }

        return try {
            // 2. Deep Sanitize: Attempt to sanitize the capped snapshot (Best Case)
            PayloadSanitizer.sanitizeMap(cappedSnapshot)
        } catch (e: Throwable) {
            // 3. Ultimate Safe Fallback:
            // If Sanitizer fails, switch to metadata-only mode.
            // NEVER call value.toString() to avoid recursive hangs or OOM.

            val safeRaw =
                runCatching {
                    cappedSnapshot.entries.joinToString(
                        limit = STRINGIFY_LIMIT,
                        truncated = "<...truncated...>",
                    ) { (k, v) ->
                        val keyPreview = k.take(KEY_PREVIEW_LIMIT)
                        val typeName = v?.javaClass?.name ?: "null"
                        "$keyPreview<$typeName>" // Key<Type> format
                    }
                }.getOrElse { t ->
                    "<Stringify Failed: ${t.javaClass.simpleName}>"
                }

            mapOf(
                "error" to "Failed to freeze context (Sanitizer Error)",
                "sanitizer_error" to
                        mapOf(
                            "type" to e.javaClass.name,
                            "message" to (e.message?.take(ERROR_MSG_LIMIT) ?: "null"),
                        ),
                "raw_summary" to safeRaw,
                // [Key Safety] Just a list of truncated keys
                "keys" to cappedSnapshot.keys.map { it.take(KEY_PREVIEW_LIMIT) },
                // [Collision-Free Metadata]
                // Uses System.identityHashCode (O(1)) + Index to guarantee 0% collision in the report.
                // Prevents O(N) hashing hangs on massive strings.
                "value_types" to
                        cappedSnapshot.entries
                            .asSequence()
                            .mapIndexed { i, (k, v) ->
                                val preview = k.take(KEY_PREVIEW_LIMIT)
                                val id = System.identityHashCode(k).toString(16) // O(1)
                                val uniqueKey = "$preview[#$id:$i]" // Guaranteed unique in this map
                                uniqueKey to (v?.javaClass?.name ?: "null")
                            }.toMap(),
            )
        }
    }
}

/**
 * [User Logic Error] Thrown when the user's contract configuration contains contradictions.
 */
class LinkingAmbiguityException(
    message: String,
    violations: List<String>,
) : LinkingException(message) {
    // Immediate defensive copy
    val violations: List<String> = violations.toList()
    override val payload = mapOf("violations" to this.violations)
}

/**
 * [Internal Protocol Error] Thrown when the framework's internal invariants are violated.
 */
class LinkingProtocolException(
    message: String,
    context: Map<String, Any?> = emptyMap(),
) : LinkingException(message) {
    override val payload = freezeContext(context)
}

/**
 * [Input Error] Thrown when the input requirements for linking are invalid.
 */
class LinkingInputException(
    message: String,
    context: Map<String, Any?> = emptyMap(),
) : LinkingException(message) {
    override val payload = freezeContext(context)
}

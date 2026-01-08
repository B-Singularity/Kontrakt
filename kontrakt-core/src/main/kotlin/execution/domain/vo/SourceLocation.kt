package execution.domain.vo

/**
 * [Value Object] Source Location
 *
 * Represents the source code location associated with an event.
 * Replaces the nullable/string-based coordinate system with a strict type hierarchy.
 */
sealed interface SourceLocation {

    /**
     * [Case 1] Location is captured and defined.
     * Used when TraceMode is ON or when an Exception stacktrace is analyzed.
     */
    data class Exact(
        val fileName: String,
        val lineNumber: Int,
        val className: String,
        val methodName: String? = null
    ) : SourceLocation {
        override fun toString(): String = "$fileName:$lineNumber ($methodName)"
    }

    /**
     * [Case 2] Approximate Location (Class Level).
     * We know the target class, but not the specific line.
     * (Used for fallback enrichment when TraceMode is ON but no specific assertion failed)
     */
    data class Approximate(
        val className: String,
        val displayName: String? = null
    ) : SourceLocation {
        override fun toString(): String = className
    }

    /**
     * [Case 3] Location cannot be determined.
     * (e.g., Native methods, or stacktrace filtering failed to find user code)
     */
    data object Unknown : SourceLocation

    /**
     * [Case 4] Location capture was skipped intentionally.
     * (e.g., TraceMode is OFF for performance optimization)
     */
    data object NotCaptured : SourceLocation
}
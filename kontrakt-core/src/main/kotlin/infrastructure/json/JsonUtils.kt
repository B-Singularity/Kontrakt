package infrastructure.json

// Heuristic buffer size to prevent internal array resizing during simple escapes.
private const val ESCAPE_BUFFER_SIZE = 16

/**
 * [Infrastructure] Zero-Dependency JSON Escaper.
 * Escapes special characters (", \, newlines, etc.) within a string to comply with JSON standards.
 *
 * This implementation intentionally avoids heavy libraries like Jackson or Gson
 * to support lightweight NDJSON generation. It prioritizes performance and memory efficiency
 * by directly controlling StringBuilder and avoiding temporary object creation.
 */
fun String.escapeJson(): String {
    // Explicitly initialize StringBuilder with a calculated capacity.
    // This avoids the overhead of checking array bounds for the first few escape characters.
    val sb = StringBuilder(this.length + ESCAPE_BUFFER_SIZE)

    for (char in this) {
        when (char) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\b' -> sb.append("\\b")
            '\u000C' -> sb.append("\\f")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> sb.append(char)
        }
    }
    return sb.toString()
}
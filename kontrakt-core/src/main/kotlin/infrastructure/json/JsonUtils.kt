package infrastructure.json

/**
 * [Infrastructure] Simple JSON Serializer.
 * Only supports types guaranteed by [PayloadSanitizer]:
 * - Maps, Lists, Strings, Numbers, Booleans, Null.
 */
object JsonUtils {
    fun toJson(obj: Any?): String = buildString { appendValue(obj) }

    private fun StringBuilder.appendValue(obj: Any?) {
        when (obj) {
            null -> append("null")
            is String -> appendString(obj)
            is Number, is Boolean -> append(obj.toString())
            is Map<*, *> -> appendMap(obj)
            is Iterable<*> -> appendList(obj)
            is Array<*> -> appendList(obj.asIterable())
            else -> appendString(obj.toString())
        }
    }

    private fun StringBuilder.appendString(str: String) {
        append('"')
        for (char in str) {
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else ->
                    if (char.code in 0..31) {
                        append(String.format("\\u%04x", char.code))
                    } else {
                        append(char)
                    }
            }
        }
        append('"')
    }

    private fun StringBuilder.appendMap(map: Map<*, *>) {
        append('{')
        var first = true
        for ((k, v) in map) {
            if (!first) append(',')
            first = false
            appendString(k.toString())
            append(':')
            appendValue(v)
        }
        append('}')
    }

    private fun StringBuilder.appendList(list: Iterable<*>) {
        append('[')
        var first = true
        for (item in list) {
            if (!first) append(',')
            first = false
            appendValue(item)
        }
        append(']')
    }
}

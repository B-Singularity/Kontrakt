package stage.admission.diagnostics.evidence

private const val MAX_DIAGNOSTIC_VALUE_CHARS: Int = 160

internal fun safeDiagnosticValue(value: String): String {
    val builder = StringBuilder()
    val limit =
        if (value.length < MAX_DIAGNOSTIC_VALUE_CHARS) {
            value.length
        } else {
            MAX_DIAGNOSTIC_VALUE_CHARS
        }

    var i = 0
    while (i < limit) {
        val ch = value[i]
        when (ch) {
            '\n' -> builder.append("\\n")
            '\r' -> builder.append("\\r")
            '\t' -> builder.append("\\t")
            else -> {
                if (ch.isISOControl()) {
                    builder.append("\\u")
                    val hex = ch.code.toString(16).padStart(4, '0')
                    builder.append(hex)
                } else {
                    builder.append(ch)
                }
            }
        }
        i++
    }

    if (value.length > MAX_DIAGNOSTIC_VALUE_CHARS) {
        builder.append("…")
        builder.append("(truncated,length=")
        builder.append(value.length)
        builder.append(')')
    }

    return builder.toString()
}

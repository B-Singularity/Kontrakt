package infrastructure.json

import java.util.IdentityHashMap

/**
 * [JSON Safety Guard]
 * Enforces strict JSON-safe types and prevents Circular References.
 *
 * ### Architecture Policy: Strict Schema (Fail-Fast)
 * Unlike the Encoder, this Validator adopts a **Paranoid** strategy:
 * * **Unknown Types:** Strictly REJECTS any type not explicitly allowlisted (e.g. Custom Numbers).
 * * **Goal:** Catch schema violations early during Development/Testing (CI).
 */
object JsonSafeValidator {

    @Volatile
    var enabled: Boolean = true

    fun validate(value: Any?, path: String = "root") {
        if (!enabled) return
        validateRecursive(value, path, IdentityHashMap())
    }

    private fun validateRecursive(value: Any?, path: String, seen: IdentityHashMap<Any, Boolean>) {
        if (value == null) return

        when (value) {
            is String, is Boolean -> return

            is Number -> when (value) {
                // [Allowlist] Only standard JVM numbers are allowed.
                is Byte, is Short, is Int, is Long, is java.math.BigInteger, is java.math.BigDecimal -> return
                is Double -> if (!value.isFinite()) throw throwUnsafe("Non-finite number ($value) at '$path'", value)
                is Float -> if (!value.isFinite()) throw throwUnsafe("Non-finite number ($value) at '$path'", value)

                // [Strict Policy] Reject Custom Numbers in QA to enforce schema consistency.
                else -> throw throwUnsafe("Unsupported Number type ${value::class.java.name} at '$path'", value)
            }

            is List<*> -> {
                if (seen.put(value, true) != null) throw throwUnsafe("Circular reference detected at '$path'", value)
                try {
                    value.forEachIndexed { index, item ->
                        validateRecursive(item, "$path[$index]", seen)
                    }
                } finally {
                    seen.remove(value)
                }
            }

            is Map<*, *> -> {
                if (seen.put(value, true) != null) throw throwUnsafe("Circular reference detected at '$path'", value)
                try {
                    value.forEach { (k, v) ->
                        if (k !is String) throw throwUnsafe(
                            "Map keys must be Strings. Found: ${k?.javaClass?.name} at '$path'",
                            value
                        )
                        val key = k as String
                        // [Path] Safe escaping for debug path
                        validateRecursive(v, "$path[\"${key.escapePathKey()}\"]", seen)
                    }
                } finally {
                    seen.remove(value)
                }
            }

            else -> throw throwUnsafe("Unsupported type ${value::class.java.name} at '$path'", value)
        }
    }

    private fun throwUnsafe(message: String, value: Any?): Nothing {
        throw JsonValidationException("[Paranoid QA] JSON Safety Violation: $message. Value: $value")
    }
}
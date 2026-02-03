package infrastructure.json

import java.util.IdentityHashMap

/**
 * [JSON Safety Guard]
 * Enforces strict JSON-safe types and prevents Circular References.
 * Adopts a "Paranoid" policy: any type not explicitly allowlisted is rejected.
 */
object JsonSafeValidator {

    @Volatile
    var enabled: Boolean = true

    fun validate(value: Any?, path: String = "root") {
        if (!enabled) return
        validateRecursive(value, path, IdentityHashMap())
    }

    private fun validateRecursive(value: Any?, path: String, seen: IdentityHashMap<Any, Boolean>) {
        when (value) {
            null, is String, is Boolean -> return

            is Number -> when (value) {
                // [Allowlist] Only standard JVM numbers are allowed.
                // Custom Number implementations are rejected to enforce schema consistency.
                is Byte, is Short, is Int, is Long, is java.math.BigInteger, is java.math.BigDecimal -> return

                is Double -> if (!value.isFinite()) throw throwUnsafe("Non-finite number ($value) at '$path'", value)
                is Float -> if (!value.isFinite()) throw throwUnsafe("Non-finite number ($value) at '$path'", value)

                // [Paranoid Policy] Reject unknown Number types (e.g. AtomicInteger, CustomNumber).
                // They should be converted to standard primitives or Strings before reaching the payload.
                else -> throw throwUnsafe("Unsupported Number type ${value::class.simpleName} at '$path'", value)
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
                            "Map keys must be Strings. Found: ${k?.javaClass?.simpleName} at '$path'",
                            value
                        )
                        validateRecursive(v, "$path.$k", seen)
                    }
                } finally {
                    seen.remove(value)
                }
            }

            else -> throw throwUnsafe("Unsupported type ${value::class.simpleName} at '$path'", value)
        }
    }

    private fun throwUnsafe(message: String, value: Any?): Nothing {
        throw IllegalStateException("[Paranoid QA] JSON Safety Violation: $message. Value: $value")
    }
}
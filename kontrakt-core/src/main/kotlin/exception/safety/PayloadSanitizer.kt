package exception.safety

import java.math.BigDecimal
import java.math.BigInteger
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.math.abs
import kotlin.math.min

/**
 * [Shared Kernel] Deep Payload Sanitizer.
 *
 * Ensures observability data is safe for consumption by reporting subsystems.
 *
 * ## Features
 * - **DoS Resistant:** Global budget, Zero-copy traversal.
 * - **Type Safe:** Handles Unsigned types, Primitives, Arrays.
 * - **Invincible:** Per-node fault tolerance.
 */
object PayloadSanitizer {
    private const val MAX_DEPTH = 10
    private const val MAX_COLLECTION_SIZE = 100
    private const val MAX_STRING_LENGTH = 1000

    // Global Budget
    private const val BUDGET_MAX_NODES = 500
    private const val BUDGET_MAX_TOTAL_CHARS = 50_000

    // BigNumber Limits
    private const val MAX_BIT_LENGTH = 4096
    private const val MAX_BIGDECIMAL_PRECISION = 4096
    private const val MAX_BIGDECIMAL_SCALE = 4096

    private class SanitizationContext {
        var nodeCount = 0
        var totalChars = 0
        var budgetExceeded = false
        val visited = IdentityHashMap<Any, Boolean>()

        fun checkBudget(): Boolean {
            if (budgetExceeded) return false
            if (++nodeCount > BUDGET_MAX_NODES || totalChars > BUDGET_MAX_TOTAL_CHARS) {
                budgetExceeded = true
                return false
            }
            return true
        }

        fun recordChars(length: Int) {
            if (budgetExceeded) return
            totalChars += length
            if (totalChars > BUDGET_MAX_TOTAL_CHARS) budgetExceeded = true
        }
    }

    fun sanitizeMap(input: Map<String, Any?>): Map<String, Any?> {
        return try {
            val context = SanitizationContext()
            sanitizeMapInternal(input, context, 0)
        } catch (exception: Throwable) {
            mapOf("error" to "Sanitization Failed", "cause" to (exception.message ?: "Unknown"))
        }
    }

    fun sanitize(input: Any?): Any? {
        return try {
            val context = SanitizationContext()
            sanitizeRecursive(input, context, 0)
        } catch (exception: Throwable) {
            "<Sanitization Failed: ${exception.message}>"
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun sanitizeRecursive(obj: Any?, context: SanitizationContext, depth: Int): Any? {
        if (context.budgetExceeded) return "<Budget Exceeded>"
        if (obj == null) return null

        if (!context.checkBudget()) return recordAndReturnString("<Budget Exceeded>", context)
        if (depth > MAX_DEPTH) return recordAndReturnString("<Max Depth Exceeded>", context)

        // 1. Primitives & Numbers
        if (obj is Number) {
            return when (obj) {
                is Double -> if (!obj.isFinite()) recordAndReturnString(obj.toString(), context) else obj
                is Float -> if (!obj.isFinite()) recordAndReturnString(obj.toString(), context) else obj

                is BigInteger -> {
                    val bits = obj.bitLength()
                    if (bits > MAX_BIT_LENGTH) {
                        recordAndReturnString("<BigInteger bitLength=$bits>", context)
                    } else {
                        recordAndReturnString(obj.toString(), context)
                    }
                }

                is BigDecimal -> {
                    val precision = obj.precision()
                    val scale = obj.scale()
                    if (precision > MAX_BIGDECIMAL_PRECISION || abs(scale) > MAX_BIGDECIMAL_SCALE) {
                        recordAndReturnString("<BigDecimal precision=$precision scale=$scale>", context)
                    } else {
                        recordAndReturnString(truncateString(obj.toString()), context)
                    }
                }

                else -> obj
            }
        }

        if (obj is UInt) return recordAndReturnString(obj.toString(), context)
        if (obj is ULong) return recordAndReturnString(obj.toString(), context)
        if (obj is UShort) return recordAndReturnString(obj.toString(), context)
        if (obj is UByte) return recordAndReturnString(obj.toString(), context)

        if (obj is Boolean) return obj
        if (obj is Char) return obj.toString()

        if (obj is String) {
            return recordAndReturnString(truncateString(obj), context)
        }

        if (obj is Enum<*>) return obj.name
        if (obj is Class<*>) return obj.name

        // 2. Side-Effect Prevention
        if (obj is Sequence<*>) return recordAndReturnString("<Sequence (Traversal Forbidden)>", context)
        if (obj is Iterator<*>) return recordAndReturnString("<Iterator (Traversal Forbidden)>", context)

        // 3. Cycle Detection
        if (context.visited.containsKey(obj)) {
            return recordAndReturnString("<Cycle Detected: ${obj.javaClass.simpleName}>", context)
        }

        context.visited[obj] = true
        var pushedToStack = true

        try {
            return when (obj) {
                is Map<*, *> -> sanitizeMapInternal(obj, context, depth)
                is Iterable<*> -> sanitizeCollection(obj, context, depth)
                is Array<*> -> sanitizeCollection(obj.asIterable(), context, depth)

                is CharArray -> {
                    val limit = min(obj.size, MAX_STRING_LENGTH)
                    val partial = String(obj, 0, limit)
                    val result = if (obj.size > limit) "$partial... (Truncated)" else partial
                    recordAndReturnString(result, context)
                }

                is ByteArray -> recordAndReturnString("<ByteArray size=${obj.size}>", context)
                is UByteArray -> recordAndReturnString("<UByteArray size=${obj.size}>", context)

                // Signed Primitive Arrays
                is IntArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is LongArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is ShortArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is DoubleArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is FloatArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is BooleanArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)

                // Unsigned Primitive Arrays
                is UIntArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is ULongArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)
                is UShortArray -> sanitizePrimitiveArray(obj.size, { obj[it] }, context, depth)

                else -> {
                    recordAndReturnString(truncateString(obj.toString()), context)
                }
            }
        } catch (e: Throwable) {
            return recordAndReturnString("<Serialization Error: ${e.javaClass.simpleName}>", context)
        } finally {
            if (pushedToStack) {
                context.visited.remove(obj)
            }
        }
    }

    private inline fun <T> sanitizePrimitiveArray(
        size: Int,
        getter: (Int) -> T,
        context: SanitizationContext,
        depth: Int
    ): List<Any?> {
        val result = ArrayList<Any?>()
        val limit = min(size, MAX_COLLECTION_SIZE)

        for (i in 0 until limit) {
            if (context.budgetExceeded) {
                result.add("<Budget Exceeded>")
                break
            }
            result.add(sanitizeRecursive(getter(i), context, depth + 1))
        }

        if (size > limit) {
            if (context.budgetExceeded) {
                result.add("<Truncated>")
            } else {
                result.add(recordAndReturnString("<Truncated: Collection size exceeded $MAX_COLLECTION_SIZE>", context))
            }
        }
        return Collections.unmodifiableList(result)
    }

    private fun sanitizeMapInternal(map: Map<*, *>, context: SanitizationContext, depth: Int): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        var count = 0

        for ((key, value) in map) {
            if (context.budgetExceeded) {
                result["<Truncated>"] = "<Budget Exceeded>"
                break
            }
            if (count++ >= MAX_COLLECTION_SIZE) {
                val truncKey = recordAndReturnString("<Truncated>", context)
                result[truncKey] = recordAndReturnString("Map size exceeded $MAX_COLLECTION_SIZE", context)
                break
            }

            val keyString = try {
                // [Fix] Use kotlin's toString() which handles nulls safely
                val rawKey = key.toString()
                val truncated = truncateString(rawKey)
                context.recordChars(truncated.length)
                truncated
            } catch (ignored: Throwable) {
                recordAndReturnString("<KeyError>", context)
            }

            result[keyString] = sanitizeRecursive(value, context, depth + 1)
        }
        return Collections.unmodifiableMap(result)
    }

    private fun sanitizeCollection(collection: Iterable<*>, context: SanitizationContext, depth: Int): List<Any?> {
        val result = ArrayList<Any?>()
        var count = 0
        for (item in collection) {
            if (context.budgetExceeded) {
                result.add("<Budget Exceeded>")
                break
            }
            if (count++ >= MAX_COLLECTION_SIZE) {
                result.add(recordAndReturnString("<Truncated: Collection size exceeded $MAX_COLLECTION_SIZE>", context))
                break
            }
            result.add(sanitizeRecursive(item, context, depth + 1))
        }
        return Collections.unmodifiableList(result)
    }

    private fun truncateString(string: String): String {
        return if (string.length > MAX_STRING_LENGTH) {
            "${string.substring(0, MAX_STRING_LENGTH)}... (Truncated)"
        } else string
    }

    private fun recordAndReturnString(string: String, context: SanitizationContext): String {
        context.recordChars(string.length)
        return string
    }
}
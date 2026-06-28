package adapter.jvm

import execution.domain.exception.RuntimeInstantiationException
import execution.port.outgoing.RuntimeInstantiator
import execution.port.outgoing.RuntimeTypeHandle
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.ArrayDeque
import java.util.Deque
import java.util.Queue

/**
 * [Adapter] JVM Reflection-based Instantiator.
 *
 * ## Fallback Policy
 * - **Set Interface:** Defaults to [LinkedHashSet] for deterministic iteration order.
 * - **List/Collection Interface:** Defaults to [ArrayList] for standard behavior.
 * - **Queue/Deque Interface:** Defaults to [ArrayDeque].
 */
class ReflectionRuntimeInstantiator : RuntimeInstantiator {
    override fun createEmptyCollection(handle: RuntimeTypeHandle): Any {
        val clazz = (handle as JvmClassHandle).clazz

        // 1. Exact Match Strategy
        if (clazz == ArrayList::class.java) return ArrayList<Any?>()
        if (clazz == HashSet::class.java) return HashSet<Any?>()
        if (clazz == LinkedHashSet::class.java) return LinkedHashSet<Any?>()
        if (clazz == ArrayDeque::class.java) return ArrayDeque<Any?>()

        // 2. Interface Fallback Strategy
        return when {
            // Set -> LinkedHashSet
            Set::class.java.isAssignableFrom(clazz) -> {
                if (clazz.isAssignableFrom(LinkedHashSet::class.java)) {
                    LinkedHashSet<Any?>()
                } else if (clazz.isAssignableFrom(HashSet::class.java)) {
                    HashSet<Any?>()
                } else {
                    throw RuntimeInstantiationException("Cannot instantiate compatible Set for ${clazz.name}")
                }
            }

            // List -> ArrayList
            List::class.java.isAssignableFrom(clazz) -> {
                if (clazz.isAssignableFrom(ArrayList::class.java)) {
                    ArrayList<Any?>()
                } else {
                    throw RuntimeInstantiationException("Cannot instantiate compatible List for ${clazz.name}")
                }
            }

            // Queue/Deque -> ArrayDeque
            Deque::class.java.isAssignableFrom(clazz) || Queue::class.java.isAssignableFrom(clazz) -> {
                if (clazz.isAssignableFrom(ArrayDeque::class.java)) {
                    ArrayDeque<Any?>()
                } else {
                    throw RuntimeInstantiationException("Cannot instantiate compatible Queue for ${clazz.name}")
                }
            }

            // Collection (General) -> ArrayList
            Collection::class.java.isAssignableFrom(clazz) -> {
                if (clazz.isAssignableFrom(ArrayList::class.java)) {
                    ArrayList<Any?>()
                } else {
                    throw RuntimeInstantiationException("Cannot instantiate compatible Collection for ${clazz.name}")
                }
            }

            else -> throw RuntimeInstantiationException("Unsupported concrete collection type: ${clazz.name}")
        }
    }

    override fun createEmptyMap(handle: RuntimeTypeHandle): Any {
        val clazz = (handle as JvmClassHandle).clazz

        if (clazz == HashMap::class.java) return HashMap<Any?, Any?>()
        if (clazz == LinkedHashMap::class.java) return LinkedHashMap<Any?, Any?>()

        return when {
            Map::class.java.isAssignableFrom(clazz) -> {
                if (clazz.isAssignableFrom(LinkedHashMap::class.java)) {
                    LinkedHashMap<Any?, Any?>()
                } else if (clazz.isAssignableFrom(HashMap::class.java)) {
                    HashMap<Any?, Any?>()
                } else {
                    throw RuntimeInstantiationException("Cannot instantiate compatible Map for ${clazz.name}")
                }
            }

            else -> throw RuntimeInstantiationException("Unsupported map type: ${clazz.name}")
        }
    }

    override fun createEmptyArray(handle: RuntimeTypeHandle): Any {
        val clazz = (handle as JvmClassHandle).clazz
        if (!clazz.isArray) {
            throw RuntimeInstantiationException("Requested array instantiation for non-array type: ${clazz.name}")
        }
        return java.lang.reflect.Array
            .newInstance(clazz.componentType, 0)
    }

    override fun createDiagnosticStub(
        handle: RuntimeTypeHandle,
        message: String,
    ): Any {
        val clazz = (handle as JvmClassHandle).clazz
        if (!clazz.isInterface) {
            throw RuntimeInstantiationException("Diagnostic stubs can only be created for interfaces. Found: ${clazz.name}")
        }

        return Proxy.newProxyInstance(
            clazz.classLoader,
            arrayOf(clazz),
            DiagnosticInvocationHandler(message),
        )
    }

    private class DiagnosticInvocationHandler(
        private val message: String,
    ) : InvocationHandler {
        override fun invoke(
            proxy: Any,
            method: Method,
            args: Array<out Any>?,
        ): Any? =
            when {
                method.name == "toString" && method.parameterCount == 0 -> "DiagnosticStub[$message]"
                method.name == "hashCode" && method.parameterCount == 0 -> System.identityHashCode(proxy)
                method.name == "equals" && method.parameterCount == 1 -> {
                    val other = args?.getOrNull(0)
                    proxy === other
                }

                else -> throw RuntimeInstantiationException(message)
            }
    }
}

package execution.adapter

import execution.domain.service.FixtureGenerator
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

class MockitoEngineAdapter : MockingEngine {
    private val logger = KotlinLogging.logger {}

    // [Circular Dependency Resolution]
    // FixtureGenerator depends on MockingEngine, and MockingEngine depends on FixtureGenerator
    // for generative fallbacks. We use 'lazy' to break this cycle.
    private val fixtureGenerator by lazy { FixtureGenerator(this) }

    /**
     * Creates a **Stateless Mock** for general components.
     *
     * Unlike standard Mockito mocks (which return null/0 by default), this mock uses
     * [GenerativeAnswer] to return plausible, non-null values based on the return type.
     * This prevents NullPointerExceptions in the tested logic without manual stubbing.
     *
     * It also warns if state-changing methods (e.g., save, delete) are called on a stateless mock.
     */
    override fun <T : Any> createMock(classToMock: KClass<T>): T {
        return Mockito.mock(classToMock.java, GenerativeAnswer(fixtureGenerator, classToMock, logger))
    }

    /**
     * Creates a **Stateful Fake** for repositories or stores.
     *
     * It behaves like an In-Memory Database (using a ConcurrentHashMap).
     * - Supports basic CRUD operations (save, findById, findAll, delete, count).
     * - Falls back to [GenerativeAnswer] for complex queries that cannot be simulated by the Map.
     */
    override fun <T : Any> createFake(classToFake: KClass<T>): T {
        val smartAnswer = StatefulOrGenrativeAnswer(fixtureGenerator)
        return Mockito.mock(classToFake.java, smartAnswer)
    }

    private class GenerativeAnswer(
        private val generator: FixtureGenerator,
        private val mockType: KClass<*>,
        private val logger: KLogger
    ) : Answer<Any?> {

        override fun answer(invocation: InvocationOnMock): Any? {
            val methodName = invocation.method.name

            // [Detective Mode]
            // Warn if a stateful method is called on a stateless mock (likely a configuration error).
            if (isSuspiciousStatefulMethod(methodName)) {
                logger.warn {
                    "⚠️ Potential Configuration Issue: " +
                            "Method '$methodName' was called on Stateless Mock '${mockType.simpleName}'. " +
                            "If this is a Repository, please annotate interface '${mockType.simpleName}' with '@Stateful' to enable In-Memory storage."
                }
            }

            val returnType = invocation.method.kotlinFunction?.returnType

            if (returnType == null || returnType.classifier == Unit::class) return null

            // Delegate value generation to FixtureGenerator
            return generator.generateByType(returnType)
        }

        private fun isSuspiciousStatefulMethod(name: String): Boolean {
            return name.startsWith("save") ||
                    name.startsWith("insert") ||
                    name.startsWith("update") ||
                    name.startsWith("delete") ||
                    name.startsWith("remove") ||
                    name.startsWith("store")
        }
    }

    /**
     * [Strategy B: Stateful Answer]
     * A hybrid answer that simulates CRUD operations using a Map,
     * and falls back to generation for unknown methods.
     */
    private class StatefulOrGenrativeAnswer(
        private val generator: FixtureGenerator
    ) : Answer<Any?> {
        private val store = ConcurrentHashMap<Any, Any>()

        override fun answer(invocation: InvocationOnMock): Any? {
            val method = invocation.method
            val name = method.name
            val args = invocation.arguments ?: emptyArray()

            if (name == "toString") return "StatefulFake\$${Integer.toHexString(hashCode())}"
            if (name == "hashCode") return System.identityHashCode(this)
            if (name == "equals") return invocation.mock === args.getOrNull(0)

            // 2. Smart CRUD Logic (Map Operations)
            try {
                // Save / Create
                if (isSave(name)) {
                    val entity = args.firstOrNull() ?: return null
                    val id = extractId(entity) ?: "auto-id-${store.size + 1}"
                    store[id] = entity
                    return entity // Usually save() returns the entity
                }

                // Find By ID
                if (isFindById(name, args)) {
                    val key = args.firstOrNull()
                    val result = store[key]

                    if (method.returnType == Optional::class.java) {
                        return Optional.ofNullable(result)
                    }
                    return result
                }

                // Find All
                if (isFindAll(name)) {
                    return store.values.toList()
                }

                // Delete
                if (isDelete(name)) {
                    val arg = args.firstOrNull()
                    if (arg != null) {
                        val id = extractId(arg) ?: arg
                        store.remove(id)
                    }
                    return null
                }

                // Count
                if (name == "count") return store.size.toLong()
            } catch (ignored: Exception) {
                // If CRUD logic fails, ignore and proceed to fallback
            }

            // 3. Generative Fallback
            val returnType = method.kotlinFunction?.returnType
            if (returnType == null || returnType.classifier == Unit::class) return null

            return generator.generateByType(returnType)
        }

        // --- Heuristics Helpers ---
        private fun isSave(n: String) = n.startsWith("save") || n.startsWith("create") || n.startsWith("register")

        private fun isFindById(n: String, args: Array<Any>) =
            (n.startsWith("find") || n.startsWith("get")) && args.size == 1 && !n.contains("All") && !n.contains("By")

        private fun isFindAll(n: String) = n.contains("All") || n.contains("findAll") || n == "list"

        private fun isDelete(n: String) = n.startsWith("delete") || n.startsWith("remove")

        private fun extractId(entity: Any): Any? {
            return try {
                val kClass = entity::class
                kClass.memberProperties.firstOrNull { it.name.equals("id", ignoreCase = true) }?.let {
                    it.isAccessible = true
                    it.getter.call(entity)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

}
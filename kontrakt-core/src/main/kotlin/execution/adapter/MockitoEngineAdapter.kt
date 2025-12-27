package execution.adapter

import execution.api.ScenarioContext
import execution.domain.generator.GenerationRequest
import execution.domain.service.FixtureGenerator
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

class MockitoEngineAdapter :
    MockingEngine,
    ScenarioControl {
    private val logger = KotlinLogging.logger {}

    // [Circular Dependency Resolution]
    // Uses lazy initialization to handle the bidirectional dependency with FixtureGenerator.
    private val fixtureGenerator by lazy { FixtureGenerator(this) }

    override fun <T : Any> createMock(classToMock: KClass<T>): T =
        Mockito.mock(classToMock.java, GenerativeAnswer(fixtureGenerator, classToMock, logger))

    override fun <T : Any> createFake(classToFake: KClass<T>): T =
        Mockito.mock(classToFake.java, StatefulOrGenerativeAnswer(fixtureGenerator))

    override fun createScenarioContext(): ScenarioContext = MockitoScenarioContext()

    /**
     * [Shim Layer] Adapts Mockito invocations to the domain's [GenerationRequest].
     */
    private class GenerativeAnswer(
        private val generator: FixtureGenerator,
        private val mockType: KClass<*>,
        private val logger: KLogger,
    ) : Answer<Any?> {
        override fun answer(invocation: InvocationOnMock): Any? {
            val methodName = invocation.method.name

            // Warn on suspicious usage
            if (isSuspiciousStatefulMethod(methodName)) {
                logger.warn {
                    "⚠️ Potential Configuration Issue: " +
                        "Method '$methodName' was called on Stateless Mock '${mockType.simpleName}'. " +
                        "If this is a Repository, please annotate interface '${mockType.simpleName}' with '@Stateful' to enable In-Memory storage."
                }
            }

            val returnType = invocation.method.kotlinFunction?.returnType
            if (returnType == null || returnType.classifier == Unit::class) return null

            // [Conversion] KType -> GenerationRequest
            // Transforms the external 'return type' concept into the domain's 'generation request'.
            val request =
                GenerationRequest.from(
                    type = returnType,
                    name = "${invocation.method.name}:ReturnValue",
                )

            return generator.generate(request)
        }

        private fun isSuspiciousStatefulMethod(name: String): Boolean =
            name.startsWith("save") ||
                name.startsWith("insert") ||
                name.startsWith("update") ||
                name.startsWith("delete") ||
                name.startsWith("remove") ||
                name.startsWith("store")
    }

    private class StatefulOrGenerativeAnswer(
        private val generator: FixtureGenerator,
    ) : Answer<Any?> {
        private val store = ConcurrentHashMap<Any, Any>()

        override fun answer(invocation: InvocationOnMock): Any? {
            val method = invocation.method
            val name = method.name
            val args = invocation.arguments ?: emptyArray()

            if (name == "toString") return "StatefulFake\$${Integer.toHexString(hashCode())}"
            if (name == "hashCode") return System.identityHashCode(this)
            if (name == "equals") return invocation.mock === args.getOrNull(0)

            // Smart CRUD Logic (Map Operations)
            try {
                if (isSave(name)) {
                    val entity = args.firstOrNull() ?: return null
                    val id = extractId(entity) ?: "auto-id-${store.size + 1}"
                    store[id] = entity
                    return entity
                }
                if (isFindById(name, args)) {
                    val key = args.firstOrNull()
                    val result = store[key]
                    if (method.returnType == Optional::class.java) {
                        return Optional.ofNullable(result)
                    }
                    return result
                }
                if (isFindAll(name)) {
                    return store.values.toList()
                }
                if (isDelete(name)) {
                    val arg = args.firstOrNull()
                    if (arg != null) {
                        val id = extractId(arg) ?: arg
                        store.remove(id)
                    }
                    return null
                }
                if (name == "count") return store.size.toLong()
            } catch (ignored: Exception) {
            }

            // Generative Fallback
            val returnType = method.kotlinFunction?.returnType
            if (returnType == null || returnType.classifier == Unit::class) return null

            // [Conversion] KType -> GenerationRequest
            val request =
                GenerationRequest.from(
                    type = returnType,
                    name = "${invocation.method.name}:ReturnValue",
                )

            return generator.generate(request)
        }

        private fun isSave(n: String) = n.startsWith("save") || n.startsWith("create") || n.startsWith("register")

        private fun isFindById(
            n: String,
            args: Array<Any>,
        ) = (n == "findById" || n == "getById") &&
            args.size == 1 ||
            ((n.startsWith("find") || n.startsWith("get")) && args.size == 1 && !n.contains("By"))

        private fun isFindAll(n: String) = n.contains("All") || n.contains("findAll") || n == "list"

        private fun isDelete(n: String) = n.startsWith("delete") || n.startsWith("remove")

        private fun extractId(entity: Any): Any? =
            try {
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

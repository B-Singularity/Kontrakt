package execution.adapter

import execution.adapter.state.ThreadLocalScenarioControl
import execution.api.ScenarioContext
import execution.domain.generator.GenerationRequest
import execution.domain.service.generation.FixtureGenerator
import execution.domain.vo.trace.ExecutionTrace
import execution.spi.MockingContext
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import execution.spi.trace.ScenarioTrace
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

/**
 * [Adapter] Smart Mockito Engine.
 *
 * This adapter bridges Mockito with the Kontrakt framework's generation and tracing systems.
 *
 * ### Architecture Note: Method Injection Pattern (Stateless)
 * This adapter receives dependencies via [MockingContext] during mock creation.
 * It does NOT rely on ThreadLocal, making it safe for parallel execution and cleaner in design.
 *
 * **Strategies:**
 * - **Stateless Mocks:** Delegate directly to [FixtureGenerator] via [GenerativeAnswer].
 * - **Stateful Fakes:** Maintain an internal `ConcurrentHashMap` for CRUD operations via [StatefulOrGenerativeAnswer].
 */
class MockitoEngineAdapter :
    MockingEngine,
    ScenarioControl {
    private val logger = KotlinLogging.logger {}

    override fun <T : Any> createMock(
        classToMock: KClass<T>,
        context: MockingContext,
    ): T =
        Mockito.mock(
            classToMock.java,
            GenerativeAnswer(context.generator, classToMock, logger, context.trace),
        )

    override fun <T : Any> createFake(
        classToFake: KClass<T>,
        context: MockingContext,
    ): T =
        Mockito.mock(
            classToFake.java,
            StatefulOrGenerativeAnswer(context.generator, context.trace),
        )

    override fun createScenarioContext(): ScenarioContext = MockitoScenarioContext()

    /**
     * [Shim Layer] Adapts Mockito invocations to the domain's [GenerationRequest].
     */
    private class GenerativeAnswer(
        private val generator: FixtureGenerator,
        private val mockType: KClass<*>,
        private val logger: KLogger,
        private val trace: ScenarioTrace,
    ) : Answer<Any?> {
        override fun answer(invocation: InvocationOnMock): Any? {
            val methodName = invocation.method.name
            val args = invocation.arguments ?: emptyArray()
            val startTime = System.currentTimeMillis()

            // [UX] Warn if user tries to use stateful methods on a stateless mock
            if (isSuspiciousStatefulMethod(methodName)) {
                logger.warn {
                    "⚠️ Suspicious call '$methodName' on Stateless Mock '${mockType.simpleName}'. " +
                        "Use 'createFake()' (or @Stateful) if you need In-Memory DB behavior."
                }
            }

            val returnType = invocation.method.kotlinFunction?.returnType
            if (returnType == null || returnType.classifier == Unit::class) {
                trace.recordCapture(invocation, args, startTime)
                return null
            }

            val request =
                GenerationRequest.from(
                    type = returnType,
                    name = "${invocation.method.name}:ReturnValue",
                )

            val result = generator.generate(request)

            trace.recordCapture(invocation, args, startTime)

            return result
        }

        private fun isSuspiciousStatefulMethod(name: String): Boolean =
            name.startsWith("save") ||
                name.startsWith("insert") ||
                name.startsWith("update") ||
                name.startsWith("delete") ||
                name.startsWith("remove") ||
                name.startsWith("store")
    }

    /**
     * Strategy for Stateful Fakes (In-Memory DB).
     * Handles CRUD operations internally and logs traces to [ThreadLocalScenarioControl].
     */
    private class StatefulOrGenerativeAnswer(
        private val generator: FixtureGenerator,
        private val trace: ScenarioTrace,
    ) : Answer<Any?> {
        private val store = ConcurrentHashMap<Any, Any>()

        override fun answer(invocation: InvocationOnMock): Any? {
            val method = invocation.method
            val name = method.name
            val args = invocation.arguments ?: emptyArray()

            if (name == "toString") return "StatefulFake\$${Integer.toHexString(hashCode())}"
            if (name == "hashCode") return System.identityHashCode(this)
            if (name == "equals") return invocation.mock === args.getOrNull(0)

            val startTime = System.currentTimeMillis()

            val (result, handled) =
                runCatching {
                    when {
                        isSave(name) -> handleSave(args)
                        isFindById(name, args) -> handleFindById(args, method.returnType)
                        isFindAll(name) -> store.values.toList() to true
                        isDelete(name) -> handleDelete(args)
                        name == "count" -> store.size.toLong() to true
                        else -> null to false
                    }
                }.getOrDefault(null to false)

            if (handled) {
                recordTrace(invocation, args, startTime)
                return result
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

            return generator.generate(
                request,
            )
        }

        private fun handleSave(args: Array<Any>): Pair<Any?, Boolean> {
            val entity = args.firstOrNull() ?: return null to false
            val id = extractId(entity) ?: "auto-id-${store.size + 1}"
            store[id] = entity
            return entity to true
        }

        private fun handleFindById(
            args: Array<Any>,
            returnType: Class<*>,
        ): Pair<Any?, Boolean> {
            val key = args.firstOrNull()
            val found = store[key]
            val result = if (returnType == Optional::class.java) Optional.ofNullable(found) else found
            return result to true
        }

        private fun handleDelete(args: Array<Any>): Pair<Any?, Boolean> {
            args.firstOrNull()?.let { arg ->
                val id = extractId(arg) ?: arg
                store.remove(id)
            }
            return null to true
        }

        private fun recordTrace(
            invocation: InvocationOnMock,
            args: Array<Any>,
            startTime: Long,
        ) {
            val duration = System.currentTimeMillis() - startTime
            val interfaceName =
                invocation.mock::class.java.interfaces
                    .firstOrNull()
                    ?.simpleName ?: "Mock"

            val traceEvent =
                ExecutionTrace(
                    methodSignature = "$interfaceName.${invocation.method.name}",
                    arguments = args.map { it.toString() },
                    durationMs = duration,
                    timestamp = startTime,
                )

            runCatching {
                trace.decisions.add(traceEvent)
            }
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
            runCatching {
                entity::class
                    .memberProperties
                    .firstOrNull { it.name.equals("id", ignoreCase = true) }
                    ?.apply { isAccessible = true }
                    ?.getter
                    ?.call(entity)
            }.getOrNull()
    }
}

private fun ScenarioTrace.recordCapture(
    invocation: InvocationOnMock,
    args: Array<Any>,
    startTime: Long,
) {
    val duration = System.currentTimeMillis() - startTime

    val interfaceName =
        invocation.mock::class.java.interfaces
            .firstOrNull()
            ?.simpleName ?: "Mock"

    val traceEvent =
        ExecutionTrace(
            methodSignature = "$interfaceName.${invocation.method.name}",
            arguments = args.map { it.toString() }, // 인자 사용
            durationMs = duration,
            timestamp = startTime,
        )

    runCatching {
        this.decisions.add(traceEvent)
    }
}

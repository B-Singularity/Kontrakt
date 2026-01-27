package execution.adapter.mockito

import exception.KontraktConfigurationException
import execution.domain.service.generation.FixtureGenerator
import execution.domain.vo.context.generation.GenerationRequest
import execution.domain.vo.trace.ExecutionTrace
import execution.port.outgoing.MockingContext
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioContext
import execution.port.outgoing.ScenarioControl
import execution.port.outgoing.ScenarioTrace
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.lang.reflect.InvocationTargetException
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

/**
 * Smart Mockito Engine.
 *
 * This adapter acts as the bridge between the standard Mockito framework and the Kontrakt
 * generation/tracing ecosystem. It allows users to use familiar Mockito syntax while
 * gaining the benefits of automated fixture generation and deep execution tracing.
 *
 * ### Architectural Decisions (ADR)
 * 1. **Stateless vs Stateful:**
 * - **Stateless Mocks:** Handled by [GenerativeAnswer]. They are pure functions that generate
 * return values on the fly without retaining any state. Ideal for service dependencies.
 * - **Stateful Fakes:** Handled by [StatefulOrGenerativeAnswer]. They simulate a lightweight
 * In-Memory Database. Ideal for Repositories or Caches.
 *
 * 2. **Safety First Strategy:**
 * - **String Keys:** We enforce `String` keys for the internal storage to prevent "Memory Leak via Key Mutation".
 * (If mutable entities were used as keys, their hashCodes could change, making them irretrievable).
 * - **Monotonic IDs:** We use an [AtomicInteger] to generate IDs (`auto-id-1`, `auto-id-2`...)
 * ensuring they never collide even if items are deleted and recreated.
 *
 * 3. **Strict Conventions:**
 * - Heuristic matching (e.g., `isFindAll`) is strict (prefixes only) to avoid False Positives
 * like matching `playlistSongs()` as a `list` operation.
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
     * [Shim Layer] Stateless Generative Answer.
     *
     * Intercepts method calls on stateless mocks and delegates the return value generation
     * to the [FixtureGenerator]. It also records every interaction to the [ScenarioTrace].
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
            // This prevents confusion when "save()" doesn't actually save anything.
            if (isSuspiciousStatefulMethod(methodName)) {
                logger.warn {
                    "⚠️ Suspicious call '$methodName' on Stateless Mock '${mockType.simpleName}'. " +
                            "Use 'createFake()' (or @Stateful) if you need In-Memory DB behavior."
                }
            }

            // Determine the return type for generation
            val returnType = invocation.method.kotlinFunction?.returnType ?: run {
                trace.recordCapture(invocation, args, startTime)
                return null
            }

            // Handle Void methods: Just record trace and return null
            if (returnType.classifier == Unit::class) {
                trace.recordCapture(invocation, args, startTime)
                return null
            }

            // Build generation request
            val request =
                GenerationRequest.from(
                    type = returnType,
                    name = "${invocation.method.name}:ReturnValue",
                )

            // Generate value
            val result = generator.generate(request)

            // [Observability] Record the interaction trace
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
     * [Shim Layer] Stateful Fake Answer (In-Memory DB Simulation).
     *
     * Handles CRUD operations (Save, Find, Delete, Count) internally using a [ConcurrentHashMap].
     * Any method that does not match the CRUD patterns falls back to the [FixtureGenerator].
     *
     * **Key Features:**
     * - **Auto-ID Generation:** Automatically assigns IDs if missing.
     * - **Trace Consistency:** Ensures that both CRUD logic and generative fallbacks are recorded.
     */
    private class StatefulOrGenerativeAnswer(
        private val generator: FixtureGenerator,
        private val trace: ScenarioTrace,
    ) : Answer<Any?> {
        // [Safety] Use String keys to ensure stability even if Entity objects are mutable.
        private val store = ConcurrentHashMap<String, Any>()

        // [Reliability] Monotonic counter prevents ID reuse collision (e.g., 1 -> delete -> 1).
        private val idCounter = AtomicInteger(0)

        override fun answer(invocation: InvocationOnMock): Any? {
            val method = invocation.method
            val name = method.name
            val args = invocation.arguments ?: emptyArray()

            // 1. Handle Object methods (toString, hashCode, equals)
            // Essential for debugging and collection operations.
            if (name == "toString") return "StatefulFake@" + Integer.toHexString(hashCode())
            if (name == "hashCode") return System.identityHashCode(this)
            if (name == "equals") return invocation.mock === args.getOrNull(0)

            val startTime = System.currentTimeMillis()

            // 2. Try to handle as Stateful CRUD operation
            // Uses strict matching logic to avoid false positives.
            // [Fail-Fast] Direct execution without 'runCatching'.
            val (result, handled) = when {
                isSave(name) -> handleSave(args)
                isFindById(name, args) -> handleFindById(args, method.returnType)
                isFindAll(name) -> store.values.toList() to true
                isDelete(name) -> handleDelete(args)
                isCount(name) -> store.size.toLong() to true
                else -> null to false
            }

            if (handled) {
                recordTrace(invocation, args, startTime)
                return result
            }

            // 3. Fallback: Generative Strategy
            // If the method is not a CRUD operation (e.g., custom query), generate a random response.
            val returnType = method.kotlinFunction?.returnType ?: run {
                recordTrace(invocation, args, startTime)
                return null
            }

            if (returnType.classifier == Unit::class) {
                recordTrace(invocation, args, startTime)
                return null
            }

            val request =
                GenerationRequest.from(
                    type = returnType,
                    name = "${invocation.method.name}:ReturnValue",
                )

            // [Consistency] Critical: Record the call BEFORE returning the generated value.
            // This ensures that the trace log is complete even for fallback scenarios.
            recordTrace(invocation, args, startTime)

            return generator.generate(request)
        }

        private fun handleSave(args: Array<Any>): Pair<Any?, Boolean> {
            val entity = args.firstOrNull() ?: return null to false

            val extractedId = extractId(entity)

            // Auto-ID strategy
            val id = extractedId?.toString() ?: "auto-id-${idCounter.incrementAndGet()}"

            store[id] = entity
            return entity to true
        }

        private fun handleFindById(
            args: Array<Any>,
            returnType: Class<*>,
        ): Pair<Any?, Boolean> {
            val key = args.firstOrNull()?.toString()
            val found = store[key]

            // [Robustness] Handle 'java.util.Optional' return types correctly.
            // Since Optional is a final class, strict equality check is sufficient and fast.
            val result =
                if (returnType == Optional::class.java) {
                    Optional.ofNullable(found)
                } else {
                    found
                }
            return result to true
        }

        private fun handleDelete(args: Array<Any>): Pair<Any?, Boolean> {
            val arg = args.firstOrNull() ?: return null to true

            val extractedId = extractId(arg)

            val idToDelete = extractedId?.toString() ?: run {
                if (isPrimitiveOrString(arg)) {
                    arg.toString()
                } else {
                    throw KontraktConfigurationException(
                        "StatefulFake violation: Cannot delete object '${arg::class.simpleName}'. " +
                                "Stateful behavior requires an accessible 'id' property or a primitive key. " +
                                "Please verify the entity structure or use a stateless mock."
                    )
                }
            }

            store.remove(idToDelete)
            return null to true
        }

        /**
         * Records the execution trace for the interaction.
         * Formats arguments and measures duration.
         */
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
                trace.add(traceEvent)
            }
        }

        // --- Matchers ---

        // Matches: save*, create*, register*
        private fun isSave(n: String) = n.startsWith("save") || n.startsWith("create") || n.startsWith("register")

        // Matches: findById, getById, OR find...ById, get...ById
        // [Safety] Must start with find/get AND end with ById to avoid matching 'getDetails(id)'.
        private fun isFindById(
            n: String,
            args: Array<Any>,
        ) = args.size == 1 &&
                (
                        n == "findById" ||
                                n == "getById" ||
                                ((n.startsWith("find") || n.startsWith("get")) && n.endsWith("ById"))
                        )

        // Matches: findAll*, getAll*, list*
        // [Safety] Strict prefix matching to avoid false positives like 'playlistSongs'.
        private fun isFindAll(n: String) = n.startsWith("findAll") || n.startsWith("getAll") || n.startsWith("list")

        // Matches: delete*, remove*
        private fun isDelete(n: String) = n.startsWith("delete") || n.startsWith("remove")

        // Matches: *count* (Case-insensitive)
        private fun isCount(n: String) = n.contains("count", ignoreCase = true)

        /**
         * Reflection helper to extract the 'id' property.
         * [Fail-Fast] No 'runCatching'.
         */
        private fun extractId(entity: Any): Any? {
            val property = entity::class.memberProperties
                .firstOrNull { it.name.equals("id", ignoreCase = true) }
                ?: return null // Property missing

            property.isAccessible = true
            return try {
                property.getter.call(entity)
            } catch (e: InvocationTargetException) {
                throw e.cause ?: e
            }
        }

        private fun isPrimitiveOrString(value: Any): Boolean =
            value is String || value is Number || value is Boolean
    }
}

// Extension helper to avoid duplication in Stateless Answer
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
            arguments = args.map { it.toString() },
            durationMs = duration,
            timestamp = startTime,
        )

    runCatching {
        this.add(traceEvent)
    }
}

package execution.domain.service

import discovery.api.NotNull
import discovery.api.Null
import execution.domain.generator.ArrayTypeGenerator
import execution.domain.generator.BooleanTypeGenerator
import execution.domain.generator.CollectionTypeGenerator
import execution.domain.generator.EnumTypeGenerator
import execution.domain.generator.GenerationContext
import execution.domain.generator.GenerationRequest
import execution.domain.generator.NumericTypeGenerator
import execution.domain.generator.ObjectGenerator
import execution.domain.generator.RecursiveGenerator
import execution.domain.generator.SealedTypeGenerator
import execution.domain.generator.StringTypeGenerator
import execution.domain.generator.TerminalGenerator
import execution.domain.generator.TimeTypeGenerator
import execution.domain.generator.TypeGenerator
import execution.exception.GenerationFailedException
import execution.exception.RecursiveGenerationFailedException
import execution.exception.UnsupportedGeneratorException
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * Coordinates the generation of fixture data for testing purposes.
 *
 * This class acts as a central hub (Coordinator) that delegates specific generation logic
 * to registered [TypeGenerator] strategies. It ensures that the generation process is
 * deterministic, safe, and robust against infinite recursion.
 *
 * ### Key Architectural Patterns:
 *
 * 1. **Extension Function Pattern for Context Propagation:**
 * Internal generation logic is implemented as extension functions on [GenerationContext].
 * This treats the context as the **receiver (`this`)**, eliminating the need to pass
 * the context object as an explicit parameter through every method call. This reduces
 * visual noise ("parameter pollution") while ensuring thread-safe state access.
 *
 * 2. **Deterministic Generation:**
 * Uses a shared, seeded [Random] instance to initialize contexts. This ensures that
 * the same seed always produces the exact same sequence of data, which is crucial for
 * reproducible tests.
 *
 * 3. **Recursion Defense & Fallback:**
 * Detects circular dependencies in the object graph (via `GenerationContext.history`)
 * and seamlessly falls back to [MockingEngine] to create a mock object, breaking the
 * infinite recursion cycle.
 *
 * @property mockingEngine The engine used to create mock objects when POJO generation fails due to recursion.
 * @property clock The clock used for time-based generation (default: system default zone).
 * @property seed The seed value for the random number generator. Fixed for reproducibility.
 */
class FixtureGenerator(
    private val mockingEngine: MockingEngine,
    private val clock: Clock = Clock.systemDefaultZone(),
    seed: Long = System.nanoTime(),
) {
    private val logger = KotlinLogging.logger {}

    /**
     * The single source of randomness for this generator instance.
     * It is initialized with the provided [seed], ensuring that the sequence of generated values
     * remains consistent across multiple executions with the same seed.
     */
    private val sharedRandom: Random = Random(seed)

    init {
        logger.info { "FixtureGenerator initialized. Seed: $seed" }
    }

    /**
     * The registry of generation strategies.
     * The order is significant: specific types (like primitives and collections) must be
     * checked before the general [ObjectGenerator] fallback.
     */
    private val generators: List<TypeGenerator> =
        listOf(
            BooleanTypeGenerator(),
            TimeTypeGenerator(clock),
            NumericTypeGenerator(),
            StringTypeGenerator(),
            CollectionTypeGenerator(),
            ArrayTypeGenerator(),
            EnumTypeGenerator(),
            SealedTypeGenerator(),
            ObjectGenerator(), // Final fallback for POJOs
        )

    // =================================================================
    // Public Entry Points (API)
    // =================================================================

    /**
     * [Entry Point 1] Generates data using a fresh, independent context.
     *
     * This method is typically used for simple, standalone generation requests where
     * continuity with previous generation steps is not required. It creates a new
     * root [GenerationContext] derived from the shared seed.
     *
     * @param request The request object describing the target type and constraints.
     * @return The generated value (nullable).
     * @throws GenerationFailedException If generation fails or integrity checks fail.
     */
    fun generate(request: GenerationRequest): Any? {
        ContractConfigurationValidator.validate(request)
        // Create a new context and enter its scope
        return with(createRootContext()) {
            val result = generateInternal(request)
            validateResult(result, request)
            result
        }
    }

    /**
     * [Entry Point 2] Generates data using an existing, externally provided context.
     *
     * **Core API:** This is used by orchestrators like `TestInstanceFactory` to maintain
     * consistency across a complex object graph. By accepting an external [context],
     * it ensures that the random seed stream and recursion history are preserved.
     *
     * @param request The request object describing the target type.
     * @param context The existing generation context to use.
     * @return The generated value.
     */
    fun generate(
        request: GenerationRequest,
        context: GenerationContext,
    ): Any? {
        ContractConfigurationValidator.validate(request)
        // Use the provided context as the receiver ('this')
        return with(context) {
            val result = generateInternal(request)
            validateResult(result, request)
            result
        }
    }

    /**
     * Convenience wrapper for [generate(GenerationRequest)].
     * Converts a [KParameter] into a [GenerationRequest] automatically.
     */
    fun generate(param: KParameter): Any? = generate(GenerationRequest.from(param))

    /**
     * Convenience wrapper for [generate(GenerationRequest, GenerationContext)].
     * Converts a [KParameter] into a [GenerationRequest] automatically while preserving context.
     */
    fun generate(
        param: KParameter,
        context: GenerationContext,
    ): Any? = generate(GenerationRequest.from(param), context)

    // =================================================================
    // Boundary & Negative Testing
    // =================================================================

    /**
     * Generates a list of valid boundary values (Smart Fuzzing) for the given [param].
     *
     * Uses a fresh context to ensure isolation. Boundary values include edge cases
     * like minimum/maximum values, empty collections, or nulls (if allowed).
     */
    fun generateValidBoundaries(param: KParameter): List<Any?> {
        val request = GenerationRequest.from(param)
        ContractConfigurationValidator.validate(request)

        return with(createRootContext()) {
            generateValidBoundariesInternal(request)
        }
    }

    /**
     * Generates a list of invalid values (Negative Testing) for the given [param].
     *
     * Uses a fresh context. Invalid values include nulls for non-nullable types
     * or values violating specific constraints, intended to provoke validation errors.
     */
    fun generateInvalid(param: KParameter): List<Any?> {
        val request = GenerationRequest.from(param)
        ContractConfigurationValidator.validate(request)

        return with(createRootContext()) {
            generateInvalidInternal(request)
        }
    }

    // =================================================================
    // Internal Orchestration (Extension Functions)
    // =================================================================

    /**
     * Creates the root [GenerationContext] for a new generation cycle.
     * Injects the [sharedRandom] instance to ensure deterministic behavior downstream.
     */
    private fun createRootContext(): GenerationContext =
        GenerationContext(
            seededRandom = sharedRandom,
            clock = clock,
            history = emptySet(),
        )

    /**
     * The core logic for data generation, implemented as an extension function of [GenerationContext].
     *
     * By being an extension function, this method has direct access to the context's properties
     * (like `seededRandom` and `history`) via `this`.
     *
     * @receiver The current [GenerationContext].
     * @param request The generation request.
     * @return The generated value.
     */
    private fun GenerationContext.generateInternal(request: GenerationRequest): Any? {
        val generator =
            findGenerator(request)
                ?: throw GenerationFailedException(request.type, "No suitable generator found for type: ${request.type}")

        return try {
            when (generator) {
                is RecursiveGenerator -> {
                    // Recursive generators need a callback to continue generation.
                    // We pass a lambda that re-enters the 'with(ctx)' scope for the next step.
                    generator.generator(request, this) { req, ctx ->
                        with(ctx) { generateInternal(req) }
                    }
                }

                is TerminalGenerator -> {
                    // Terminal generators simply use the current context to produce a value.
                    generator.generate(request, this)
                }

                else -> throw UnsupportedGeneratorException(generator::class)
            }
        } catch (recursionEx: RecursiveGenerationFailedException) {
            handleRecursionFallback(recursionEx, request)
        } catch (e: Exception) {
            logger.error(e) { "Error generating value for ${request.name} (${request.type})" }
            throw e
        }
    }

    /**
     * Internal implementation for generating boundary values.
     */
    private fun GenerationContext.generateValidBoundariesInternal(request: GenerationRequest): List<Any?> {
        val boundaries = mutableListOf<Any?>()

        // 1. Explicit Null Constraints
        if (request.has<Null>()) return listOf(null)

        // 2. Implicit Nullability
        if (request.type.isMarkedNullable && !request.has<NotNull>()) {
            boundaries.add(null)
        }

        // 3. Delegate to strategies
        val generator = findGenerator(request)
        if (generator != null) {
            val generated =
                when (generator) {
                    is RecursiveGenerator ->
                        generator.generateValidBoundaries(request, this) { req, ctx ->
                            with(ctx) { generateInternal(req) }
                        }

                    is TerminalGenerator -> generator.generateValidBoundaries(request, this)
                    else -> emptyList()
                }
            boundaries.addAll(generated)
        }

        // 4. Fallback: Ensure at least one value exists
        if (boundaries.isEmpty()) {
            runCatching { boundaries.add(generateInternal(request)) }
        }

        return boundaries
    }

    /**
     * Internal implementation for generating invalid values.
     */
    private fun GenerationContext.generateInvalidInternal(request: GenerationRequest): List<Any?> {
        val invalids = mutableListOf<Any?>()

        // 1. Common Defense: Inject Null into Non-Nullable types
        if (!request.type.isMarkedNullable) {
            invalids.add(null)
        }

        // 2. Delegate to strategies
        val generator = findGenerator(request)
        if (generator != null) {
            val generated =
                when (generator) {
                    is RecursiveGenerator ->
                        generator.generateInvalid(request, this) { req, ctx ->
                            with(ctx) { generateInternal(req) }
                        }

                    is TerminalGenerator -> generator.generateInvalid(request, this)
                    else -> emptyList()
                }
            invalids.addAll(generated)
        }
        return invalids
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    /**
     * Handles recursion exceptions by attempting to create a Mock object instead.
     * This is the "safety net" for circular dependencies (e.g., A -> B -> A).
     */
    private fun handleRecursionFallback(
        recursionEx: RecursiveGenerationFailedException,
        request: GenerationRequest,
    ): Any {
        logger.debug { "Recursive generation detected for '${request.type}'. Attempting fallback to Mock." }

        val kClass = request.type.classifier as? KClass<*> ?: throw recursionEx

        return try {
            mockingEngine.createMock(kClass)
        } catch (mockEx: Exception) {
            val combinedEx =
                GenerationFailedException(
                    request.type,
                    "Failed to handle recursion via Mocking. (Recursion: ${recursionEx.message})",
                    mockEx,
                )
            combinedEx.addSuppressed(recursionEx)
            throw combinedEx
        }
    }

    private fun findGenerator(request: GenerationRequest): TypeGenerator? = generators.firstOrNull { it.supports(request) }

    /**
     * Performs final integrity checks on the generated result.
     * Currently enforces non-nullability constraints.
     */
    private fun validateResult(
        result: Any?,
        request: GenerationRequest,
    ) {
        if (result == null && !request.type.isMarkedNullable && !request.has<Null>()) {
            throw GenerationFailedException(
                request.type,
                "Generator returned null for non-nullable type '${request.type}'. Check the logic of the generator handling this type.",
            )
        }
    }
}

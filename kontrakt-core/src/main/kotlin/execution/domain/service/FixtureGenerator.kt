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
 * Key Responsibilities:
 * 1. **Deterministic Generation:** Uses a shared, seeded [Random] instance to ensure that
 * tests are reproducible. The same seed will always produce the same sequence of data.
 * 2. **Strategy Delegation:** Dispatches generation requests to the appropriate [TypeGenerator]
 * implementation based on the target type.
 * 3. **Integrity Validation:** Enforces type system constraints (specifically nullability)
 * on the generated values to prevent invalid states.
 * 4. **Recursion Defense & Fallback:** Detects circular dependencies in the object graph
 * and seamlessly falls back to [MockingEngine] to break the cycle.
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
     * handled before the general [ObjectGenerator] fallback.
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

    /**
     * [Primary Port]
     * Generates a single valid value based on the provided [GenerationRequest].
     *
     * This is the canonical entry point for all generation logic. It handles the lifecycle
     * of the generation context and performs final integrity checks on the result.
     * External adapters (like Mockito adapters) should convert their specific requirements
     * into a [GenerationRequest] to use this method.
     *
     * @param request The standard request object describing the generation target.
     * @return The generated value.
     * @throws GenerationFailedException If generation fails or integrity checks fail.
     */
    fun generate(request: GenerationRequest): Any? {
        ContractConfigurationValidator.validate(request)

        val context = createRootContext()
        val result = generateInternal(request, context)

        // Ensure result integrity (e.g., preventing nulls for non-nullable types)
        validateResult(result, request)

        return result
    }

    /**
     * [Convenience Port]
     * Generates a valid value for the given function parameter [param].
     *
     * This method acts as a convenience wrapper for [generate(GenerationRequest)],
     * primarily for use in test execution flows where [KParameter] is readily available.
     *
     * @param param The target parameter.
     * @return The generated value.
     */
    fun generate(param: KParameter): Any? = generate(GenerationRequest.from(param))

    /**
     * Generates a list of valid boundary values (Smart Fuzzing) for the given [param].
     *
     * These values are chosen to test the edges of the valid input space, such as:
     * - Minimum and maximum allowed values.
     * - Empty collections.
     * - Null (if the parameter is nullable).
     *
     * @param param The target parameter.
     * @return A list of valid boundary values. Guaranteed to contain at least one value if possible.
     */
    fun generateValidBoundaries(param: KParameter): List<Any?> {
        val request = GenerationRequest.from(param)

        ContractConfigurationValidator.validate(request)

        val context = createRootContext()
        val boundaries = mutableListOf<Any?>()

        // 1. Explicit Null Constraints
        if (request.has<Null>()) return listOf(null)

        // 2. Implicit Nullability
        if (request.type.isMarkedNullable && !request.has<NotNull>()) {
            boundaries.add(null)
        }

        // 3. Delegate to specific generator strategies
        val generator = findGenerator(request)
        if (generator != null) {
            val generated =
                when (generator) {
                    is RecursiveGenerator -> generator.generateValidBoundaries(request, context, ::generateInternal)
                    is TerminalGenerator -> generator.generateValidBoundaries(request, context)
                    else -> emptyList()
                }
            boundaries.addAll(generated)
        }

        // 4. Fallback: Ensure at least one value exists to keep the test running
        if (boundaries.isEmpty()) {
            try {
                boundaries.add(generateInternal(request, context))
            } catch (ignored: Exception) {
                // Ignore fallback failure during boundary analysis
            }
        }

        return boundaries
    }

    /**
     * Generates a list of invalid values (Negative Testing) for the given [param].
     *
     * These values are intended to provoke validation errors or test defense mechanisms.
     * Common examples include:
     * - Null for non-nullable parameters.
     * - Values exceeding size limits or range constraints.
     *
     * @param param The target parameter.
     * @return A list of invalid values.
     */
    fun generateInvalid(param: KParameter): List<Any?> {
        val request = GenerationRequest.from(param)

        ContractConfigurationValidator.validate(request)

        val context = createRootContext()
        val invalids = mutableListOf<Any?>()

        // 1. Common Defense: Inject Null into Non-Nullable types
        if (!request.type.isMarkedNullable) {
            invalids.add(null)
        }

        // 2. Delegate to specific generator strategies
        val generator = findGenerator(request)
        if (generator != null) {
            val generated =
                when (generator) {
                    is RecursiveGenerator -> generator.generateInvalid(request, context, ::generateInternal)
                    is TerminalGenerator -> generator.generateInvalid(request, context)
                    else -> emptyList()
                }
            invalids.addAll(generated)
        }
        return invalids
    }

    // =================================================================
    // Internal Orchestration
    // =================================================================

    /**
     * Creates the root [GenerationContext] for a new generation cycle.
     *
     * It injects the [sharedRandom] instance into the context, ensuring that
     * all downstream generators consume the same random sequence.
     */
    private fun createRootContext(): GenerationContext =
        GenerationContext(
            seededRandom = sharedRandom,
            clock = clock,
            history = emptySet(),
        )

    /**
     * The core orchestration method that handles the generation logic recursively.
     *
     * It finds the appropriate generator and executes it. If a recursive cycle is detected
     * (indicated by [RecursiveGenerationFailedException]), it attempts to recover by
     * creating a mock object using the [mockingEngine].
     *
     * @throws GenerationFailedException If generation fails unrecoverably.
     */
    private fun generateInternal(
        request: GenerationRequest,
        context: GenerationContext,
    ): Any? {
        val generator =
            findGenerator(request)
                ?: throw GenerationFailedException(request.type, "No suitable generator found for type: ${request.type}")

        return try {
            when (generator) {
                is RecursiveGenerator -> generator.generator(request, context, ::generateInternal)
                is TerminalGenerator -> generator.generate(request, context)
                else -> throw UnsupportedGeneratorException(generator::class)
            }
        } catch (recursionEx: RecursiveGenerationFailedException) {
            // Fallback Strategy: Break recursion with Mocking
            logger.debug { "Recursive generation detected for '${request.type}'. Attempting fallback to Mock." }

            val kClass = request.type.classifier as? KClass<*> ?: throw recursionEx

            try {
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
        } catch (e: Exception) {
            logger.error(e) { "Error generating value for ${request.name} (${request.type})" }
            throw e
        }
    }

    private fun findGenerator(request: GenerationRequest): TypeGenerator? = generators.firstOrNull { it.supports(request) }

    /**
     * Validates that the generated [result] complies with the type constraints of the [request].
     *
     * Currently, checks if a null value was generated for a non-nullable type.
     *
     * @throws GenerationFailedException If the result violates integrity constraints.
     */
    private fun validateResult(
        result: Any?,
        request: GenerationRequest,
    ) {
        if (result == null && !request.type.isMarkedNullable) {
            throw GenerationFailedException(
                request.type,
                "Generator returned null for non-nullable type '${request.type}'. Check the logic of the generator handling this type.",
            )
        }
    }
}

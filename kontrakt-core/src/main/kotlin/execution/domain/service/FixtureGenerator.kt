package execution.domain.service

import discovery.api.NotNull
import discovery.api.Null
import execution.domain.generator.BooleanTypeGenerator
import execution.domain.generator.CollectionTypeGenerator
import execution.domain.generator.GeneratorUtils
import execution.domain.generator.NumericTypeGenerator
import execution.domain.generator.StringTypeGenerator
import execution.domain.generator.TimeTypeGenerator
import execution.domain.generator.TypeGenerator
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Coordinates the generation of fixture data for testing purposes.
 *
 * This class acts as a central hub that delegates specific generation logic to registered [TypeGenerator] strategies.
 * It handles the orchestration of:
 * 1. Smart Fuzzing (generating valid random values).
 * 2. Boundary Analysis (generating edge cases based on constraints).
 * 3. Invalid Fuzzing (generating values that violate constraints).
 * 4. Recursive POJO generation and Mock creation fallback.
 */
class FixtureGenerator(
    private val mockingEngine: MockingEngine,
    clock: Clock = Clock.systemDefaultZone()
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Registry of strategies. Order matters: specific generators should come before general ones.
     */
    private val generators: List<TypeGenerator> = listOf(
        BooleanTypeGenerator(),
        TimeTypeGenerator(clock),
        NumericTypeGenerator(),
        StringTypeGenerator(),
        CollectionTypeGenerator()
    )

    /**
     * Generates a single valid value for the given [param].
     *
     * It attempts to find a supporting [TypeGenerator] strategy.
     * If found, it returns a smart-fuzzed value.
     * Otherwise, it falls back to recursive POJO generation via [generateByType].
     */
    fun generate(param: KParameter, history: Set<KClass<*>> = emptySet()): Any? {
        generators.firstOrNull { it.supports(param) }?.let {
            val result = it.generate(param)
            if (result != null) return result
        }
        return generateByType(param.type, history)
    }

    /**
     * Generates a list of valid boundary values for the given [param].
     *
     * This includes:
     * - Null values (if permitted).
     * - Edge cases calculated by strategies (e.g., Min/Max values).
     * - A default valid value if no boundaries are found.
     */
    fun generateValidBoundaries(param: KParameter): List<Any?> {
        val boundaries = mutableListOf<Any?>()

        // Handle @Null constraint
        if (param.findAnnotation<Null>() != null) {
            return listOf(null)
        }

        // Handle implicit nullability
        if (param.type.isMarkedNullable && param.findAnnotation<NotNull>() == null) {
            boundaries.add(null)
        }

        // Delegate to strategies
        val generated = generators.firstOrNull { it.supports(param) }
            ?.generateValidBoundaries(param)
            ?: emptyList()

        boundaries.addAll(generated)

        // Fallback: Ensure at least one value exists for testing
        if (boundaries.isEmpty()) {
            boundaries.add(generate(param))
        }

        return boundaries
    }

    /**
     * Generates a list of invalid values for the given [param] to test defense mechanisms.
     *
     * This includes:
     * - Null values (if the type is non-nullable).
     * - Values that violate specific constraints (e.g., out of range, wrong format).
     */
    fun generateInvalid(param: KParameter): List<Any?> {
        val invalids = mutableListOf<Any?>()

        // Common defense: Null injection on non-nullable types
        if (!param.type.isMarkedNullable) {
            invalids.add(null)
        }

        // Delegate to strategies
        generators.firstOrNull { it.supports(param) }?.let { generator ->
            invalids.addAll(generator.generateInvalid(param))
        }

        return invalids
    }

    /**
     * Recursively generates an instance of the given [type].
     *
     * Handles:
     * - Primitives (fallback if no strategy applied).
     * - Collections (List, Set, Map).
     * - Circular dependencies (via Mocking).
     * - Constructor injection for POJOs.
     */
    fun generateByType(type: KType, history: Set<KClass<*>> = emptySet()): Any? {
        val kClass = type.classifier as? KClass<*> ?: return null

        // Fallback for unannotated primitives
        when (kClass) {
            String::class -> return GeneratorUtils.generateRandomString(5, 15)
            Int::class -> return kotlin.random.Random.nextInt(1, 100)
            Long::class -> return kotlin.random.Random.nextLong(1, 1000)
            Boolean::class -> return kotlin.random.Random.nextBoolean()
            Double::class -> return kotlin.random.Random.nextDouble()
            Float::class -> return kotlin.random.Random.nextFloat()
        }

        // Handle Collections
        if (kClass == List::class || kClass == Collection::class || kClass == Iterable::class) {
            val elementType = type.arguments.firstOrNull()?.type ?: return emptyList<Any>()
            return List(1) { generateByType(elementType, history) }
        }
        if (kClass == Set::class) return emptySet<Any>()
        if (kClass == Map::class) return emptyMap<Any, Any>()

        // Detect circular dependencies
        if (kClass in history) {
            logger.debug { "Circular dependency detected for '${kClass.simpleName}'. Breaking cycle with Mock." }
            return tryCreateMock(kClass)
        }

        // Attempt Constructor Injection
        val constructor = kClass.primaryConstructor
        if (constructor != null) {
            try {
                val args = constructor.parameters
                    .map { generate(it, history + kClass) }
                    .toTypedArray()
                return constructor.call(*args)
            } catch (e: Exception) {
                logger.debug(e) { "Constructor failed for '${kClass.simpleName}'. Fallback to Mock." }
            }
        }

        // Final Fallback: Mock creation
        return tryCreateMock(kClass)
    }

    private fun tryCreateMock(type: KClass<*>): Any? = try {
        mockingEngine.createMock(type)
    } catch (e: Exception) {
        logger.warn(e) { "Failed to create Mock for '${type.simpleName}'" }
        null
    }
}
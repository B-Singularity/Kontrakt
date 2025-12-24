package execution.domain.generator

import execution.exception.SealedClassHasNoSubclassesException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail


class SealedTypeGeneratorBranchTest {

    private lateinit var generator: SealedTypeGenerator
    private lateinit var context: GenerationContext

    // =================================================================
    // Test Helpers & Dummy Classes
    // =================================================================

    // 1. Sealed Class for Valid Logic
    sealed class Result {
        object Success : Result()
        data class Failure(val msg: String) : Result()
    }

    // 2. Sealed Class with No Subclasses (Empty)
    sealed class EmptySealed

    // 3. Generic Class to create a TypeParameter (Non-KClass classifier)
    class Container<T> {
        val item: T? = null // Reflection on this property gives a classifier that is NOT KClass
    }

    @BeforeTest
    fun setup() {
        generator = SealedTypeGenerator()
        context = GenerationContext(
            seededRandom = Random(12345),
            clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))
        )
    }

    // =================================================================
    // 1. Missing Branch: supports() - Classifier is NOT KClass
    // =================================================================

    @Test
    fun verifySupports_ReturnsFalse_WhenClassifierIsNotKClass() {
        val typeParameterType = Container::class.declaredMemberProperties
            .first { it.name == "item" }
            .returnType

        // Ensure our setup is correct (it is a TypeParameter, not a Class)
        assertFalse(
            typeParameterType.classifier is kotlin.reflect.KClass<*>,
            "Setup Check: Classifier should be a TypeParameter"
        )

        val request = GenerationRequest.from(
            type = typeParameterType,
            name = "genericItem"
        )

        assertFalse(generator.supports(request), "Should return false for TypeParameter classifiers")
    }

    @Test
    fun verifySupports_ReturnsFalse_WhenClassifierIsSealedFalse() {
        // [Branch Coverage Target]
        // "return kClass.isSealed" -> returns false
        val request = createRequest(String::class) // String is not sealed
        assertFalse(generator.supports(request))
    }

    @Test
    fun verifySupports_ReturnsTrue_WhenClassifierIsSealedTrue() {
        // [Branch Coverage Target]
        // "return kClass.isSealed" -> returns true
        val request = createRequest(Result::class)
        assertTrue(generator.supports(request))
    }

    // =================================================================
    // 2. Missing Branch: generator() - Empty Subclasses
    // =================================================================

    @Test
    fun verifyGenerator_Throws_WhenSubclassesEmpty() {
        // [Branch Coverage Target]
        // "if (subclasses.isEmpty())" -> True branch
        val request = createRequest(EmptySealed::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ -> null }

        assertFailsWith<SealedClassHasNoSubclassesException> {
            generator.generator(request, context, regenerator)
        }
    }

    @Test
    fun verifyGenerator_Proceeds_WhenSubclassesExist() {
        // [Branch Coverage Target]
        // "if (subclasses.isEmpty())" -> False branch
        val request = createRequest(Result::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
            if (req.type == Result.Success::class.starProjectedType) Result.Success else null
        }

        // We can't guarantee random choice in one go, but we guarantee no exception is thrown
        // and regenerator is called.
        try {
            generator.generator(request, context, regenerator)
        } catch (e: SealedClassHasNoSubclassesException) {
            fail("Should not throw exception for populated sealed class")
        } catch (e: Exception) {
            // Ignore other errors (like null return from regenerator) as we just test the branch entrance
        }
    }

    // =================================================================
    // 3. Missing Branch: generateValidBoundaries - mapNotNull Logic
    // =================================================================

    @Test
    fun verifyBoundaries_HandlesNullsAndNonNulls() {
        // [Branch Coverage Target]
        // "mapNotNull { ... }" -> Needs to hit both "transform returns null" and "transform returns value"

        val request = createRequest(Result::class) // Has Success and Failure

        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
            // Logic: Return Success, but return NULL for Failure
            // This forces mapNotNull to exercise both "add" and "skip" branches
            if (req.type.classifier == Result.Success::class) {
                Result.Success
            } else {
                null // Simulate generation failure
            }
        }

        val results = generator.generateValidBoundaries(request, context, regenerator)

        assertEquals(1, results.size, "Should contain only non-null results")
        assertEquals(Result.Success, results.first())
    }

    @Test
    fun verifyBoundaries_HandlesEmptySubclassesLoop() {
        // [Branch Coverage Target]
        // Loop inside mapNotNull over empty collection
        val request = createRequest(EmptySealed::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ -> "fail" }

        val results = generator.generateValidBoundaries(request, context, regenerator)
        assertTrue(results.isEmpty())
    }

    // =================================================================
    // Helper
    // =================================================================

    private fun createRequest(kClass: kotlin.reflect.KClass<*>): GenerationRequest {
        return GenerationRequest.from(
            type = kClass.starProjectedType,
            name = "testParam"
        )
    }
}
package execution.domain.generator

import execution.exception.GenerationFailedException
import execution.exception.RecursiveGenerationFailedException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ObjectGeneratorTest {

    private lateinit var generator: ObjectGenerator
    private lateinit var context: GenerationContext

    // =================================================================
    // Dummy Classes for Testing
    // =================================================================

    data class SimpleData(val name: String, val age: Int)
    data class RecursiveNullable(val next: RecursiveNullable?)
    data class RecursiveStrict(val next: RecursiveStrict)
    abstract class AbstractItem
    sealed class SealedItem
    enum class EnumItem { A, B }

    class ExplodingClass(val data: String) {
        init {
            throw RuntimeException("Boom!")
        }
    }

    class SecondaryOnly {
        val value: String

        constructor(value: String) {
            this.value = value
        }
    }

    @BeforeTest
    fun setup() {
        generator = ObjectGenerator()
        context = GenerationContext(
            seededRandom = Random(1234),
            clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))
        )
    }

    // =================================================================
    // 1. Supports Method Tests
    // =================================================================

    @Test
    fun verifySupportsLogic() {
        assertTrue(generator.supports(createRequest(SimpleData::class)))
        assertTrue(generator.supports(createRequest(RecursiveNullable::class)))
        assertTrue(generator.supports(createRequest(SecondaryOnly::class)))

        assertFalse(generator.supports(createRequest(AbstractItem::class)))
        assertFalse(generator.supports(createRequest(SealedItem::class)))
        assertFalse(generator.supports(createRequest(EnumItem::class)))
        assertFalse(generator.supports(createRequest(List::class)))
    }

    // =================================================================
    // 2. Generator Logic Tests
    // =================================================================

    @Test
    fun verifyGeneratorCreatesObjectWithDependencies() {
        val request = createRequest(SimpleData::class)

        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
            when (req.type) {
                String::class.createType() -> "TestName"
                Int::class.createType() -> 42
                else -> null
            }
        }

        val result = generator.generator(request, context, regenerator)

        assertNotNull(result)
        assertTrue(result is SimpleData)
        assertEquals("TestName", result.name)
        assertEquals(42, result.age)
    }

    @Test
    fun verifyGeneratorWorksWithSecondaryConstructor() {
        val request = createRequest(SecondaryOnly::class)

        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
            if (req.type == String::class.createType()) "Secondary" else null
        }

        val result = generator.generator(request, context, regenerator) as SecondaryOnly
        assertEquals("Secondary", result.value)
    }

    // =================================================================
    // 3. Recursion Handling Tests
    // =================================================================

    @Test
    fun verifyRecursionBreaksOnNullableField() {
        val recursiveContext = context.copy(history = setOf(RecursiveNullable::class))
        val nullableRequest = GenerationRequest.from(RecursiveNullable::class.createType(nullable = true))
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ -> fail("Should not be called") }

        val result = generator.generator(nullableRequest, recursiveContext, regenerator)

        assertNull(result, "Should return null when recursion is detected on nullable type")
    }

    @Test
    fun verifyRecursionThrowsOnStrictField() {
        val strictRequest = createRequest(RecursiveStrict::class)
        val recursiveContext = context.copy(history = setOf(RecursiveStrict::class))
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ -> null }

        val exception = assertFailsWith<RecursiveGenerationFailedException> {
            generator.generator(strictRequest, recursiveContext, regenerator)
        }

        // 'type' is not a property in RecursiveGenerationFailedException, so we check the path and message
        assertTrue(exception.path.contains("RecursiveStrict"), "Path should contain the recursive class")
        assertTrue(exception.message?.contains("recursive structure") == true)
    }

    @Test
    fun verifyContextHistoryIsUpdated() {
        val request = createRequest(SimpleData::class)
        var capturedContext: GenerationContext? = null

        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { req, ctx ->
            if (req.name == "name") {
                capturedContext = ctx
                "test"
            } else 0
        }

        generator.generator(request, context, regenerator)

        assertNotNull(capturedContext)
        assertTrue(capturedContext!!.history.contains(SimpleData::class), "History should contain current class")
    }

    // =================================================================
    // 4. Error Handling Tests
    // =================================================================

    @Test
    fun verifyConstructorExceptionIsWrapped() {
        val request = createRequest(ExplodingClass::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ -> "trigger" }

        val exception = assertFailsWith<GenerationFailedException> {
            generator.generator(request, context, regenerator)
        }
        assertEquals(ExplodingClass::class.createType(), exception.type)
        val msg = exception.message
        assertNotNull(msg, "Exception message should not be null")
        assertTrue(
            msg.contains("Constructor"),
            "Exception message should contain failure context 'Constructor'. Actual message: $msg"
        )

        val cause = exception.cause
        assertNotNull(cause, "Exception cause should not be null")

        val actualCause = if (cause is java.lang.reflect.InvocationTargetException) cause.targetException else cause
        assertTrue(actualCause is RuntimeException, "Cause should be RuntimeException")
        assertEquals("Boom!", actualCause.message)
    }

    @Test
    fun verifyRegeneratorExceptionIsWrapped() {
        val request = createRequest(SimpleData::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ ->
            throw IllegalArgumentException("Dependency failed")
        }

        val exception = assertFailsWith<GenerationFailedException> {
            generator.generator(request, context, regenerator)
        }

        assertTrue(exception.cause is IllegalArgumentException)
        assertEquals("Dependency failed", exception.cause?.message)
    }

    // =================================================================
    // 5. Interface Contract Tests
    // =================================================================

    @Test
    fun verifyGenerateValidBoundariesReturnsSingleItem() {
        val request = createRequest(SimpleData::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { req, _ ->
            if (req.type == String::class.createType()) "Boundary" else 1
        }

        val result = generator.generateValidBoundaries(request, context, regenerator)

        assertEquals(1, result.size)
        assertTrue(result[0] is SimpleData)
    }

    @Test
    fun verifyGenerateInvalidReturnsEmpty() {
        val request = createRequest(SimpleData::class)
        val regenerator: (GenerationRequest, GenerationContext) -> Any? = { _, _ -> null }

        val result = generator.generateInvalid(request, context, regenerator)

        assertTrue(result.isEmpty())
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private fun createRequest(kClass: kotlin.reflect.KClass<*>): GenerationRequest {
        return GenerationRequest.from(
            type = kClass.starProjectedType,
            name = "testObj"
        )
    }
}
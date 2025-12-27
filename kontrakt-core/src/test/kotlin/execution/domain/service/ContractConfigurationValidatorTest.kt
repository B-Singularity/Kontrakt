package execution.domain.service

import discovery.api.AssertFalse
import discovery.api.AssertTrue
import discovery.api.Future
import discovery.api.Negative
import discovery.api.NotNull
import discovery.api.Null
import discovery.api.Past
import discovery.api.Pattern
import discovery.api.Positive
import discovery.api.Size
import discovery.api.StringLength
import execution.domain.generator.GenerationRequest
import execution.exception.InvalidAnnotationValueException
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ContractConfigurationValidatorTest {
    // =================================================================
    // 1. Mutually Exclusive Rules (Conflict Tests)
    // =================================================================

    @Test
    fun `validate throws exception when Null and NotNull are present together`() {
        val request =
            createRequest(
                type = String::class,
                annotations = listOf(createAnnotation(Null::class), createAnnotation(NotNull::class)),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("cannot be strictly marked as both"))
    }

    @Test
    fun `validate throws exception when AssertTrue and AssertFalse are present together`() {
        val request =
            createRequest(
                type = Boolean::class,
                annotations = listOf(createAnnotation(AssertTrue::class), createAnnotation(AssertFalse::class)),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("cannot be required to be both True and False"))
    }

    @Test
    fun `validate throws exception when multiple time constraints are present`() {
        val request =
            createRequest(
                type = java.time.Instant::class,
                annotations = listOf(createAnnotation(Past::class), createAnnotation(Future::class)),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("Time constraints are mutually exclusive"))
    }

    @Test
    fun `validate throws exception when Positive and Negative are present together`() {
        val request =
            createRequest(
                type = Int::class,
                annotations = listOf(createAnnotation(Positive::class), createAnnotation(Negative::class)),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("cannot be strictly Positive and strictly Negative"))
    }

    // =================================================================
    // 2. Forbidden Combination Rules
    // =================================================================

    @Test
    fun `validate throws exception when Null is combined with value constraints`() {
        val request =
            createRequest(
                type = String::class,
                annotations =
                    listOf(
                        createAnnotation(Null::class),
                        createAnnotation(Size::class, mapOf("min" to 5)),
                    ),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("cannot have value constraints"))
    }

    // =================================================================
    // 3. Type Compatibility Rules
    // =================================================================

    @Test
    fun `validate throws exception when Pattern is applied to non-String type`() {
        val request =
            createRequest(
                type = Int::class,
                annotations = listOf(createAnnotation(Pattern::class, mapOf("regexp" to "\\d+"))),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("only be applied to String or CharSequence"))
    }

    @Test
    fun `validate throws exception when Positive is applied to non-Numeric type`() {
        val request =
            createRequest(
                type = String::class,
                annotations = listOf(createAnnotation(Positive::class)),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("only be applied to Numeric types"))
    }

    @Test
    fun `validate throws exception when Future is applied to non-Temporal type`() {
        val request =
            createRequest(
                type = String::class,
                annotations = listOf(createAnnotation(Future::class)),
            )

        val exception =
            assertFailsWith<RuntimeException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("only be applied to Time/Date types"))
    }

    // =================================================================
    // 4. Annotation Value Logic Rules
    // =================================================================

    @Test
    fun `validate throws exception when Size min is negative`() {
        val request =
            createRequest(
                type = String::class,
                annotations = listOf(createAnnotation(Size::class, mapOf("min" to -1))),
            )

        val exception =
            assertFailsWith<InvalidAnnotationValueException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("min must be non-negative"))
    }

    @Test
    fun `validate throws exception when Size min is greater than max`() {
        val request =
            createRequest(
                type = String::class,
                annotations = listOf(createAnnotation(Size::class, mapOf("min" to 10, "max" to 5))),
            )

        val exception =
            assertFailsWith<InvalidAnnotationValueException> {
                ContractConfigurationValidator.validate(request)
            }
        assertTrue(exception.message!!.contains("min cannot be greater than max"))
    }

    @Test
    fun `validate passes for valid StringLength configuration`() {
        val request =
            createRequest(
                type = String::class,
                annotations = listOf(createAnnotation(StringLength::class, mapOf("min" to 1, "max" to 10))),
            )

        ContractConfigurationValidator.validate(request)
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private fun createRequest(
        type: KClass<*>,
        annotations: List<Annotation> = emptyList(),
    ): GenerationRequest =
        GenerationRequest.from(
            type = type.starProjectedType,
            annotations = annotations,
            name = "testField",
        )

    @Suppress("UNCHECKED_CAST")
    private fun <T : Annotation> createAnnotation(
        kClass: KClass<T>,
        values: Map<String, Any> = emptyMap(),
    ): T {
        val javaClass = kClass.java
        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(javaClass),
        ) { _, method, _ ->
            if (method.name == "annotationType") {
                return@newProxyInstance javaClass
            }

            if (values.containsKey(method.name)) {
                return@newProxyInstance values[method.name]
            }

            return@newProxyInstance when (method.returnType) {
                Int::class.java -> 0
                Long::class.java -> 0L
                String::class.java -> ""
                Boolean::class.java -> false
                Array<String>::class.java -> emptyArray<String>()
                else -> null
            }
        } as T
    }
}

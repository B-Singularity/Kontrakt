package execution.domain.generator

import java.lang.reflect.Proxy
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GenerationRequestTest {
    annotation class TestInfo(
        val description: String,
    )

    annotation class IgnoredInfo

    @Suppress("unused")
    fun reflectionTarget(
        @TestInfo("sample_param")
        targetParam: String,
    ) {
    }

    @Test
    fun testAnnotationsHasAndFind() {
        // Setup: Use real reflection to get a valid KParameter
        val function = ::reflectionTarget
        val kParam = function.valueParameters.first()
        val request = GenerationRequest.from(kParam)

        // Execution & Assertion
        assertTrue(request.has<TestInfo>(), "Should verify annotation presence")
        assertFalse(request.has<IgnoredInfo>(), "Should verify annotation absence")

        val found = request.find<TestInfo>()
        assertEquals("sample_param", found?.description, "Should retrieve correct annotation value")

        val notFound = request.find<IgnoredInfo>()
        assertNull(notFound, "Should return null for missing annotation")
    }

    @Test
    fun testFromKParameterWithValidName() {
        // Setup
        val function = ::reflectionTarget
        val kParam = function.valueParameters.first()

        // Execution
        val request = GenerationRequest.from(kParam)

        // Assertion
        assertEquals("targetParam", request.name)
        assertEquals(String::class.createType(), request.type)
        assertEquals(1, request.annotations.size)
    }

    @Test
    fun testFromKParameterWithNullNameFallback() {
        // Setup: Create a dynamic proxy to simulate KParameter with null name (hard to reproduce with real reflection)
        val kTypeStub = String::class.createType()

        val kParamStub =
            Proxy.newProxyInstance(
                KParameter::class.java.classLoader,
                arrayOf(KParameter::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getName" -> null
                    "getType" -> kTypeStub
                    "getAnnotations" -> emptyList<Annotation>()
                    "toString" -> "KParameterStub"
                    else -> null
                }
            } as KParameter

        // Execution
        val request = GenerationRequest.from(kParamStub)

        // Assertion
        assertEquals("[ARG]", request.name, "Should use fallback name when parameter name is null")
        assertEquals(kTypeStub, request.type)
        assertTrue(request.annotations.isEmpty())
    }

    @Test
    fun testFromKTypeDefaultValues() {
        // Setup
        val type = Int::class.createType()

        // Execution
        val request = GenerationRequest.from(type)

        // Assertion
        assertEquals("[ELEMENT]", request.name)
        assertEquals(type, request.type)
        assertTrue(request.annotations.isEmpty())
    }

    @Test
    fun testFromKTypeExplicitValues() {
        // Setup
        val type = Double::class.createType()
        val customName = "custom_element"
        val customAnnotations = listOf(TestInfo("explicit"))

        // Execution
        val request = GenerationRequest.from(type, customName, customAnnotations)

        // Assertion
        assertEquals(customName, request.name)
        assertEquals(type, request.type)
        assertEquals(customAnnotations, request.annotations)
    }

    @Test
    fun testEntryPoint() {
        // Setup
        val type = Boolean::class.createType()

        // Execution
        val request = GenerationRequest.entryPoint(type)

        // Assertion
        assertEquals("[ENTRY_POINT]", request.name)
        assertEquals(type, request.type)
        assertTrue(request.annotations.isEmpty())
    }
}

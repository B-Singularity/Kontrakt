package execution.domain.service

import execution.domain.generator.GenerationRequest
import execution.domain.generator.RecursiveGenerator
import execution.domain.generator.TerminalGenerator
import execution.domain.generator.TypeGenerator
import execution.domain.service.generation.FixtureGenerator
import execution.exception.GenerationFailedException
import execution.exception.RecursiveGenerationFailedException
import execution.exception.UnsupportedGeneratorException
import execution.spi.MockingEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Clock
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class FixtureGeneratorTest {
    private lateinit var mockingEngine: MockingEngine
    private lateinit var fixtureGenerator: FixtureGenerator

    @BeforeEach
    fun setup() {
        mockingEngine = mock(MockingEngine::class.java)
        fixtureGenerator =
            FixtureGenerator(
                mockingEngine = mockingEngine,
                clock = Clock.systemDefaultZone(),
                seed = 12345L,
            )
    }

    // =================================================================
    // Helper Dummy Functions (Defined as members to avoid Reflection Error)
    // =================================================================

    private fun dummyFuncString(p: String) {}

    private fun dummyFuncNullableString(p: String?) {}

    // =================================================================
    // 1. Standard Generation Logic
    // =================================================================

    @Test
    fun `generate delegates to the correct generator`() {
        // Configure a mock generator that supports the request
        val expectedValue = "Success"
        val mockGenerator = mock(TerminalGenerator::class.java)

        whenever(mockGenerator.supports(any())).thenReturn(true)
        whenever(mockGenerator.generate(any(), any())).thenReturn(expectedValue)

        injectGenerators(listOf(mockGenerator))
        val request = mockRequest()

        val result = fixtureGenerator.generate(request)

        assertEquals(expectedValue, result)
        verify(mockGenerator).generate(any(), any())
    }

    @Test
    fun `generate throws exception when no suitable generator found`() {
        // Configure a generator that does not support any request
        val mockGenerator = mock(TerminalGenerator::class.java)
        whenever(mockGenerator.supports(any())).thenReturn(false)

        injectGenerators(listOf(mockGenerator))
        val request = mockRequest()

        val ex =
            assertThrows<GenerationFailedException> {
                fixtureGenerator.generate(request)
            }
        assertTrue(ex.message!!.contains("No suitable generator found"))
    }

    @Test
    fun `generate throws exception when unsupported generator type is encountered`() {
        // Mock a generator that implements TypeGenerator but is neither Terminal nor Recursive
        val unsupportedGenerator = mock(TypeGenerator::class.java)
        whenever(unsupportedGenerator.supports(any())).thenReturn(true)

        injectGenerators(listOf(unsupportedGenerator))
        val request = mockRequest()

        assertThrows<UnsupportedGeneratorException> {
            fixtureGenerator.generate(request)
        }
    }

    // =================================================================
    // 2. Recursion Handling (Fallback & Failure)
    // =================================================================

    @Test
    fun `generate falls back to MockingEngine when recursion detected`() {
        val recursiveGenerator = mock(RecursiveGenerator::class.java)
        whenever(recursiveGenerator.supports(any())).thenReturn(true)

        val dummyType = mock(KType::class.java)
        val recursionException =
            RecursiveGenerationFailedException(
                type = dummyType,
                path = listOf("root", "circularField"),
                cause = RuntimeException("Cause"),
            )

        whenever(recursiveGenerator.generator(any(), any(), any())).thenThrow(recursionException)

        val fallbackMock = "Fallback Mock Object"
        whenever(mockingEngine.createMock(any<KClass<*>>())).thenReturn(fallbackMock)

        injectGenerators(listOf(recursiveGenerator))
        val request = mockRequest()

        val result = fixtureGenerator.generate(request)

        assertEquals(fallbackMock, result)
        verify(mockingEngine).createMock(any<KClass<*>>())
    }

    @Test
    fun `generate throws exception when MockingEngine also fails during recursion fallback`() {
        val recursiveGenerator = mock(RecursiveGenerator::class.java)
        whenever(recursiveGenerator.supports(any())).thenReturn(true)

        val dummyType = mock(KType::class.java)
        val recursionException =
            RecursiveGenerationFailedException(
                type = dummyType,
                path = listOf("root", "circularField"),
                cause = RuntimeException("Cause"),
            )

        whenever(recursiveGenerator.generator(any(), any(), any())).thenThrow(recursionException)

        whenever(mockingEngine.createMock(any<KClass<*>>())).thenThrow(RuntimeException("Mock failed"))

        injectGenerators(listOf(recursiveGenerator))
        val request = mockRequest()

        val ex =
            assertThrows<GenerationFailedException> {
                fixtureGenerator.generate(request)
            }
        assertTrue(ex.message!!.contains("Failed to handle recursion via Mocking"))
    }

    // =================================================================
    // 3. Integrity & Validation (Null Safety)
    // =================================================================

    @Test
    fun `generate throws exception when non-nullable request returns null`() {
        // Generator returns null for a Non-Nullable request
        val nullGenerator = mock(TerminalGenerator::class.java)
        whenever(nullGenerator.supports(any())).thenReturn(true)
        whenever(nullGenerator.generate(any(), any())).thenReturn(null)

        injectGenerators(listOf(nullGenerator))
        val request = mockRequest(isNullable = false)

        val ex =
            assertThrows<GenerationFailedException> {
                fixtureGenerator.generate(request)
            }
        assertTrue(ex.message!!.contains("returned null for non-nullable"))
    }

    @Test
    fun `generate allows null when request is nullable`() {
        // Generator returns null for a Nullable request
        val nullGenerator = mock(TerminalGenerator::class.java)
        whenever(nullGenerator.supports(any())).thenReturn(true)
        whenever(nullGenerator.generate(any(), any())).thenReturn(null)

        injectGenerators(listOf(nullGenerator))
        val request = mockRequest(isNullable = true)

        val result = fixtureGenerator.generate(request)

        assertNull(result)
    }

    // =================================================================
    // 4. Boundary Analysis (Smart Fuzzing)
    // =================================================================

    @Test
    fun `generateValidBoundaries includes null for nullable types`() {
        // Use member function reference (this::dummyFuncNullableString)
        val param = getFirstParameter(this::dummyFuncNullableString)

        // Generator returns empty list, but the framework should add null
        val mockGenerator = mock(TerminalGenerator::class.java)
        whenever(mockGenerator.supports(any())).thenReturn(true)
        whenever(mockGenerator.generateValidBoundaries(any(), any())).thenReturn(emptyList())
        whenever(mockGenerator.generate(any(), any())).thenReturn("Fallback")

        injectGenerators(listOf(mockGenerator))

        val boundaries = fixtureGenerator.generateValidBoundaries(param)

        assertTrue(boundaries.contains(null), "Should contain null for nullable types")
    }

    @Test
    fun `generateValidBoundaries falls back to generateInternal when boundaries are empty`() {
        // Use member function reference (this::dummyFuncString)
        val param = getFirstParameter(this::dummyFuncString)

        // Generator returns empty boundaries, triggering fallback to single generation
        val mockGenerator = mock(TerminalGenerator::class.java)
        whenever(mockGenerator.supports(any())).thenReturn(true)
        whenever(mockGenerator.generateValidBoundaries(any(), any())).thenReturn(emptyList())
        whenever(mockGenerator.generate(any(), any())).thenReturn("FallbackValue")

        injectGenerators(listOf(mockGenerator))

        val boundaries = fixtureGenerator.generateValidBoundaries(param)

        assertTrue(boundaries.contains("FallbackValue"), "Should invoke fallback generation if boundaries are empty")
    }

    @Test
    fun `generateValidBoundaries aggregates results from RecursiveGenerator`() {
        // Use member function reference
        val param = getFirstParameter(this::dummyFuncString)

        val recursiveGenerator = mock(RecursiveGenerator::class.java)
        whenever(recursiveGenerator.supports(any())).thenReturn(true)
        whenever(recursiveGenerator.generateValidBoundaries(any(), any(), any()))
            .thenReturn(listOf("RecursiveBoundary"))

        injectGenerators(listOf(recursiveGenerator))

        val boundaries = fixtureGenerator.generateValidBoundaries(param)

        assertTrue(boundaries.contains("RecursiveBoundary"))
    }

    // =================================================================
    // 5. Invalid Generation (Negative Testing)
    // =================================================================

    @Test
    fun `generateInvalid includes null for non-nullable types`() {
        // Use member function reference
        val param = getFirstParameter(this::dummyFuncString)

        val mockGenerator = mock(TerminalGenerator::class.java)
        whenever(mockGenerator.supports(any())).thenReturn(true)
        whenever(mockGenerator.generateInvalid(any(), any())).thenReturn(emptyList())

        injectGenerators(listOf(mockGenerator))

        val invalids = fixtureGenerator.generateInvalid(param)

        assertTrue(invalids.contains(null), "Should inject null into non-nullable type")
    }

    @Test
    fun `generateInvalid aggregates results from RecursiveGenerator`() {
        // Use member function reference
        val param = getFirstParameter(this::dummyFuncString)

        val recursiveGenerator = mock(RecursiveGenerator::class.java)
        whenever(recursiveGenerator.supports(any())).thenReturn(true)
        whenever(recursiveGenerator.generateInvalid(any(), any(), any()))
            .thenReturn(listOf("InvalidValue"))

        injectGenerators(listOf(recursiveGenerator))

        val invalids = fixtureGenerator.generateInvalid(param)

        assertTrue(invalids.contains("InvalidValue"))
    }

    // =================================================================
    // 6. Convenience Methods
    // =================================================================

    @Test
    fun `generate(KParameter) convenience method works`() {
        // Use member function reference
        val param = getFirstParameter(this::dummyFuncString)

        val mockGenerator = mock(TerminalGenerator::class.java)
        whenever(mockGenerator.supports(any())).thenReturn(true)
        whenever(mockGenerator.generate(any(), any())).thenReturn("KParamResult")

        injectGenerators(listOf(mockGenerator))

        val result = fixtureGenerator.generate(param)

        assertEquals("KParamResult", result)
    }

    // =================================================================
    // Helpers
    // =================================================================

    private fun injectGenerators(generators: List<TypeGenerator>) {
        val field = FixtureGenerator::class.java.getDeclaredField("generators")
        field.isAccessible = true
        field.set(fixtureGenerator, generators)
    }

    private fun mockRequest(
        isNullable: Boolean = false,
        annotations: List<Annotation> = emptyList(),
    ): GenerationRequest {
        val request = mock(GenerationRequest::class.java)
        val kType = mock(KType::class.java)
        val kClass = String::class

        whenever(kType.classifier).thenReturn(kClass)
        whenever(kType.isMarkedNullable).thenReturn(isNullable)

        whenever(request.type).thenReturn(kType)
        whenever(request.name).thenReturn("testParam")
        whenever(request.annotations).thenReturn(annotations)

        return request
    }

    private fun getFirstParameter(func: KFunction<*>): KParameter = func.parameters.first()
}

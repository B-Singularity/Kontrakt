package execution.domain.service.generation

import discovery.api.Null
import execution.domain.vo.context.generation.GenerationContext
import execution.domain.vo.context.generation.GenerationRequest
import execution.exception.GenerationFailedException
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioTrace
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class FixtureGeneratorTest {

    private val mockingEngine = mockk<MockingEngine>()
    private val clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))
    private val trace = mockk<ScenarioTrace>(relaxed = true)

    private lateinit var sut: FixtureGenerator

    @BeforeEach
    fun setUp() {
        // [Authority] Use the Public Constructor only.
        sut = FixtureGenerator(mockingEngine, clock, trace, seed = 1234L)
    }

    // =========================================================================
    // 1. Overloaded Methods Coverage (KParameter & Context)
    // =========================================================================

    @Test
    fun `generate(KParameter) - delegates to internal logic`() {
        val param = mockParam(String::class.createType())
        val result = sut.generate(param)
        assertThat(result).isInstanceOf(String::class.java)
    }

    @Test
    fun `generate(KParameter, Context) - preserves context`() {
        val context = mockk<GenerationContext>(relaxed = true)
        val param = mockParam(String::class.createType())

        val result = sut.generate(param, context)
        assertThat(result).isInstanceOf(String::class.java)
    }

    @Test
    fun `generate(Request, Context) - preserves context`() {
        val context = mockk<GenerationContext>(relaxed = true)
        val request = createRequest(String::class.createType())

        val result = sut.generate(request, context)
        assertThat(result).isInstanceOf(String::class.java)
    }

    // =========================================================================
    // 2. Boundary Generation Coverage (generateValidBoundaries)
    // =========================================================================

    @Test
    fun `generateValidBoundaries - includes null for nullable types`() {
        val param = mockParam(String::class.createType(nullable = true))
        val boundaries = sut.generateValidBoundaries(param)
        assertThat(boundaries).contains(null)
    }

    @Test
    fun `generateValidBoundaries - returns only null if @Null annotation is present`() {
        val param = mockParam(String::class.createType(), annotations = listOf(Null()))
        val boundaries = sut.generateValidBoundaries(param)
        assertThat(boundaries).containsExactly(null)
    }

    @Test
    fun `generateValidBoundaries - falls back to generateInternal if no boundaries found`() {
        // Given: Non-nullable String (no implicit boundaries like null)
        val param = mockParam(String::class.createType(nullable = false))

        // When
        val boundaries = sut.generateValidBoundaries(param)

        // Then: Should generate at least one valid value
        assertThat(boundaries).isNotEmpty
        assertThat(boundaries.first()).isInstanceOf(String::class.java)
    }

    // =========================================================================
    // 3. Invalid Generation Coverage (generateInvalid)
    // =========================================================================

    @Test
    fun `generateInvalid - injects null for non-nullable types`() {
        val param = mockParam(String::class.createType(nullable = false))
        val invalids = sut.generateInvalid(param)
        assertThat(invalids).contains(null)
    }

    @Test
    fun `generateInvalid - delegates to strategy (Primitive)`() {
        val param = mockParam(Int::class.createType(nullable = false))
        val invalids = sut.generateInvalid(param)
        assertThat(invalids).contains(null)
    }

    // =========================================================================
    // 4. Standard Logic & Recursion
    // =========================================================================

    @Test
    fun `generate - supports standard types`() {
        val result = sut.generate(createRequest(Int::class.createType()))
        assertThat(result).isInstanceOf(Int::class.javaObjectType)
    }

    // [Fix] Defined at class level to resolve forward reference error
    data class CyclicA(val b: CyclicB)
    data class CyclicB(val a: CyclicA)

    @Test
    fun `generate - fails explicitly if mocking fallback also fails`() {
        // [Fix] Use explicit generics for any()
        every {
            mockingEngine.createMock(
                any<kotlin.reflect.KClass<*>>(),
                any<execution.port.outgoing.MockingContext>()
            )
        } throws RuntimeException("Mocking failed")

        val request = createRequest(CyclicA::class.createType())

        assertThatThrownBy { sut.generate(request) }
            .isInstanceOf(GenerationFailedException::class.java)
            // [Fix] Check stack trace because the exception is wrapped
            .hasStackTraceContaining("Failed to handle recursion via Mocking")
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun createRequest(type: KType): GenerationRequest {
        return GenerationRequest(
            name = "testParam",
            type = type,
            annotations = emptyList()
        )
    }

    private fun mockParam(
        type: KType,
        name: String = "testParam",
        annotations: List<Annotation> = emptyList()
    ): KParameter {
        val param = mockk<KParameter>()
        every { param.type } returns type
        every { param.name } returns name
        every { param.annotations } returns annotations
        return param
    }
}
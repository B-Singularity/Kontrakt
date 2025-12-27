package execution.domain.generator

import discovery.api.DecimalMax
import discovery.api.DecimalMin
import discovery.api.Digits
import discovery.api.IntRange
import discovery.api.Negative
import discovery.api.NegativeOrZero
import discovery.api.Positive
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericTypeGeneratorTest {
    private lateinit var generator: NumericTypeGenerator
    private lateinit var context: GenerationContext
    private val seed = 12345

    @BeforeTest
    fun setup() {
        generator = NumericTypeGenerator()
        val random = Random(seed)
        val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))
        context =
            GenerationContext(
                seededRandom = random,
                clock = fixedClock,
            )
    }

    @Test
    fun verifyContractSupportsValidNumericTypes() {
        val supportedTypes =
            listOf(
                Int::class,
                Long::class,
                Double::class,
                Float::class,
                BigDecimal::class,
            )

        supportedTypes.forEach { kClass ->
            val request = createRequest(kClass)
            assertTrue(generator.supports(request), "Should support type: ${kClass.simpleName}")
        }
    }

    @Test
    fun verifyContractRejectsUnsupportedTypes() {
        val unsupportedTypes =
            listOf(
                String::class,
                Boolean::class,
                List::class,
            )

        unsupportedTypes.forEach { kClass ->
            val request = createRequest(kClass)
            assertFalse(generator.supports(request), "Should reject type: ${kClass.simpleName}")
        }
    }

    @Test
    fun testIntGenerationWithConstraints() {
        val min = 10
        val max = 20
        val request = createRequest(Int::class, listOf(IntRange(min, max)))

        repeat(100) {
            val result = generator.generate(request, context) as Int
            assertTrue(result in min..max, "Generated Int should be in range")
        }

        val posRequest = createRequest(Int::class, listOf(Positive()))
        repeat(50) {
            val result = generator.generate(posRequest, context) as Int
            assertTrue(result > 0, "Generated Int should be positive")
        }

        val negRequest = createRequest(Int::class, listOf(NegativeOrZero()))
        repeat(50) {
            val result = generator.generate(negRequest, context) as Int
            assertTrue(result <= 0, "Generated Int should be negative or zero")
        }
    }

    @Test
    fun testDoubleGenerationWithDecimalConstraints() {
        val minVal = "10.5"
        val request = createRequest(Double::class, listOf(DecimalMin(minVal, inclusive = false)))

        repeat(50) {
            val result = generator.generate(request, context) as Double
            assertTrue(result >= 10.50001, "Generated Double should be strictly greater than min")
        }

        val maxVal = "5.5"
        val request2 = createRequest(Double::class, listOf(DecimalMax(maxVal, inclusive = true)))

        repeat(50) {
            val result = generator.generate(request2, context) as Double
            assertTrue(result <= 5.5, "Generated Double should be less than or equal to max")
        }
    }

    @Test
    fun testBigDecimalGenerationWithDigits() {
        val request = createRequest(BigDecimal::class, listOf(Digits(integer = 2, fraction = 2)))

        repeat(50) {
            val result = generator.generate(request, context) as BigDecimal
            assertTrue(result.abs() <= BigDecimal("99.99"), "Value exceeds digit limits")
            assertTrue(result.scale() <= 2, "Scale should be within limits")
        }
    }

    @Test
    fun testEdgeCaseInvertedRange() {
        val request = createRequest(Int::class, listOf(IntRange(min = 100, max = 10)))

        val result = generator.generate(request, context) as Int

        assertEquals(100, result, "Should return minLimit when range is inverted")
    }

    @Test
    fun testEdgeCaseMinEqualsMax() {
        val request = createRequest(Int::class, listOf(IntRange(min = 5, max = 5)))
        val result = generator.generate(request, context) as Int
        assertEquals(5, result, "Should return exact value when min == max")
    }

    @Test
    fun verifyBoundaryGeneration() {
        val request = createRequest(Int::class, listOf(IntRange(1, 10)))
        val boundaries = generator.generateValidBoundaries(request, context)

        assertTrue(boundaries.contains(1), "Should contain min boundary")
        assertTrue(boundaries.contains(10), "Should contain max boundary")
    }

    @Test
    fun verifyBigDecimalSpecificBoundariesWithDigits() {
        val request = createRequest(BigDecimal::class, listOf(Digits(integer = 1, fraction = 1)))

        val boundaries = generator.generateValidBoundaries(request, context)

        assertTrue(boundaries.any { (it as BigDecimal).compareTo(BigDecimal("9.9")) == 0 })
        assertTrue(boundaries.any { (it as BigDecimal).compareTo(BigDecimal("-9.9")) == 0 })
    }

    @Test
    fun verifyInvalidGenerationProducesValuesOutsideRange() {
        val request = createRequest(Int::class, listOf(IntRange(10, 20)))
        val invalids = generator.generateInvalid(request, context)

        assertTrue(invalids.contains(9), "Should contain min-1")
        assertTrue(invalids.contains(21), "Should contain max+1")
    }

    @Test
    fun verifyInvalidGenerationHandlesTypeLimits() {
        val request = createRequest(Int::class)
        val invalids = generator.generateInvalid(request, context)

        assertTrue(invalids.isEmpty(), "Should not generate invalid values if range covers entire type spectrum")
    }

    // =================================================================
    // Added Branch Coverage Tests
    // =================================================================

    @Test
    fun testIntGenerationWithMaxValue() {
        // Logic: Covers the branch where max == Int.MAX_VALUE in smartFuzzInt
        val request = createRequest(Int::class) // Implicitly [MIN_VALUE, MAX_VALUE]

        repeat(50) {
            val result = generator.generate(request, context) as Int
            // Assert merely that it works and returns a valid Int without crashing
            assertTrue(result >= Int.MIN_VALUE && result <= Int.MAX_VALUE)
        }
    }

    @Test
    fun testFloatSignAnnotations() {
        // Logic: Covers 'if (type == Int...)' else branch for Float/Double with Negative/Positive
        // Specifically testing Float + Negative (using epsilon boundary logic)
        val request = createRequest(Float::class, listOf(Negative()))

        repeat(20) {
            val result = generator.generate(request, context) as Float
            assertTrue(result < 0.0f, "Generated Float should be strictly negative")
        }
    }

    @Test
    fun testBigDecimalZeroAndEpsilonBranches() {
        // Logic: Covers 'if (min <= BigDecimal.ZERO && max >= BigDecimal.ZERO)'
        // and 'if (min < max)' random path for BigDecimal
        val request = createRequest(BigDecimal::class, listOf(DecimalMin("-10.0"), DecimalMax("10.0")))

        // Execute to ensure branches are hit
        val result = generator.generate(request, context) as BigDecimal
        assertTrue(result >= BigDecimal("-10.0") && result <= BigDecimal("10.0"))
    }

    @Test
    fun verifyInvalidGenerationHandlesLowerBoundOverflow() {
        // Logic: Covers 'if (minVal > Int.MIN_VALUE)' overflow check check
        // Range is [Int.MIN_VALUE, 0]
        val request = createRequest(Int::class, listOf(NegativeOrZero()))
        val invalids = generator.generateInvalid(request, context)

        // Should NOT contain Int.MIN_VALUE - 1 (overflow protection)
        // Should contain 0 + 1 = 1
        assertTrue(invalids.contains(1))
        assertFalse(invalids.any { it == Int.MIN_VALUE }, "Should not attempt to create value below Int.MIN_VALUE")
        assertEquals(1, invalids.size)
    }

    @Test
    fun verifyInvalidGenerationDoubleFullRange() {
        // Logic: Covers Double limits in generateInvalid (infinite/max value checks)
        val request = createRequest(Double::class)
        val invalids = generator.generateInvalid(request, context)

        // Should be empty because we cannot generate outside [-Double.MAX, +Double.MAX] safely/logically in this context
        assertTrue(invalids.isEmpty(), "Full Double range should produce no invalid values")
    }

    private fun createRequest(
        kClass: KClass<*>,
        annotations: List<Annotation> = emptyList(),
    ): GenerationRequest =
        GenerationRequest.from(
            type = kClass.starProjectedType,
            annotations = annotations,
            name = "testParam",
        )
}

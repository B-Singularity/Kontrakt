package execution.domain.generator

import discovery.api.DecimalMax
import discovery.api.DecimalMin
import discovery.api.Digits
import discovery.api.DoubleRange
import discovery.api.IntRange
import discovery.api.LongRange
import discovery.api.Negative
import discovery.api.NegativeOrZero
import discovery.api.Positive
import discovery.api.PositiveOrZero
import java.math.BigDecimal
import kotlin.reflect.KParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericTypeGeneratorTest {

    private val generator = NumericTypeGenerator()

    @Suppress("UNUSED_PARAMETER")
    fun annotationStub(
        // [Existing Cases]
        @IntRange(min = 1, max = 10) simpleRange: Int,
        @IntRange(min = -50, max = 50) @Positive overlapInt: Int,
        @DecimalMin("10.5") @DecimalMax("20.5") decimalRange: BigDecimal,
        @DoubleRange(min = -10.0, max = 10.0) @Negative overlapDouble: Double,
        @Digits(integer = 3, fraction = 0) @PositiveOrZero digitWithSign: BigDecimal,

        // [New Cases for High Coverage]

        // 1. Unsupported Type
        stringParam: String,

        // 2. Long Type & LongRange
        @LongRange(min = 100L, max = 200L) longParam: Long,

        // 3. Float Type (Untested branch)
        @Positive floatParam: Float,

        // 4. Decimal Exclusive (Hits 'inclusive = false' branch)
        @DecimalMin("0.0", inclusive = false) @DecimalMax("1.0", inclusive = false) exclusiveDecimal: BigDecimal,

        // 5. NegativeOrZero (Untested branch)
        @NegativeOrZero negZeroInt: Int,

        // 6. Single Value Range (Hits 'min == max' optimization branch)
        @IntRange(min = 10, max = 10) fixedInt: Int,

        // 7. Range Conflict (Min > Max) - Safety check branch
        @IntRange(min = 100, max = 50) conflictInt: Int,

        // 8. Full Integer Range (Hits 'Int.MAX_VALUE' checks in smartFuzz & generateInvalid)
        @IntRange(min = Int.MIN_VALUE, max = Int.MAX_VALUE) extremeInt: Int,

        // 9. Full Long Range (Hits 'Long.MAX_VALUE' checks in smartFuzz & generateInvalid)
        @LongRange(min = Long.MIN_VALUE, max = Long.MAX_VALUE) extremeLong: Long,

        // 10. Pure Digits (Hits 'maxLimit.min(maxVal)' in calculateEffectiveRange)
        // integer=2, fraction=2 -> Max 99.99, Min -99.99
        @Digits(integer = 2, fraction = 2) simpleDigits: BigDecimal,

        // 11. Positive Double (Hits the '0.00001' epsilon branch, distinct from Int '1')
        @Positive tinyDouble: Double,

        // 12. Positive Long (Hits the '1' branch for Long types)
        @Positive positiveLong: Long,

        // 13. Negative Float (Hits the '-0.00001' epsilon branch for Floating types)
        @Negative negativeFloat: Float,

        // 14. Zero Crossing Double (Hits 'min <= 0.0 && max >= 0.0' branch in smartFuzzDouble)
        @DoubleRange(min = -5.0, max = 5.0) crossingDouble: Double
    ) {
    }

    private fun getParameter(name: String): KParameter {
        val func = ::annotationStub
        return func.parameters.find { it.name == name }
            ?: throw IllegalArgumentException("Parameter '$name' not found in stub function")
    }

    @Test
    fun `supports - should return true for all numeric types`() {
        assertTrue(generator.supports(getParameter("simpleRange")))
        assertTrue(generator.supports(getParameter("longParam")))
        assertTrue(generator.supports(getParameter("floatParam")))
        assertTrue(generator.supports(getParameter("overlapDouble")))
        assertTrue(generator.supports(getParameter("decimalRange")))
    }

    @Test
    fun `supports - should return false for unsupported types`() {
        assertFalse(generator.supports(getParameter("stringParam")))
    }


    @Test
    fun `generate - should handle Long type correctly`() {
        val param = getParameter("longParam")
        repeat(20) {
            val value = generator.generate(param)
            assertTrue(value is Long, "Should generate Long")
            val longVal = value as Long
            assertTrue(longVal in 100L..200L)
        }

        // Also test generateInvalid for Long branch
        val invalids = generator.generateInvalid(param)
        assertTrue(invalids.contains(99L) || invalids.contains(201L))
    }

    @Test
    fun `generate - should handle Float type correctly`() {
        val param = getParameter("floatParam")
        repeat(20) {
            val value = generator.generate(param)
            assertTrue(value is Float, "Should generate Float")
            assertTrue((value as Float) > 0f)
        }

        // Test boundaries for Float
        val boundaries = generator.generateValidBoundaries(param)
        assertTrue(boundaries.isNotEmpty())
    }

    @Test
    fun `generateValidBoundaries - should calculate intersection of IntRange and Positive`() {
        val param = getParameter("overlapInt")

        val boundaries = generator.generateValidBoundaries(param)

        assertTrue(boundaries.contains(1), "Min boundary should be 1 due to @Positive")
        assertTrue(boundaries.contains(50), "Max boundary should remain 50")
        assertFalse(boundaries.contains(-50), "Original min -50 should be ignored")
    }

    @Test
    fun `generateValidBoundaries - should respect inclusive=false in DecimalMinMax`() {
        // Hits the 'else' block where 0.00001 is added/subtracted
        val param = getParameter("exclusiveDecimal") // (0.0, 1.0)
        val boundaries = generator.generateValidBoundaries(param)

        val minBD = boundaries.minOf { it as BigDecimal }
        val maxBD = boundaries.maxOf { it as BigDecimal }

        // Expected: 0.0 + epsilon, 1.0 - epsilon
        assertTrue(minBD > BigDecimal.ZERO, "Min should be slightly greater than 0.0")
        assertTrue(maxBD < BigDecimal.ONE, "Max should be slightly less than 1.0")
    }

    @Test
    fun `generate - should generate values within the intersected range (Int)`() {
        val param = getParameter("overlapInt")

        repeat(100) {
            val result = generator.generate(param) as Int
            assertTrue(
                result in 1..50,
                "Generated value $result should be within effective range [1, 50]"
            )
        }
    }

    @Test
    fun `generateValidBoundaries - should respect DecimalMin and DecimalMax together`() {
        val param = getParameter("decimalRange")

        val boundaries = generator.generateValidBoundaries(param)

        val min = BigDecimal("10.5")
        val max = BigDecimal("20.5")

        assertTrue(boundaries.contains(min), "Should contain explicit min")
        assertTrue(boundaries.contains(max), "Should contain explicit max")
    }

    @Test
    fun `generate - should generate Double respecting Negative constraint on a wide range`() {
        val param = getParameter("overlapDouble")

        repeat(50) {
            val result = generator.generate(param) as Double
            assertTrue(result < 0.0, "Value should be negative")
            assertTrue(result >= -10.0, "Value should be greater than or equal to -10.0")
        }
    }

    @Test
    fun `generateValidBoundaries - should intersect Digits and PositiveOrZero`() {
        // @Digits(integer=3) [-999, 999] AND @PositiveOrZero [0, Infinity]
        val param = getParameter("digitWithSign")

        val boundaries = generator.generateValidBoundaries(param)
        val boundaryValues = boundaries.map { it as BigDecimal }

        // Max possible for 3 digits is 999
        val maxPossible = BigDecimal("999")
        // Min possible should be 0 (clipped by PositiveOrZero)
        val minPossible = BigDecimal.ZERO

        assertTrue(boundaryValues.any { it.compareTo(maxPossible) == 0 }, "Should contain max 999")
        assertTrue(boundaryValues.any { it.compareTo(minPossible) == 0 }, "Should contain min 0")

        // Ensure no negative values exist in boundaries
        assertFalse(boundaryValues.any { it < BigDecimal.ZERO }, "Should not contain negative boundaries")
    }

    @Test
    fun `generateInvalid - should generate values outside the intersected valid range`() {
        // @IntRange(-50, 50) AND @Positive -> Effective [1, 50]
        val param = getParameter("overlapInt")

        val invalids = generator.generateInvalid(param)

        // Invalid values should be <= 0 OR > 50
        invalids.forEach {
            val valInt = it as Int
            val isInvalid = valInt <= 0 || valInt > 50
            assertTrue(isInvalid, "Invalid value $valInt should be outside [1, 50]")
        }

        // Specifically check if 0 or negative numbers are suggested as invalid
        assertTrue(invalids.any { (it as Int) <= 0 }, "Should suggest non-positive numbers as invalid")
    }

    @Test
    fun `generate - should respect NegativeOrZero`() {
        // Hits the 'has<NegativeOrZero>' branch
        val param = getParameter("negZeroInt")
        repeat(20) {
            val value = generator.generate(param) as Int
            assertTrue(value <= 0, "Value $value should be <= 0")
        }

        val boundaries = generator.generateValidBoundaries(param)
        assertTrue(boundaries.contains(0), "Boundaries should contain 0")
    }

    @Test
    fun `generate - should handle fixed range (min == max)`() {
        // Hits the 'if (min == max)' optimization branch in smartFuzz
        val param = getParameter("fixedInt") // 10..10

        repeat(5) {
            assertEquals(10, generator.generate(param))
        }

        val boundaries = generator.generateValidBoundaries(param)
        assertEquals(1, boundaries.size, "Should only have 1 boundary for fixed range")
        assertEquals(10, boundaries[0])
    }

    @Test
    fun `calculateEffectiveRange - should handle conflicting range by favoring min`() {
        // Hits the 'if (minLimit > maxLimit)' safety branch
        // @IntRange(min=100, max=50) -> This is impossible. Logic says return minLimit for both.
        val param = getParameter("conflictInt")

        val value = generator.generate(param)
        assertEquals(100, value, "Should fallback to min when range is invalid")
    }

    @Test
    fun `generateInvalid - should generate correct types for invalid inputs`() {
        // Double branch
        val doubleParam = getParameter("overlapDouble")
        val invalidDoubles = generator.generateInvalid(doubleParam)
        assertTrue(invalidDoubles.all { it is Double })

        // BigDecimal branch
        val decimalParam = getParameter("decimalRange")
        val invalidDecimals = generator.generateInvalid(decimalParam)
        assertTrue(invalidDecimals.all { it is BigDecimal })
    }

    @Test
    fun `generate - should handle full Int range without overflow`() {
        // This targets the 'if (max == Int.MAX_VALUE)' branch in smartFuzzInt
        val param = getParameter("extremeInt")
        repeat(50) {
            val result = generator.generate(param) as Int
            // Just ensure it doesn't crash and returns a valid Int
            assertTrue(result >= Int.MIN_VALUE && result <= Int.MAX_VALUE)
        }
    }

    @Test
    fun `generateInvalid - should return empty list for full Int range`() {
        // This targets the 'if (min > Int.MIN_VALUE)' check.
        // Since min IS Int.MIN_VALUE, it should skip adding (min - 1).
        val param = getParameter("extremeInt")
        val invalids = generator.generateInvalid(param)

        assertTrue(invalids.isEmpty(), "Cannot generate invalid integer outside of full Int range")
    }

    @Test
    fun `generate - should handle full Long range without overflow`() {
        // This targets the 'if (max == Long.MAX_VALUE)' branch in smartFuzzLong
        val param = getParameter("extremeLong")
        repeat(50) {
            val result = generator.generate(param) as Long
            assertTrue(result >= Long.MIN_VALUE && result <= Long.MAX_VALUE)
        }
    }

    @Test
    fun `generateInvalid - should return empty list for full Long range`() {
        // Targets 'if (min > Long.MIN_VALUE)' branch avoidance
        val param = getParameter("extremeLong")
        val invalids = generator.generateInvalid(param)
        assertTrue(invalids.isEmpty(), "Cannot generate invalid long outside of full Long range")
    }

    @Test
    fun `generateValidBoundaries - should calculate limits based on Digits annotation`() {
        // @Digits(integer=2, fraction=2) -> Max 99.99, Min -99.99
        val param = getParameter("simpleDigits")
        val boundaries = generator.generateValidBoundaries(param)

        val maxBD = boundaries.maxOf { it as BigDecimal }
        val minBD = boundaries.minOf { it as BigDecimal }

        assertEquals(0, BigDecimal("99.99").compareTo(maxBD), "Max should be 99.99")
        assertEquals(0, BigDecimal("-99.99").compareTo(minBD), "Min should be -99.99")
    }

    @Test
    fun `calculateEffectiveRange - should distinguish Positive limit for Double vs Long`() {
        // Case A: Double @Positive -> Should be 0.00001 (Epsilon)
        val doubleParam = getParameter("tinyDouble")
        val doubleBoundaries = generator.generateValidBoundaries(doubleParam)
        val doubleMin = doubleBoundaries.minOf { it as Double }

        assertTrue(doubleMin > 0.0)
        assertTrue(doubleMin < 1.0, "Double Positive min should be small epsilon, not 1")

        // Case B: Long @Positive -> Should be 1 (Integer logic)
        val longParam = getParameter("positiveLong")
        val longBoundaries = generator.generateValidBoundaries(longParam)
        val longMin = longBoundaries.minOf { it as Long }

        assertEquals(1L, longMin, "Long Positive min should be 1")
    }

    @Test
    fun `calculateEffectiveRange - should handle Negative limit for Float`() {
        // @Negative Float -> Should be -0.00001
        val param = getParameter("negativeFloat")
        val boundaries = generator.generateValidBoundaries(param)
        val maxVal = boundaries.maxOf { it as Float }

        assertTrue(maxVal < 0.0f)
        assertTrue(maxVal > -1.0f, "Float Negative max should be small epsilon close to 0")
    }

    @Test
    fun `smartFuzz - should explicitly include 0 for crossing range`() {
        // Targets 'if (min <= 0.0 && max >= 0.0)' in smartFuzzDouble
        val param = getParameter("crossingDouble")
        val boundaries = generator.generateValidBoundaries(param)

        // Boundaries logic usually just checks min/max, but let's check generation distribution
        // to ensure 0.0 is a candidate.
        // Since we can't inspect private candidates, we rely on generateValidBoundaries logic or check random output.
        // However, 'generateValidBoundaries' for Double in your code adds min/max.
        // Let's verify 'generate' produces 0.0 occasionally or simply valid ranges.

        var zeroGenerated = false
        repeat(100) {
            val res = generator.generate(param) as Double
            if (res == 0.0) zeroGenerated = true
        }
        // Note: Random might not hit 0.0 exactly unless it's added as a candidate.
        // If smartFuzz adds 0.0 to candidates, probability is high (1/3 or 1/4).
        assertTrue(zeroGenerated, "Should generate 0.0 as it is explicitly added to candidates")
    }
}
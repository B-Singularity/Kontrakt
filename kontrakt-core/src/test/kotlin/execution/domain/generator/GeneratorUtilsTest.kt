package execution.domain.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class GeneratorUtilsTest {

    @Test
    fun `generateRandomString - valid range generates string within length`() {
        val min = 5
        val max = 15
        val result = GeneratorUtils.generateRandomString(min, max)

        assertTrue(result.length in min..max, "Length should be between min ($min) and max ($max)")
        assertTrue(result.all { it in GeneratorUtils.ALPHANUMERIC_POOL }, "Should only contain alphanumeric characters")
    }

    @Test
    fun `generateRandomString - fixed length when min equals max`() {
        val length = 8
        val result = GeneratorUtils.generateRandomString(length, length)

        assertEquals(length, result.length, "Length should be exactly $length")
    }

    @Test
    fun `generateRandomString - negative min is coerced to zero`() {
        // Input: min = -5, max = 5
        // Expected Logic: targetMin = 0, targetMax = 5 -> length in 0..5
        val result = GeneratorUtils.generateRandomString(-5, 5)

        assertTrue(result.length in 0..5, "Negative min should be treated as 0")
    }

    @Test
    fun `generateRandomString - max less than min is coerced to min`() {
        // Input: min = 10, max = 5
        // Expected Logic: targetMin = 10, targetMax = max(5, 10) = 10 -> length is exactly 10
        val min = 10
        val max = 5
        val result = GeneratorUtils.generateRandomString(min, max)

        assertEquals(min, result.length, "When max < min, max should be coerced to min (fixed length)")
    }

    @Test
    fun `generateRandomString - zero length generation`() {
        val result = GeneratorUtils.generateRandomString(0, 0)
        assertTrue(result.isEmpty(), "Should generate empty string for length 0")
    }

    @Test
    fun `generateRandomNumericString - generates specific length`() {
        val length = 10
        val result = GeneratorUtils.generateRandomNumericString(length)

        assertEquals(length, result.length, "Should generate string of requested length")
    }

    @Test
    fun `generateRandomNumericString - contains only digits`() {
        val result = GeneratorUtils.generateRandomNumericString(20)

        assertTrue(result.all { it.isDigit() }, "Should only contain numeric digits (0-9)")
    }

    @Test
    fun `generateRandomNumericString - zero or negative length returns empty`() {
        // Kotlin range (1..0) or (1..-1) is empty, so map returns empty list -> empty string
        val zeroResult = GeneratorUtils.generateRandomNumericString(0)
        assertTrue(zeroResult.isEmpty(), "Length 0 should return empty string")

        val negativeResult = GeneratorUtils.generateRandomNumericString(-5)
        assertTrue(negativeResult.isEmpty(), "Negative length should return empty string")
    }
}
package execution.domain.generator

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratorUtilsTest {
    private val testRandom = Random(42) // Fixed seed for reproducibility

    @Test
    fun verify_contract_generateRandomString_returns_valid_alphanumeric_string() {
        val min = 5
        val max = 10
        val iterations = 100

        repeat(iterations) {
            val result = GeneratorUtils.generateRandomString(min, max, testRandom)

            assertTrue(result.length in min..max, "Length should be between min and max")
            assertTrue(result.all { it.isLetterOrDigit() }, "String should only contain alphanumeric characters")
        }
    }

    @Test
    fun verify_parameter_and_branch_generateRandomString_handles_edge_cases() {
        // Case 1: Negative Min (Should be coerced to 0)
        val negativeMinResult = GeneratorUtils.generateRandomString(-5, 5, testRandom)
        assertTrue(negativeMinResult.length in 0..5, "Negative min should be coerced to 0")

        // Case 2: Max less than Min (Max should be coerced to Min)
        val swappedBoundsResult = GeneratorUtils.generateRandomString(10, 5, testRandom)
        assertEquals(10, swappedBoundsResult.length, "Max < Min should result in fixed length of Min")

        // Case 3: Min equals Max (Should skip random logic and return fixed length)
        val fixedLengthResult = GeneratorUtils.generateRandomString(8, 8, testRandom)
        assertEquals(8, fixedLengthResult.length, "Min == Max should return exact length")

        // Case 4: Zero Length
        val zeroLengthResult = GeneratorUtils.generateRandomString(0, 0, testRandom)
        assertTrue(zeroLengthResult.isEmpty(), "Min=0, Max=0 should return empty string")
    }

    @Test
    fun verify_contract_generateRandomNumericString_returns_only_digits() {
        val length = 20
        val result = GeneratorUtils.generateRandomNumericString(length, testRandom)

        assertEquals(length, result.length, "Should return exact requested length")
        assertTrue(result.all { it.isDigit() }, "Should contain only numeric characters")
    }

    @Test
    fun verify_contract_generateRandomStringFromCharRange_respects_range_and_length() {
        val range = 'a'..'f'
        val allowedChars = range.toList()
        val iterations = 50

        repeat(iterations) {
            val result = GeneratorUtils.generateRandomStringFromCharRange(range, testRandom)

            // Internal logic specifies hardcoded length between 5 and 11 (exclusive 11)
            assertTrue(result.length in 5..10, "Length should be determined by internal logic (5 to 10)")
            assertTrue(result.all { it in allowedChars }, "All characters must be from the provided range")
        }
    }

    @Test
    fun verify_branch_consistency_check_pool_integrity() {
        // Safety check to ensure the pool constant wasn't accidentally modified
        val pool = GeneratorUtils.ALPHANUMERIC_POOL
        assertEquals(62, pool.length, "Pool should contain A-Z, a-z, 0-9")
        assertTrue(pool.contains("A"))
        assertTrue(pool.contains("z"))
        assertTrue(pool.contains("9"))
    }
}

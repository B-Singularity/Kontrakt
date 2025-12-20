package execution.domain.generator

import kotlin.random.Random

object GeneratorUtils {
    const val ALPHANUMERIC_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    const val DEFAULT_STRING_MIN_LENGTH = 5
    const val DEFAULT_STRING_MAX_LENGTH = 15
    const val NOT_BLANK_MAX_LENGTH = 20
    const val DEFAULT_STRING_BUFFER = 20

    fun generateRandomString(min: Int, max: Int, random: Random): String {
        val targetMin = min.coerceAtLeast(0)
        val targetMax = max.coerceAtLeast(targetMin)
        val length = if (targetMin == targetMax) targetMin else random.nextInt(targetMin, targetMax + 1)
        return (1..length).map { ALPHANUMERIC_POOL.random() }.joinToString("")
    }

    fun generateRandomNumericString(length: Int, random: Random): String =
        (1..length).map { random.nextInt(0, 10) }.joinToString("")

    fun generateRandomStringFromCharRange(range: CharRange, random: Random): String {
        val chars = range.toList()
        val length = random.nextInt(5, 11)
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
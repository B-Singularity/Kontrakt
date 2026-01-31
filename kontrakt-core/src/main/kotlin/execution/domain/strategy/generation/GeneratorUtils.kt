package execution.domain.strategy.generation

import kotlin.math.min
import kotlin.random.Random

object GeneratorUtils {
    /**
     * Generates an Int within [min, max] INCLUSIVE.
     * Prevents Integer Overflow when max is Int.MAX_VALUE.
     */
    fun nextIntInclusive(random: Random, min: Int, max: Int): Int {
        if (min == max) return min
        return random.nextLong(min.toLong(), max.toLong() + 1L).toInt()
    }

    /**
     * Generates a Long within [min, max] INCLUSIVE with 100% Uniform Distribution.
     * Handles strict boundaries and overflow risks without bias.
     */
    fun nextLongInclusive(random: Random, min: Long, max: Long): Long {
        if (min == max) return min

        // Case 1: Standard Safe Range (max < Long.MAX_VALUE)
        // We can safely add 1 without overflow.
        if (max < Long.MAX_VALUE) {
            return random.nextLong(min, max + 1L)
        }

        // --- Handling max == Long.MAX_VALUE ---

        // Case 2: Full Range Optimization
        if (min == Long.MIN_VALUE) {
            return random.nextLong()
        }

        // Case 3: Positive Range (min > 0)
        // Range size = (MAX - min + 1).
        // Since min > 0, the size fits perfectly in a positive Long (size <= MAX).
        // Strategy: Positive Shift (Guaranteed termination).
        // Using rejection sampling here would risk infinite loops if min is close to MAX.
        if (min > 0) {
            val range = max - min + 1L
            return min + random.nextLong(range)
        }

        // Case 4: Massive Range crossing Zero (min <= 0)
        // Range size > 2^63.
        // Strategy: Rejection Sampling.
        // Acceptance probability P = (MAX - min + 1) / 2^64.
        // Since min <= 0, P >= 0.5. Very efficient (expected iterations <= 2).
        var r = random.nextLong()
        // Note: r > max is impossible because max is Long.MAX_VALUE. Only checking lower bound.
        while (r < min) {
            r = random.nextLong()
        }
        return r
    }

    fun clampLength(length: Int, physicalLimit: Int): Int {
        return min(length, physicalLimit)
    }
}
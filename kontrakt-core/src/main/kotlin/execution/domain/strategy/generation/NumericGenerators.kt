package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.nextDown
import kotlin.math.nextUp

/**
 * Generator for Integer values.
 */
class IntGenerator(
    private val min: Int = Int.MIN_VALUE,
    private val max: Int = Int.MAX_VALUE
) : Generator<Int> {

    init {
        require(min <= max) { "Invalid Int range: min ($min) > max ($max)." }
    }

    override fun generate(context: GenerationContext): Int {
        if (min == max) return min
        val random = context.seededRandom
        if (random.nextDouble() < 0.1) return generateEdgeCases(context).random(random)
        return GeneratorUtils.nextIntInclusive(random, min, max)
    }

    override fun generateEdgeCases(context: GenerationContext): List<Int> = buildList {
        add(min)
        if (min != max) add(max)

        // Neighbors (Off-by-one)
        if (min < Int.MAX_VALUE && min + 1 <= max) add(min + 1)
        if (max > Int.MIN_VALUE && max - 1 >= min) add(max - 1)

        // Interesting Values (if within range)
        if (0 in min..max) add(0)

        // Note: MIN/MAX_VALUE are implicitly covered if min/max equals them,
        // or by the logic above. Redundant explicit checks removed for cleaner logic.
    }.distinct()

    /**
     * @return List of invalid values.
     * Note: Returns Long to inject Type Overflow violations.
     */
    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        if (min > Int.MIN_VALUE) add(min - 1)
        if (max < Int.MAX_VALUE) add(max + 1)
        // Type Overflow (Long injection)
        add(max.toLong() + 1L)
    }
}

/**
 * Generator for Long values.
 * Fixed: 'max' coverage hole in random generation logic.
 */
class LongGenerator(
    private val min: Long = Long.MIN_VALUE,
    private val max: Long = Long.MAX_VALUE
) : Generator<Long> {

    init {
        require(min <= max) { "Invalid Long range: min ($min) > max ($max)." }
    }

    override fun generate(context: GenerationContext): Long {
        if (min == max) return min
        val random = context.seededRandom

        // 1. Smart Fuzzing (10%) - Guaranteed to hit 'max' even if random logic misses it
        if (random.nextDouble() < 0.1) return generateEdgeCases(context).random(random)

        // 2. Random Generation
        if (max == Long.MAX_VALUE) {
            if (min == Long.MIN_VALUE) {
                // Full Range: nextLong() is fully inclusive
                return random.nextLong()
            } else {
                // Range [min, Long.MAX_VALUE]
                // random.nextLong(min, max) is EXCLUSIVE of max.
                // To maintain PBT contract, we must allow max to be generated.
                // Approach: 0.1% chance to force max, otherwise approximation.
                return if (random.nextDouble() < 0.001) max else random.nextLong(min, max)
            }
        }

        // Standard Case: max < Long.MAX_VALUE (Safe to add 1)
        return random.nextLong(min, max + 1L)
    }

    override fun generateEdgeCases(context: GenerationContext): List<Long> = buildList {
        add(min)
        add(max)
        if (min < Long.MAX_VALUE) add(min + 1)
        if (max > Long.MIN_VALUE) add(max - 1)
        if (0L in min..max) add(0L)
    }.distinct()

    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        if (min > Long.MIN_VALUE) add(min - 1L)
        if (max < Long.MAX_VALUE) add(max + 1L)
    }
}

/**
 * Generator for Double values.
 * Fixed: Added -0.0 edge case.
 */
class DoubleGenerator(
    private val min: Double = -Double.MAX_VALUE,
    private val max: Double = Double.MAX_VALUE
) : Generator<Double> {

    init {
        require(min <= max) { "Invalid Double range: min ($min) > max ($max)." }
        require(!min.isNaN() && !max.isNaN()) { "Range cannot contain NaN." }
    }

    override fun generate(context: GenerationContext): Double {
        if (min == max) return min
        val random = context.seededRandom
        if (random.nextDouble() < 0.1) return generateEdgeCases(context).random(random)
        return random.nextDouble(min, max)
    }

    override fun generateEdgeCases(context: GenerationContext): List<Double> = buildList {
        add(min)
        add(max)

        // Near-Boundaries
        if (min < Double.MAX_VALUE) min.nextUp().takeIf { it <= max }?.let { add(it) }
        if (max > -Double.MAX_VALUE) max.nextDown().takeIf { it >= min }?.let { add(it) }

        // Zero handling with Negative Zero
        if (0.0 in min..max) {
            add(0.0)
            add(-0.0) // [Philosophy] Important for equality/hashing bugs
        }
    }.distinct()

    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        if (min > -Double.MAX_VALUE) add(min.nextDown())
        if (max < Double.MAX_VALUE) add(max.nextUp())
        add(Double.NaN)
        add(Double.POSITIVE_INFINITY)
    }
}

/**
 * Generator for BigDecimal values.
 * Note: MVP Implementation.
 */
class BigDecimalGenerator(
    private val min: BigDecimal,
    private val max: BigDecimal,
    private val scale: Int = 2
) : Generator<BigDecimal> {

    init {
        require(min <= max) { "Invalid BigDecimal range: min ($min) > max ($max)." }
    }

    override fun generate(context: GenerationContext): BigDecimal {
        if (min == max) return min
        val random = context.seededRandom
        if (random.nextDouble() < 0.1) return generateEdgeCases(context).random(random)

        // [MVP Limitation]
        // Converting random Double to BigDecimal introduces precision bias and non-uniform distribution.
        // For production PBT, this should be replaced with BigInteger-based generation.
        val factor = BigDecimal.valueOf(random.nextDouble())
        val range = max.subtract(min)
        return min.add(range.multiply(factor)).setScale(scale, RoundingMode.HALF_UP)
    }

    override fun generateEdgeCases(context: GenerationContext): List<BigDecimal> = buildList {
        val epsilon = if (scale >= 0) BigDecimal.ONE.movePointLeft(scale) else BigDecimal("0.00001")

        add(min)
        add(max)

        // Near-Boundaries
        if (min.add(epsilon) <= max) add(min.add(epsilon))
        if (max.subtract(epsilon) >= min) add(max.subtract(epsilon))

        if (min <= BigDecimal.ZERO && max >= BigDecimal.ZERO) {
            add(BigDecimal.ZERO.setScale(scale))
        }
    }.distinct()

    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        val epsilon = if (scale >= 0) BigDecimal.ONE.movePointLeft(scale) else BigDecimal("0.00001")

        // 1. Range Violation
        add(min.subtract(epsilon))
        add(max.add(epsilon))

        // 2. Structural Violation (Invalid Scale)
        // Tests if the consumer enforces strict scale constraints.
        add(min.setScale(scale + 3, RoundingMode.UNNECESSARY))
    }
}
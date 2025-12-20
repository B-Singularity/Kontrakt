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
import java.math.RoundingMode
import kotlin.random.Random
import kotlin.reflect.KClass

class NumericTypeGenerator : TerminalGenerator {

    override fun supports(request: GenerationRequest): Boolean {
        val type = request.type.classifier as? KClass<*> ?: return false
        return type == Int::class || type == Long::class || type == Double::class ||
                type == Float::class || type == BigDecimal::class
    }

    override fun generate(request: GenerationRequest, context: GenerationContext): Any? {
        val type = request.type.classifier as KClass<*>

        val (min, max) = calculateEffectiveRange(request, type)

        return when (type) {
            Int::class -> smartFuzzInt(min as Int, max as Int, context.seededRandom)
            Long::class -> smartFuzzLong(min as Long, max as Long, context.seededRandom)
            Double::class -> smartFuzzDouble(min as Double, max as Double, context.seededRandom)
            Float::class -> smartFuzzDouble(
                (min as Float).toDouble(),
                (max as Float).toDouble(),
                context.seededRandom
            ).toFloat()

            BigDecimal::class -> smartFuzzBigDecimal(
                min as BigDecimal,
                max as BigDecimal,
                request.find<Digits>(),
                context.seededRandom
            )

            else -> 0
        }
    }

    override fun generateValidBoundaries(request: GenerationRequest, context: GenerationContext): List<Any?> =
        buildList {
            val type = request.type.classifier as KClass<*>
            val (min, max) = calculateEffectiveRange(request, type)

            add(min)
            if (min != max) add(max)

            if (type == BigDecimal::class) {
                request.find<Digits>()?.let { digits ->
                    val maxPossible = BigDecimal.TEN.pow(digits.integer)
                        .subtract(BigDecimal.ONE.movePointLeft(digits.fraction))

                    val maxBD = max as BigDecimal
                    val minBD = min as BigDecimal

                    if (maxPossible <= maxBD && maxPossible >= minBD) {
                        add(maxPossible)
                    }
                    val negMax = maxPossible.negate()
                    if (negMax >= minBD && negMax <= maxBD) {
                        add(negMax)
                    }
                }
            }
        }

    override fun generateInvalid(request: GenerationRequest, context: GenerationContext): List<Any?> = buildList {
        val type = request.type.classifier as KClass<*>
        val (min, max) = calculateEffectiveRange(request, type)

        when (type) {
            Int::class -> {
                val minVal = min as Int
                val maxVal = max as Int
                if (minVal > Int.MIN_VALUE) add(minVal - 1)
                if (maxVal < Int.MAX_VALUE) add(maxVal + 1)
            }

            Long::class -> {
                val minVal = min as Long
                val maxVal = max as Long
                if (minVal > Long.MIN_VALUE) add(minVal - 1L)
                if (maxVal < Long.MAX_VALUE) add(maxVal + 1L)
            }

            Double::class -> {
                val minVal = min as Double
                val maxVal = max as Double
                if (minVal > -Double.MAX_VALUE) add(minVal - 0.1)
                if (maxVal < Double.MAX_VALUE) add(maxVal + 0.1)
            }

            BigDecimal::class -> {
                val minVal = min as BigDecimal
                val maxVal = max as BigDecimal
                add(minVal.subtract(BigDecimal("0.00001")))
                add(maxVal.add(BigDecimal("0.00001")))
            }
        }
    }

    // =================================================================
    // Internal Logic: Smart Fuzzing Implementations
    // =================================================================

    private fun smartFuzzInt(min: Int, max: Int, random: Random): Int {
        val candidates = mutableListOf(min, max)
        if (min < Int.MAX_VALUE && (min + 1) <= max) candidates.add(min + 1)
        if (max > Int.MIN_VALUE && (max - 1) >= min) candidates.add(max - 1)
        if (0 in min..max) candidates.add(0)

        // Random value within range
        val rand = if (min == max) min
        else if (max == Int.MAX_VALUE) Random.nextInt(min, Int.MAX_VALUE) // max exclusive
        else Random.nextInt(min, max + 1)

        candidates.add(rand)
        return candidates.random(random)
    }

    private fun smartFuzzLong(min: Long, max: Long, random: Random): Long {
        val candidates = mutableListOf(min, max)
        if (min < Long.MAX_VALUE && (min + 1) <= max) candidates.add(min + 1)
        if (max > Long.MIN_VALUE && (max - 1) >= min) candidates.add(max - 1)
        if (0L in min..max) candidates.add(0L)

        val rand = if (min == max) min
        else if (max == Long.MAX_VALUE) random.nextLong(min, Long.MAX_VALUE)
        else random.nextLong(min, max + 1)

        candidates.add(rand)
        return candidates.random(random)
    }

    private fun smartFuzzDouble(min: Double, max: Double, random: Random): Double {
        val candidates = mutableListOf(min, max)
        if (min <= 0.0 && max >= 0.0) candidates.add(0.0)

        val rand = if (min == max) min else random.nextDouble(min, max)
        candidates.add(rand)

        return candidates.random(random)
    }

    private fun smartFuzzBigDecimal(min: BigDecimal, max: BigDecimal, digits: Digits?, random: Random): BigDecimal {
        val candidates = mutableListOf(min, max)
        val scale = digits?.fraction ?: 2
        val epsilon = BigDecimal.ONE.movePointLeft(scale)

        // Min + epsilon
        val minPlus = min.add(epsilon)
        if (minPlus <= max) candidates.add(minPlus)

        // Max - epsilon
        val maxMinus = max.subtract(epsilon)
        if (maxMinus >= min) candidates.add(maxMinus)

        // 0
        if (min <= BigDecimal.ZERO && max >= BigDecimal.ZERO) {
            candidates.add(BigDecimal.ZERO.setScale(scale))
        }

        // Random
        if (min < max) {
            val randomFactor = BigDecimal.valueOf(random.nextDouble())
            val range = max.subtract(min)
            val randomVal = min.add(range.multiply(randomFactor)).setScale(scale, RoundingMode.HALF_UP)
            candidates.add(randomVal)
        }

        return candidates.random(random)
    }

    // =================================================================
    // Internal Logic: Effective Range Calculation
    // =================================================================

    private fun calculateEffectiveRange(request: GenerationRequest, type: KClass<*>): Pair<Any, Any> {
        var minLimit = BigDecimal.valueOf(-Double.MAX_VALUE)
        var maxLimit = BigDecimal.valueOf(Double.MAX_VALUE)

        when (type) {
            Int::class -> {
                minLimit = BigDecimal(Int.MIN_VALUE)
                maxLimit = BigDecimal(Int.MAX_VALUE)
            }

            Long::class -> {
                minLimit = BigDecimal(Long.MIN_VALUE)
                maxLimit = BigDecimal(Long.MAX_VALUE)
            }

            Double::class -> {
                minLimit = BigDecimal(-Double.MAX_VALUE)
                maxLimit = BigDecimal(Double.MAX_VALUE)
            }

            Float::class -> {
                minLimit = BigDecimal(-Float.MAX_VALUE.toDouble())
                maxLimit = BigDecimal(Float.MAX_VALUE.toDouble())
            }
        }

        // Range Annotations
        request.find<IntRange>()?.let {
            minLimit = minLimit.max(BigDecimal(it.min))
            maxLimit = maxLimit.min(BigDecimal(it.max))
        }
        request.find<LongRange>()?.let {
            minLimit = minLimit.max(BigDecimal(it.min))
            maxLimit = maxLimit.min(BigDecimal(it.max))
        }
        request.find<DoubleRange>()?.let {
            minLimit = minLimit.max(BigDecimal(it.min))
            maxLimit = maxLimit.min(BigDecimal(it.max))
        }

        // Decimal Annotations
        request.find<DecimalMin>()?.let {
            val value = BigDecimal(it.value)
            val effective = if (it.inclusive) value else value.add(BigDecimal("0.00001"))
            minLimit = minLimit.max(effective)
        }
        request.find<DecimalMax>()?.let {
            val value = BigDecimal(it.value)
            val effective = if (it.inclusive) value else value.subtract(BigDecimal("0.00001"))
            maxLimit = maxLimit.min(effective)
        }

        // Sign Annotations
        if (request.has<Positive>()) {
            val bound =
                if (type == Int::class || type == Long::class) BigDecimal.ONE else BigDecimal("0.00001")
            minLimit = minLimit.max(bound)
        }
        if (request.has<PositiveOrZero>()) {
            minLimit = minLimit.max(BigDecimal.ZERO)
        }
        if (request.has<Negative>()) {
            val bound =
                if (type == Int::class || type == Long::class) BigDecimal("-1") else BigDecimal("-0.00001")
            maxLimit = maxLimit.min(bound)
        }
        if (request.has<NegativeOrZero>()) {
            maxLimit = maxLimit.min(BigDecimal.ZERO)
        }

        // Digits Annotation (limits magnitude)
        request.find<Digits>()?.let {
            val maxVal = BigDecimal.TEN.pow(it.integer)
                .subtract(BigDecimal.ONE.movePointLeft(it.fraction))
            maxLimit = maxLimit.min(maxVal)
            minLimit = minLimit.max(maxVal.negate())
        }

        // Range Inversion Check
        if (minLimit > maxLimit) {
            return convertToPrimitivePair(minLimit, minLimit, type)
        }

        return convertToPrimitivePair(minLimit, maxLimit, type)
    }

    private fun convertToPrimitivePair(
        min: BigDecimal,
        max: BigDecimal,
        type: KClass<*>
    ): Pair<Any, Any> {
        return when (type) {
            Int::class -> min.toInt() to max.toInt()
            Long::class -> min.toLong() to max.toLong()
            Double::class -> min.toDouble() to max.toDouble()
            Float::class -> min.toFloat() to max.toFloat()
            else -> min to max
        }
    }

}
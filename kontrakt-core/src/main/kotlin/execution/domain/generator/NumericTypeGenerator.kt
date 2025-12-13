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
import kotlin.math.pow
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class NumericTypeGenerator : TypeGenerator {

    private val defaultDecimalLimit = BigDecimal("10000")
    private val defaultDecimalBuffer = BigDecimal("100")

    override fun supports(param: KParameter): Boolean {
        val type = param.type.classifier as? KClass<*> ?: return false
        return type == Int::class || type == Long::class || type == Double::class ||
                type == Float::class || type == BigDecimal::class
    }

    override fun generateValidBoundaries(param: KParameter): List<Any?> = buildList {
        val type = param.type.classifier as KClass<*>

        val (min, max) = calculateEffectiveRange(param, type)

        add(min)
        if (min != max) add(max)

        if (type == BigDecimal::class) {
            param.find<Digits>()?.let { digits ->
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

    override fun generate(param: KParameter): Any? {
        val type = param.type.classifier as KClass<*>

        val minDecimal = param.find<DecimalMin>()
        val maxDecimal = param.find<DecimalMax>()
        val digits = param.find<Digits>()

        if (minDecimal != null || maxDecimal != null || digits != null) {
            return generateDecimalSmartFuzz(minDecimal, maxDecimal, digits)
        }

        param.find<IntRange>()?.let { return it.smartFuzz() }
        param.find<LongRange>()?.let { return it.smartFuzz() }
        param.find<DoubleRange>()?.let { return it.smartFuzz() }
        param.find<Digits>()?.let { return it.generate() }

        if (param.has<Positive>()) return generatePositive(type, false)
        if (param.has<PositiveOrZero>()) return generatePositive(type, true)
        if (param.has<Negative>()) return generateNegative(type, false)
        if (param.has<NegativeOrZero>()) return generateNegative(type, true)

        return when (type) {
            Int::class -> Random.nextInt(1, 100)
            Long::class -> Random.nextLong(1, 1000)
            Double::class -> Random.nextDouble()
            Float::class -> Random.nextFloat()
            BigDecimal::class -> BigDecimal.valueOf(Random.nextDouble())
            else -> 0
        }
    }

    override fun generateInvalid(param: KParameter): List<Any?> = buildList {
        val type = param.type.classifier as KClass<*>

        param.find<IntRange>()?.let {
            if (it.min > Int.MIN_VALUE) add(it.min - 1)
            if (it.max < Int.MAX_VALUE) add(it.max + 1)
        }
        param.find<LongRange>()?.let {
            if (it.min > Long.MIN_VALUE) add(it.min - 1L)
            if (it.max < Long.MAX_VALUE) add(it.max + 1L)
        }
        param.find<DoubleRange>()?.let {
            if (it.min > -Double.MAX_VALUE) add(it.min - 0.1)
            if (it.max < Double.MAX_VALUE) add(it.max + 0.1)
        }

        if (param.has<Positive>()) add(getZeroOrNegative(type))
        if (param.has<Negative>()) add(getZeroOrPositive(type))

        param.find<DecimalMin>()?.let {
            val limit = BigDecimal(it.value)
            add(limit.subtract(BigDecimal("0.00001")))
        }
        param.find<DecimalMax>()?.let {
            val limit = BigDecimal(it.value)
            add(limit.add(BigDecimal("0.00001")))
        }
    }


    private fun calculateEffectiveRange(param: KParameter, type: KClass<*>): Pair<Any, Any> {
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
        }

        param.find<IntRange>()?.let {
            minLimit = minLimit.max(BigDecimal(it.min))
            maxLimit = maxLimit.min(BigDecimal(it.max))
        }
        param.find<LongRange>()?.let {
            minLimit = minLimit.max(BigDecimal(it.min))
            maxLimit = maxLimit.min(BigDecimal(it.max))
        }
        param.find<DoubleRange>()?.let {
            minLimit = minLimit.max(BigDecimal(it.min))
            maxLimit = maxLimit.min(BigDecimal(it.max))
        }

        param.find<DecimalMin>()?.let {
            val value = BigDecimal(it.value)
            val effective = if (it.inclusive) value else value.add(BigDecimal("0.00001"))
            minLimit = minLimit.max(effective)
        }
        param.find<DecimalMax>()?.let {
            val value = BigDecimal(it.value)
            val effective = if (it.inclusive) value else value.subtract(BigDecimal("0.00001"))
            maxLimit = maxLimit.min(effective)
        }

        if (param.has<Positive>()) {
            val bound = if (type == Int::class || type == Long::class) BigDecimal.ONE else BigDecimal("0.00001")
            minLimit = minLimit.max(bound)
        }
        if (param.has<PositiveOrZero>()) {
            minLimit = minLimit.max(BigDecimal.ZERO)
        }
        if (param.has<Negative>()) {
            val bound = if (type == Int::class || type == Long::class) BigDecimal("-1") else BigDecimal("-0.00001")
            maxLimit = maxLimit.min(bound)
        }
        if (param.has<NegativeOrZero>()) {
            maxLimit = maxLimit.min(BigDecimal.ZERO)
        }

        return convertToPrimitivePair(minLimit, maxLimit, type)
    }

    private fun convertToPrimitivePair(min: BigDecimal, max: BigDecimal, type: KClass<*>): Pair<Any, Any> {
        return when (type) {
            Int::class -> min.toInt() to max.toInt()
            Long::class -> min.toLong() to max.toLong()
            Double::class -> min.toDouble() to max.toDouble()
            Float::class -> min.toFloat() to max.toFloat()
            else -> min to max
        }
    }


    private fun IntRange.smartFuzz(): Int {
        val candidates = mutableListOf(min, max)
        if (min < Int.MAX_VALUE) candidates.add(min + 1)
        if (max > Int.MIN_VALUE) candidates.add(max - 1)
        if (0 in min..max) candidates.add(0)
        val rand = if (min == max) min else Random.nextInt(min, max + 1)
        candidates.add(rand)
        return candidates.random()
    }

    private fun LongRange.smartFuzz(): Long {
        val candidates = mutableListOf(min, max)
        if (min < Long.MAX_VALUE) candidates.add(min + 1)
        if (max > Long.MIN_VALUE) candidates.add(max - 1)
        if (0L in min..max) candidates.add(0L)
        val rand = if (min == max) min else Random.nextLong(min, max + 1)
        candidates.add(rand)
        return candidates.random()
    }

    private fun DoubleRange.smartFuzz(): Double = when (Random.nextInt(3)) {
        0 -> min
        1 -> max
        else -> Random.nextDouble(min, max)
    }

    private fun Digits.generate(): BigDecimal =
        BigDecimal.valueOf(Random.nextDouble(0.0, 10.0.pow(integer))).setScale(fraction, RoundingMode.HALF_UP)

    private fun generatePositive(type: KClass<*>, includeZero: Boolean): Any = when (type) {
        Int::class -> Random.nextInt(if (includeZero) 0 else 1, Int.MAX_VALUE)
        Long::class -> Random.nextLong(if (includeZero) 0L else 1L, Long.MAX_VALUE)
        Double::class -> Random.nextDouble(if (includeZero) 0.0 else Double.MIN_VALUE, Double.MAX_VALUE)
        BigDecimal::class -> BigDecimal.valueOf(Random.nextDouble(if (includeZero) 0.0 else 0.00001, Double.MAX_VALUE))
        else -> 1
    }

    private fun generateNegative(type: KClass<*>, includeZero: Boolean): Any = when (type) {
        Int::class -> Random.nextInt(Int.MIN_VALUE, if (includeZero) 1 else 0)
        Long::class -> Random.nextLong(Long.MIN_VALUE, if (includeZero) 1L else 0L)
        Double::class -> Random.nextDouble(-Double.MAX_VALUE, if (includeZero) 0.00001 else -Double.MIN_VALUE)
        BigDecimal::class -> BigDecimal.valueOf(
            Random.nextDouble(
                -Double.MAX_VALUE,
                if (includeZero) 0.0 else -0.00001
            )
        )

        else -> -1
    }

    private fun getZeroOrNegative(type: KClass<*>): Any = when (type) {
        Int::class -> 0
        Long::class -> 0L
        Double::class -> 0.0
        BigDecimal::class -> BigDecimal.ZERO
        else -> 0
    }

    private fun getZeroOrPositive(type: KClass<*>): Any = when (type) {
        Int::class -> 0
        Long::class -> 0L
        Double::class -> 0.0
        BigDecimal::class -> BigDecimal.ZERO
        else -> 0
    }

    private fun generateDecimalSmartFuzz(min: DecimalMin?, max: DecimalMax?, digits: Digits?): BigDecimal {
        val minVal = if (min != null) BigDecimal(min.value) else BigDecimal("-10000")
        val maxVal = if (max != null) BigDecimal(max.value) else
            if (min != null) minVal.add(defaultDecimalBuffer) else defaultDecimalLimit

        val scale = digits?.fraction ?: 2
        val epsilon = BigDecimal.ONE.movePointLeft(scale)
        val candidates = mutableListOf<BigDecimal>()

        val effectiveMin = if (min?.inclusive == false) minVal.add(epsilon) else minVal
        candidates.add(effectiveMin)
        val effectiveMax = if (max?.inclusive == false) maxVal.subtract(epsilon) else maxVal
        candidates.add(effectiveMax)

        if (BigDecimal.ZERO >= effectiveMin && BigDecimal.ZERO <= effectiveMax) candidates.add(
            BigDecimal.ZERO.setScale(
                scale
            )
        )

        if (effectiveMin > effectiveMax) return effectiveMin

        val randomFactor = BigDecimal.valueOf(Random.nextDouble())
        val range = effectiveMax.subtract(effectiveMin)
        val randomVal = effectiveMin.add(range.multiply(randomFactor)).setScale(scale, RoundingMode.HALF_UP)
        candidates.add(randomVal)

        return candidates.random()
    }

    private inline fun <reified T : Annotation> KParameter.has(): Boolean = findAnnotation<T>() != null
    private inline fun <reified T : Annotation> KParameter.find(): T? = findAnnotation<T>()
}
package execution.domain.service

import discovery.api.AssertFalse
import discovery.api.AssertTrue
import discovery.api.DecimalMax
import discovery.api.DecimalMin
import discovery.api.Digits
import discovery.api.DoubleRange
import discovery.api.Email
import discovery.api.Future
import discovery.api.FutureOrPresent
import discovery.api.IntRange
import discovery.api.LongRange
import discovery.api.Negative
import discovery.api.NegativeOrZero
import discovery.api.NotBlank
import discovery.api.Past
import discovery.api.PastOrPresent
import discovery.api.Pattern
import discovery.api.Positive
import discovery.api.PositiveOrZero
import discovery.api.StringLength
import discovery.api.Url
import discovery.api.Uuid
import execution.spi.MockingEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class FixtureGenerator(
    private val mockingEngine: MockingEngine,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DEFAULT_STRING_MIN_LENGTH = 5
        private const val DEFAULT_STRING_MAX_LENGTH = 15
        private const val DEFAULT_STRING_BUFFER = 20
        private const val NOT_BLANK_MAX_LENGTH = 20
        private const val DEFAULT_DECIMAL_LIMIT = "10000"
        private const val DEFAULT_DECIMAL_BUFFER = "100"
        private const val EFFECTIVEACCOUNTMAX = 20

        private const val DEFAULT_DATE_RANGE_DAYS = 3650L
        private const val ALPHANUMERIC_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }

    fun generate(
        param: KParameter,
        history: Set<KClass<*>> = emptySet(),
    ): Any? {
        val type = param.type.classifier as? KClass<*> ?: return null

        return generateBooleanConstraint(param)
            ?: generateSpecialFormat(param)
            ?: generateTime(param, type)
            ?: generateNumericConstraint(param, type)
            ?: generateStringConstraint(param)
            ?: generateByType(param.type, history)
    }

    fun generateByType(
        type: KType,
        history: Set<KClass<*>> = emptySet(),
    ): Any? {
        val kClass = type.classifier as? KClass<*> ?: return null

        generatePrimitiveDefaults(kClass)?.let { return it }

        if (kClass == List::class || kClass == Collection::class || kClass == Iterable::class) {
            val elementType = type.arguments.firstOrNull()?.type ?: return emptyList<Any>()
            return List(1) { generateByType(elementType, history) }
        }
        if (kClass == Set::class) return emptySet<Any>()
        if (kClass == Map::class) return emptyMap<Any, Any>()

        if (kClass in history) {
            logger.debug { "Circular dependency detected for '${kClass.simpleName}'. Breaking cycle with Mock." }
            return tryCreateMock(kClass)
        }

        val constructor = kClass.primaryConstructor
        if (constructor != null) {
            try {
                val newHistory = history + kClass

                val args =
                    constructor.parameters
                        .map { param ->
                            generate(param, newHistory)
                        }.toTypedArray()
                return constructor.call(*args)
            } catch (e: Exception) {
                logger.debug(e) { "Constructor failed for '${kClass.simpleName}'. Fallback to Mock." }
            }
        }
        return tryCreateMock(kClass)
    }

    private fun tryCreateMock(type: KClass<*>): Any? =
        try {
            mockingEngine.createMock(type)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to create Mock for '${type.simpleName}'. Returning null." }
            null
        }

    fun generateInvalid(param: KParameter): List<Any?> =
        buildList<Any?> {
            val type = param.type.classifier as? KClass<*>

            if (!param.type.isMarkedNullable) add(null)

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

            if (param.has<Positive>()) {
                if (type == Int::class) {
                    add(0)
                } else if (type == Long::class) {
                    add(0L)
                } else if (type == Double::class) {
                    add(0.0)
                } else if (type == BigDecimal::class) {
                    add(BigDecimal.ZERO)
                }
            }

            if (param.has<Negative>()) {
                if (type == Int::class) {
                    add(0)
                } else if (type == Long::class) {
                    add(0L)
                } else if (type == Double::class) {
                    add(0.0)
                } else if (type == BigDecimal::class) {
                    add(BigDecimal.ZERO)
                }
            }

            param.find<StringLength>()?.let {
                if (it.min > 0) add("x".repeat(it.min - 1))
            }

            if (param.has<NotBlank>()) {
                add("")
                add("   ")
            }

            if (param.has<Email>()) {
                add("not-an-email")
                add("@domain.com")
            }
        }

    private fun generateBooleanConstraint(param: KParameter): Boolean? =
        when {
            param.has<AssertTrue>() -> true
            param.has<AssertFalse>() -> false
            else -> null
        }

    private fun generateSpecialFormat(param: KParameter): Any? {
        val length = param.find<StringLength>()
        val maxLen = length?.max ?: Int.MAX_VALUE

        val urlAnno = param.find<Url>()

        return when {
            param.has<Email>() -> generateComplexEmail(maxLen, param.find<Email>()!!)
            param.has<Uuid>() -> UUID.randomUUID().toString()
            urlAnno != null -> generateComplexUrl(maxLen, urlAnno)
            param.has<Pattern>() -> generateFromRegex(param.find<Pattern>()!!.regexp)
            else -> null
        }
    }

    private fun generateFromRegex(regex: String): String =
        when {
            regex == "\\d+" || regex == "[0-9]+" -> generateRandomNumericString(5)
            regex == "\\w+" || regex == "[a-zA-Z]+" -> generateRandomString(5, 10)
            regex == "^[A-Z]+$" -> generateRandomString(5, 10).uppercase()
            regex == "^[a-z]+$" -> generateRandomString(5, 10).lowercase()
            else -> "Pattern_Placeholder_for_$regex"
        }

    private fun generateComplexEmail(
        limit: Int,
        rule: Email,
    ): String {
        val availableDomains =
            if (rule.allow.isNotEmpty()) {
                rule.allow.toList()
            } else {
                val defaults = listOf("com", "net", "org", "io", "co.kr", "gov")
                defaults.filter { it !in rule.block }
            }

        val targetDomains = if (availableDomains.isEmpty()) listOf("com") else availableDomains
        val suffix = targetDomains.random()

        val domainPart = if (suffix.contains(".")) suffix else "${generateRandomString(3, 5)}.$suffix"

        val overhead = 1 + domainPart.length // '@' + domainPart
        val maxAccountLen = limit - overhead

        if (maxAccountLen < 1) return "a@$domainPart"

        val effectiveMax = maxAccountLen.coerceAtMost(20)
        val accountLen = if (effectiveMax <= 1) 1 else Random.Default.nextInt(1, effectiveMax + 1)

        val account = generateRandomString(accountLen, accountLen)

        return "$account@$domainPart"
    }

    private fun generateComplexUrl(
        limit: Int,
        rule: Url,
    ): String {
        val scheme = rule.protocol.random() + "://"

        val host =
            if (rule.hostAllow.isNotEmpty()) {
                rule.hostAllow.random()
            } else {
                val generated = generateHost()
                if (rule.hostBlock.any { generated.contains(it) }) "example.com" else generated
            }

        val overhead = scheme.length + host.length
        val remaining = limit - overhead

        if (remaining < 1) return "$scheme$host"

        val pathLen = (remaining / 2).coerceAtLeast(1)
        val path = "/" + generateRandomString(pathLen.coerceAtMost(10), pathLen.coerceAtMost(10))

        return "$scheme$host$path"
    }

    private fun generateHost(): String {
        val subDomains = listOf("www", "api", "m", "blog", "admin", "")
        val domainSuffixes = listOf("com", "net", "org", "io", "kr")

        val sub = subDomains.random().let { if (it.isBlank()) "" else "$it." }
        val domain = generateRandomString(3, 10).lowercase()
        val suffix = domainSuffixes.random()

        return "$sub$domain.$suffix"
    }

    private fun generatePath(): String {
        val depth = Random.Default.nextInt(4)
        if (depth == 0) return ""
        return (1..depth).joinToString(prefix = "/", separator = "/") {
            generateRandomString(3, 8).lowercase()
        }
    }

    private fun generateQueryParam(): String {
        if (Random.Default.nextBoolean()) return ""
        val count = Random.Default.nextInt(1, 4)
        val params =
            (1..count).joinToString("&") {
                "${generateRandomString(2, 5)}=${generateRandomString(3, 6)}"
            }
        return "?$params"
    }

    private fun generateTime(
        param: KParameter,
        type: KClass<*>,
    ): Any? {
        val now = clock.instant()
        val offsetDays = Random.Default.nextLong(1, DEFAULT_DATE_RANGE_DAYS)

        val targetInstant =
            when {
                param.has<Future>() || param.has<FutureOrPresent>() -> now.plus(offsetDays, ChronoUnit.DAYS)
                param.has<Past>() || param.has<PastOrPresent>() -> now.minus(offsetDays, ChronoUnit.DAYS)
                else -> return null
            }

        return convertToTimeType(targetInstant, type)
    }

    private fun convertToTimeType(
        instant: Instant,
        type: KClass<*>,
    ): Any? =
        when (type) {
            Instant::class -> instant
            String::class -> instant.toString()
            Date::class -> Date.from(instant)
            LocalDate::class -> LocalDate.ofInstant(instant, clock.zone)
            LocalDateTime::class -> LocalDateTime.ofInstant(instant, clock.zone)
            ZonedDateTime::class -> ZonedDateTime.ofInstant(instant, clock.zone)
            else -> null
        }

    private fun generateNumericConstraint(
        param: KParameter,
        type: KClass<*>,
    ): Any? {
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

        if (param.has<Positive>()) return generatePositive(type, includeZero = false)
        if (param.has<PositiveOrZero>()) return generatePositive(type, includeZero = true)
        if (param.has<Negative>()) return generateNegative(type, includeZero = false)
        if (param.has<NegativeOrZero>()) return generateNegative(type, includeZero = true)

        return null
    }

    private fun IntRange.smartFuzz(): Int {
        val base = mutableListOf(min, max)
        if (min < Int.MAX_VALUE) base.add(min + 1)
        if (max > Int.MIN_VALUE) base.add(max - 1)
        if (0 in min..max) base.add(0)

        val randomVal = if (min == max) min else Random.Default.nextInt(min, max + 1)
        base.add(randomVal)
        return base.random()
    }

    private fun LongRange.smartFuzz(): Long {
        val base = mutableListOf(min, max)
        if (min < Long.MAX_VALUE) base.add(min + 1)
        if (max > Long.MIN_VALUE) base.add(max - 1)
        if (0L in min..max) base.add(0L)

        val randomVal = if (min == max) min else Random.Default.nextLong(min, max + 1)
        base.add(randomVal)
        return base.random()
    }

    private fun DoubleRange.smartFuzz(): Double =
        when (Random.Default.nextInt(3)) {
            0 -> min
            1 -> max
            else -> Random.Default.nextDouble(min, max)
        }

    private fun Digits.generate(): BigDecimal =
        BigDecimal.valueOf(Random.Default.nextDouble(0.0, 10.0.pow(integer))).setScale(fraction, RoundingMode.HALF_UP)

    private fun generatePositive(
        type: KClass<*>,
        includeZero: Boolean,
    ): Any =
        when (type) {
            Int::class -> Random.Default.nextInt(if (includeZero) 0 else 1, Int.MAX_VALUE)
            Long::class -> Random.Default.nextLong(if (includeZero) 0L else 1L, Long.MAX_VALUE)
            Double::class -> {
                val origin = if (includeZero) 0.0 else Double.MIN_VALUE
                Random.Default.nextDouble(origin, Double.MAX_VALUE)
            }

            BigDecimal::class -> {
                val origin = if (includeZero) 0.0 else Double.MIN_VALUE
                BigDecimal.valueOf(Random.Default.nextDouble(origin, Double.MAX_VALUE))
            }

            else -> 1
        }

    private fun generateNegative(
        type: KClass<*>,
        includeZero: Boolean,
    ): Any =
        when (type) {
            Int::class -> Random.Default.nextInt(Int.MIN_VALUE, if (includeZero) 1 else 0)
            Long::class -> Random.Default.nextLong(Long.MIN_VALUE, if (includeZero) 1L else 0L)
            Double::class -> {
                val bound = if (includeZero) 0.00001 else -Double.MIN_VALUE
                Random.Default.nextDouble(-Double.MAX_VALUE, bound)
            }

            BigDecimal::class -> BigDecimal.valueOf(Random.Default.nextDouble(-Double.MAX_VALUE, -0.00001))
            else -> -1
        }

    private fun generateStringConstraint(param: KParameter): String? {
        val length = param.find<StringLength>()
        if (length != null) {
            val effectiveMax = if (length.max == Int.MAX_VALUE) length.min + DEFAULT_STRING_BUFFER else length.max
            return generateRandomString(length.min, effectiveMax)
        }

        if (param.has<NotBlank>()) {
            return generateRandomString(1, NOT_BLANK_MAX_LENGTH)
        }
        return null
    }

    private fun generateRandomString(
        min: Int,
        max: Int,
    ): String {
        val targetMin = min.coerceAtLeast(0)
        val targetMax = max.coerceAtLeast(targetMin)

        val length = if (targetMin == targetMax) targetMin else Random.Default.nextInt(targetMin, targetMax + 1)

        return (1..length)
            .map { ALPHANUMERIC_POOL.random() }
            .joinToString("")
    }

    private fun generateRandomNumericString(length: Int): String =
        (1..length).map { Random.Default.nextInt(0, 10) }.joinToString("")

    private fun generatePrimitiveDefaults(type: KClass<*>): Any? =
        when (type) {
            String::class -> generateRandomString(DEFAULT_STRING_MIN_LENGTH, DEFAULT_STRING_MAX_LENGTH)
            Int::class -> Random.Default.nextInt(1, 100)
            Long::class -> Random.Default.nextLong(1, 1000)
            Boolean::class -> Random.Default.nextBoolean()
            Double::class -> Random.Default.nextDouble()
            Float::class -> Random.Default.nextFloat()
            BigDecimal::class -> BigDecimal.valueOf(Random.Default.nextDouble())
            else -> null
        }

    private fun generateDecimalSmartFuzz(
        min: DecimalMin?,
        max: DecimalMax?,
        digits: Digits?,
    ): BigDecimal {
        val minVal = if (min != null) BigDecimal(min.value) else BigDecimal("-$DEFAULT_DECIMAL_LIMIT")
        val maxVal =
            if (max != null) {
                BigDecimal(max.value)
            } else {
                if (min != null) minVal.add(BigDecimal(DEFAULT_DECIMAL_BUFFER)) else BigDecimal(DEFAULT_DECIMAL_LIMIT)
            }

        val scale = digits?.fraction ?: 2
        val epsilon = BigDecimal.ONE.movePointLeft(scale)

        val candidates = mutableListOf<BigDecimal>()

        val effectiveMin = if (min?.inclusive == false) minVal.add(epsilon) else minVal
        candidates.add(effectiveMin)

        val effectiveMax = if (max?.inclusive == false) maxVal.subtract(epsilon) else maxVal
        candidates.add(effectiveMax)

        if (BigDecimal.ZERO >= effectiveMin && BigDecimal.ZERO <= effectiveMax) {
            candidates.add(BigDecimal.ZERO.setScale(scale))
        }

        if (effectiveMin > effectiveMax) return effectiveMin

        val randomFactor = BigDecimal.valueOf(Random.Default.nextDouble())
        val range = effectiveMax.subtract(effectiveMin)
        val randomVal = effectiveMin.add(range.multiply(randomFactor)).setScale(scale, RoundingMode.HALF_UP)
        candidates.add(randomVal)

        return candidates.random()
    }

    private inline fun <reified T : Annotation> KParameter.has(): Boolean = findAnnotation<T>() != null

    private inline fun <reified T : Annotation> KParameter.find(): T? = findAnnotation<T>()
}

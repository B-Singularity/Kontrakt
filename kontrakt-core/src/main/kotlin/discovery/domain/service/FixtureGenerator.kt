package discovery.domain.service

import discovery.api.*
import discovery.api.IntRange
import discovery.api.LongRange
import discovery.api.DoubleRange
import discovery.api.StringLength
import execution.spi.MockingEngine
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import kotlin.math.pow
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.random.Random
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation


class FixtureGenerator(
    private val mockingEngine: MockingEngine
) {
    companion object {
        private const val SAFE_STRING_LIMIT = 1000
        private const val DEFAULT_STRING_MIN_LENGTH = 5
        private const val DEFAULT_STRING_MAX_LENGTH = 15
        private const val NOT_BLANK_MAX_LENGTH = 20

        private const val DEFAULT_DATE_RANGE_DAYS = 3650L

        private const val ALPHANUMERIC_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }

    fun generate(param: KParameter): Any? {
        val type = param.type.classifier as KClass<*> ?: return null

        return generateSpecialFormat(param)
            ?: generateTime(param, type)
            ?: generateNumericConstraint(param, type)
            ?: generateStringConstraint(param)
            ?: generatePrimitiveDefaults(type)
            ?: mockingEngine.createMock(type)
    }

    private fun generateSpecialFormat(param: KParameter): Any? = when {
        param.hasAnnotation<Email>() -> generateComplexEmail()
        param.hasAnnotation<Uuid>() -> UUID.randomUUID().toString()
        param.hasAnnotation<Url>() -> generateComplexUrl()
        param.hasAnnotation<Pattern>() -> generateFromRegex(param.findAnnotation<Pattern>()!!.regexp)
        else -> null
    }

    private fun generateFromRegex(regex: String): String {
        return when {
            regex == "\\d+" || regex == "[0-9]+" -> generateRandomNumericString(5)
            regex == "\\w+" || regex == "[a-zA-Z]+" -> generateRandomString(5)
            regex == "^[A-Z]+$" -> generateRandomString(5).uppercase()
            regex == "^[a-z]+$" -> generateRandomString(5).lowercase()
            else -> "Pattern_Placeholder_for_$regex"
        }
    }

    private fun generateComplexEmail(): String {
        val domainSuffixes = listOf("com", "net", "org", "io", "co.kr", "gov")
        val separators = listOf(".", "_", "-", "")

        val account = buildString {
            append(generateRandomString(3, 6))
            append(separators.random())
            append(generateRandomString(3, 6))
        }

        val domain = if (Random.nextBoolean()) {
            "${generateRandomString(3, 5)}.${generateRandomString(3, 5)}"
        } else {
            generateRandomString(4, 8)
        }

        return "$account@$domain.${domainSuffixes.random()}"
    }

    private fun generateComplexUrl(): String {
        val scheme = listOf("http", "https").random()
        val host = generateHost()
        val path = generatePath()
        val query = generateQueryParam()

        return "$scheme://$host$path$query"
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
        val depth = Random.nextInt(4)
        if (depth == 0) return ""
        return (1..depth).joinToString(prefix = "/", separator = "/") {
            generateRandomString(3, 8).lowercase()
        }
    }

    private fun generateQueryParam(): String {
        if (Random.nextBoolean()) return ""
        val count = Random.nextInt(1, 4)
        val params = (1..count).joinToString("&") {
            "${generateRandomString(2, 5)}=${generateRandomString(3, 6)}"
        }
        return "?$params"
    }

    private fun generateTime(param: KParameter, type: KClass<*>): Any? {
        val now = Instant.now()
        val offsetDays = Random.nextLong(1, DEFAULT_DATE_RANGE_DAYS)

        val targetInstant = when {
            param.hasAnnotation<Future>() -> now.plus(offsetDays, ChronoUnit.DAYS)
            param.hasAnnotation<Past>() -> now.minus(offsetDays, ChronoUnit.DAYS)
            else -> return null
        }

        return convertToTimeType(targetInstant, type)
    }

    private fun convertToTimeType(instant: Instant, type: KClass<*>): Any? = when (type) {
        Instant::class -> instant
        String::class -> instant.toString()
        Date::class -> Date.from(instant)
        LocalDate::class -> Date.from(instant)
        LocalDateTime::class -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        ZonedDateTime::class -> ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        else -> null
    }

    private fun generateNumericConstraint(param: KParameter, type: KClass<*>): Any? {
        param.findAnnotation<IntRange>()?.let { return it.smartFuzz() }
        param.findAnnotation<LongRange>()?.let { return it.smartFuzz() }
        param.findAnnotation<DoubleRange>()?.let { return it.smartFuzz() }
        param.findAnnotation<Digits>()?.let { return it.generate() }

        if (param.hasAnnotation<Positive>()) return generatePositive(type)
        if (param.hasAnnotation<PositiveOrZero>()) return generatePositive(type, includeZero = true)
        if (param.hasAnnotation<Negative>()) return generateNegative(type)

        return null
    }

    private fun IntRange.smartFuzz(): Int = listOf(
        min, max,
        if (min < Int.MAX_VALUE) min + 1 else min,
        if (max > Int.MIN_VALUE) max - 1 else max,
        Random.nextInt(min, max)
    ).random()

    private fun LongRange.smartFuzz(): Long = listOf(
        min, max,
        if (min < Long.MAX_VALUE) min + 1 else min,
        if (max > Long.MIN_VALUE) max - 1 else max,
        Random.nextLong(min, max)
    ).random()

    private fun DoubleRange.smartFuzz(): Double = when(Random.nextInt(5)) {
        0 -> min
        1 -> max
        else -> Random.nextDouble(min, max)
    }

    private fun Digits.generate(): BigDecimal {
        val maxInteger = 10.0.pow(integer.toDouble())
        val randomVal = Random.nextDouble(0.0, maxInteger)
        return BigDecimal.valueOf(randomVal).setScale(fraction, RoundingMode.HALF_UP)
    }

    private fun generatePositive(type: KClass<*>, includeZero: Boolean = false): Any = when (type) {
        Int::class -> if (includeZero) Random.nextInt(0, Int.MAX_VALUE) else Random.nextInt(1, Int.MAX_VALUE)
        Long::class -> if (includeZero) Random.nextLong(0, Long.MAX_VALUE) else Random.nextLong(1, Long.MAX_VALUE)
        Double::class -> if (includeZero) Random.nextDouble(0.0, Double.MAX_VALUE) else Random.nextDouble(0.1, Double.MAX_VALUE)
        BigDecimal::class -> BigDecimal.valueOf(Random.nextDouble(0.1, Double.MAX_VALUE))
        else -> 1
    }

    private fun generateNegative(type: KClass<*>): Any = when (type) {
        Int::class -> Random.nextInt(Int.MIN_VALUE, 0)
        Long::class -> Random.nextLong(Long.MIN_VALUE, 0)
        Double::class -> Random.nextDouble(-Double.MAX_VALUE, -0.0001)
        BigDecimal::class -> BigDecimal.valueOf(Random.nextDouble(-Double.MAX_VALUE, -0.0001))
        else -> -1
    }

    private fun generateStringConstraint(param: KParameter): String? {
        val length = param.findAnnotation<StringLength>()
        if (length != null) {
            return generateRandomString(length.min, length.max)
        }

        if (param.hasAnnotation<NotBlank>()) {
            return generateRandomString(1, NOT_BLANK_MAX_LENGTH)
        }

        return null
    }

    private fun generateRandomString(min: Int, max: Int = min + 10): String {
        val safeMin = min.coerceAtLeast(0)
        val safeMax = max.coerceAtMost(SAFE_STRING_LIMIT).coerceAtLeast(safeMin)
        val length = if (safeMin == safeMax) safeMin else Random.nextInt(safeMin, safeMax + 1)

        return (1..length)
            .map{ ALPHANUMERIC_POOL.random() }
            .joinToString("")
    }

    private fun generatePrimitiveDefaults(type: KClass<*>): Any? = when (type) {
        String::class -> generateRandomString(DEFAULT_STRING_MIN_LENGTH, DEFAULT_STRING_MAX_LENGTH)
        Int::class -> Random.nextInt(1, 100)
        Long::class -> Random.nextLong(1, 1000)
        Boolean::class -> Random.nextBoolean()
        Double::class -> Random.nextDouble()
        Float::class -> Random.nextFloat()
        BigDecimal::class -> BigDecimal.valueOf(Random.nextDouble())
        else -> null
    }
    private fun generateRandomNumericString(length: Int): String {
        return (1..length).map { Random.nextInt(0, 10) }.joinToString("")
    }

    private inline fun <reified T : Annotation> KParameter.hasAnnotation(): Boolean =
        findAnnotation<T>() != null
}
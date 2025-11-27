package execution.domain.service

import discovery.api.AssertFalse
import discovery.api.AssertTrue
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
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

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

        return generateBooleanConstraint(param)
            ?: generateSpecialFormat(param)
            ?: generateTime(param, type)
            ?: generateNumericConstraint(param, type)
            ?: generateStringConstraint(param)
            ?: generateByType(type)
    }

    fun generateByType(type: KClass<*>): Any? {
        generatePrimitiveDefaults(type)?.let { return it }

        if (type == List::class || type == Collection::class || type == Iterable::class) return emptyList<Any>()
        if (type == Set::class) return emptySet<Any>()
        if (type == Map::class) return emptyMap<Any, Any>()

        val constructor = type.primaryConstructor
        if (constructor != null) {
            try {
                val args = constructor.parameters.map { param ->
                    generate(param)
                }.toTypedArray()
                return constructor.call(*args)
            } catch (e: Exception) {}
        }

        return try {
            mockingEngine.createMock(type)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateBooleanConstraint(param: KParameter): Boolean? = when {
        param.has<AssertTrue>() -> true
        param.has<AssertFalse>() -> false
        else -> null
    }


    private fun generateSpecialFormat(param: KParameter): Any? = when {
        param.has<Email>() -> generateComplexEmail()
        param.has<Uuid>() -> UUID.randomUUID().toString()
        param.has<Url>() -> generateComplexUrl()
        param.has<Pattern>() -> generateFromRegex(param.findAnnotation<Pattern>()!!.regexp)
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

        val domain = if (Random.Default.nextBoolean()) {
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
        val depth = Random.Default.nextInt(4)
        if (depth == 0) return ""
        return (1..depth).joinToString(prefix = "/", separator = "/") {
            generateRandomString(3, 8).lowercase()
        }
    }

    private fun generateQueryParam(): String {
        if (Random.Default.nextBoolean()) return ""
        val count = Random.Default.nextInt(1, 4)
        val params = (1..count).joinToString("&") {
            "${generateRandomString(2, 5)}=${generateRandomString(3, 6)}"
        }
        return "?$params"
    }

    private fun generateTime(param: KParameter, type: KClass<*>): Any? {
        val now = Instant.now()
        val offsetDays = Random.Default.nextLong(1, DEFAULT_DATE_RANGE_DAYS)

        val targetInstant = when {
            param.has<Future>() || param.has<FutureOrPresent>() -> now.plus(offsetDays, ChronoUnit.DAYS)
            param.has<Past>() || param.has<PastOrPresent>() -> now.minus(offsetDays, ChronoUnit.DAYS)
            else -> return null
        }

        return convertToTimeType(targetInstant, type)
    }

    private fun convertToTimeType(instant: Instant, type: KClass<*>): Any? = when (type) {
        Instant::class -> instant
        String::class -> instant.toString()
        Date::class -> Date.from(instant)
        LocalDate::class -> LocalDate.ofInstant(instant, ZoneId.systemDefault())
        LocalDateTime::class -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        ZonedDateTime::class -> ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        else -> null
    }

    private fun generateNumericConstraint(param: KParameter, type: KClass<*>): Any? {
        param.find<IntRange>()?.let { return it.smartFuzz() }
        param.find<LongRange>()?.let { return it.smartFuzz() }
        param.find<DoubleRange>()?.let { return it.smartFuzz() }
        param.find<Digits>()?.let { return it.generate() }

        if (param.has<Positive>()) return generatePositive(type)
        if (param.has<PositiveOrZero>()) return generatePositive(type, includeZero = true)
        if (param.has<Negative>()) return generateNegative(type)
        if (param.has<NegativeOrZero>()) return generateNegative(type, includeZero = true)

        return null
    }

    private fun IntRange.smartFuzz(): Int = listOf(
        min, max,
        if (min < Int.MAX_VALUE) min + 1 else min,
        if (max > Int.MIN_VALUE) max - 1 else max,
        Random.Default.nextInt(min, max)
    ).random()

    private fun LongRange.smartFuzz(): Long = listOf(
        min, max,
        if (min < Long.MAX_VALUE) min + 1 else min,
        if (max > Long.MIN_VALUE) max - 1 else max,
        Random.Default.nextLong(min, max)
    ).random()

    private fun DoubleRange.smartFuzz(): Double = when(Random.Default.nextInt(5)) {
        0 -> min
        1 -> max
        else -> Random.Default.nextDouble(min, max)
    }

    private fun Digits.generate(): BigDecimal {
        val maxInteger = 10.0.pow(integer.toDouble())
        val randomVal = Random.Default.nextDouble(0.0, maxInteger)
        return BigDecimal.valueOf(randomVal).setScale(fraction, RoundingMode.HALF_UP)
    }

    private fun generatePositive(type: KClass<*>, includeZero: Boolean = false): Any = when (type) {
        Int::class -> if (includeZero) Random.Default.nextInt(0, Int.MAX_VALUE) else Random.Default.nextInt(1, Int.MAX_VALUE)
        Long::class -> if (includeZero) Random.Default.nextLong(0, Long.MAX_VALUE) else Random.Default.nextLong(1, Long.MAX_VALUE)
        Double::class -> if (includeZero) Random.Default.nextDouble(0.0, Double.MAX_VALUE) else Random.Default.nextDouble(0.1, Double.MAX_VALUE)
        BigDecimal::class -> BigDecimal.valueOf(Random.Default.nextDouble(0.1, Double.MAX_VALUE))
        else -> 1
    }

    private fun generateNegative(type: KClass<*>, includeZero: Boolean = false): Any = when (type) {
        Int::class -> if (includeZero) Random.Default.nextInt(Int.MIN_VALUE, 1) else Random.Default.nextInt(Int.MIN_VALUE, 0)
        Long::class -> if (includeZero) Random.Default.nextLong(Long.MIN_VALUE, 1) else Random.Default.nextLong(Long.MIN_VALUE, 0)
        Double::class -> if (includeZero) Random.Default.nextDouble(-Double.MAX_VALUE, 0.1) else Random.Default.nextDouble(-Double.MAX_VALUE, -0.0001)
        BigDecimal::class -> BigDecimal.valueOf(Random.Default.nextDouble(-Double.MAX_VALUE, -0.0001))
        else -> -1
    }

    private fun generateStringConstraint(param: KParameter): String? {
        val length = param.find<StringLength>()
        if (length != null) {
            return generateRandomString(length.min, length.max)
        }

        if (param.has<NotBlank>()) {
            return generateRandomString(1, NOT_BLANK_MAX_LENGTH)
        }

        return null
    }

    private fun generateRandomString(min: Int, max: Int = min + 10): String {
        val safeMin = min.coerceAtLeast(0)
        val safeMax = max.coerceAtMost(SAFE_STRING_LIMIT).coerceAtLeast(safeMin)
        val length = if (safeMin == safeMax) safeMin else Random.Default.nextInt(safeMin, safeMax + 1)

        return (1..length)
            .map{ ALPHANUMERIC_POOL.random() }
            .joinToString("")
    }


    private fun generatePrimitiveDefaults(type: KClass<*>): Any? = when (type) {
        String::class -> generateRandomString(DEFAULT_STRING_MIN_LENGTH, DEFAULT_STRING_MAX_LENGTH)
        Int::class -> Random.Default.nextInt(1, 100)
        Long::class -> Random.Default.nextLong(1, 1000)
        Boolean::class -> Random.Default.nextBoolean()
        Double::class -> Random.Default.nextDouble()
        Float::class -> Random.Default.nextFloat()
        BigDecimal::class -> BigDecimal.valueOf(Random.Default.nextDouble())
        else -> null
    }
    private fun generateRandomNumericString(length: Int): String {
        return (1..length).map { Random.Default.nextInt(0, 10) }.joinToString("")
    }

    private inline fun <reified T : Annotation> KParameter.has(): Boolean =
        findAnnotation<T>() != null

    private inline fun <reified T : Annotation> KParameter.find(): T? = findAnnotation<T>()
}
package execution.domain.service.validation

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
import discovery.api.NotEmpty
import discovery.api.NotNull
import discovery.api.Null
import discovery.api.Past
import discovery.api.PastOrPresent
import discovery.api.Pattern
import discovery.api.Positive
import discovery.api.PositiveOrZero
import discovery.api.Size
import discovery.api.StringLength
import discovery.api.Url
import discovery.api.Uuid
import exception.ContractViolationException
import execution.domain.vo.AnnotationRule
import execution.domain.vo.AssertionRule
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoLocalDateTime
import java.time.chrono.ChronoZonedDateTime
import java.util.Date
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotation

class ContractValidator(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun validate(
        element: KAnnotatedElement,
        value: Any?,
    ) {
        if (value == null) {
            if (element.has<NotNull>()) {
                throw ContractViolationException(
                    rule = AnnotationRule(NotNull::class),
                    message = "NotNull violation: value is null"
                )
            }
            return
        }

        if (element.has<Null>()) {
            throw ContractViolationException(
                rule = AnnotationRule(Null::class),
                message = "Null violation: value must be null but got '$value'"
            )
        }

        when (value) {
            is Boolean -> validateBoolean(element, value)
            is Number -> validateNumeric(element, value)
            is String -> validateString(element, value)
            is Collection<*> -> validateCollection(element, value)
            is Map<*, *> -> validateMap(element, value)
            is Array<*> -> validateArray(element, value)
            else -> {
                if (isTimeType(value)) {
                    validateTime(element, value)
                }
            }
        }
    }

    private fun validateBoolean(
        element: KAnnotatedElement,
        value: Boolean,
    ) {
        if (element.has<AssertTrue>()) {
            ensure(value, AnnotationRule(AssertTrue::class)) { "AssertTrue violation: expected true but got false" }
        }
        if (element.has<AssertFalse>()) {
            ensure(!value, AnnotationRule(AssertFalse::class)) { "AssertFalse violation: expected false but got true" }
        }
    }

    private fun validateNumeric(
        element: KAnnotatedElement,
        value: Number,
    ) {
        val decimalValue = toBigDecimal(value)

        element.find<IntRange>()?.let { range ->
            if (value is Int) {
                ensure(value in range.min..range.max, AnnotationRule(IntRange::class)) {
                    "IntRange violation: expected [${range.min}..${range.max}] but got $value"
                }
            }
        }

        element.find<LongRange>()?.let { range ->
            if (value is Long) {
                ensure(value in range.min..range.max, AnnotationRule(LongRange::class)) {
                    "LongRange violation: expected [${range.min}..${range.max}] but got $value"
                }
            }
        }

        element.find<DoubleRange>()?.let { range ->
            if (value is Double) {
                ensure(value >= range.min && value <= range.max, AnnotationRule(DoubleRange::class)) {
                    "DoubleRange violation: expected [${range.min}..${range.max}] but got $value"
                }
            }
        }

        element.find<DecimalMin>()?.let { min ->
            val minVal = BigDecimal(min.value)
            val result = decimalValue.compareTo(minVal)
            val rule = AnnotationRule(DecimalMin::class)
            if (min.inclusive) {
                ensure(result >= 0, rule) { "DecimalMin violation: expected >= ${min.value} but got $value" }
            } else {
                ensure(result > 0, rule) { "DecimalMin violation: expected > ${min.value} but got $value" }
            }
        }

        element.find<DecimalMax>()?.let { max ->
            val maxVal = BigDecimal(max.value)
            val result = decimalValue.compareTo(maxVal)
            val rule = AnnotationRule(DecimalMax::class)
            if (max.inclusive) {
                ensure(result <= 0, rule) { "DecimalMax violation: expected <= ${max.value} but got $value" }
            } else {
                ensure(result < 0, rule) { "DecimalMax violation: expected < ${max.value} but got $value" }
            }
        }

        element.find<Digits>()?.let { digits ->
            val integerPart = decimalValue.precision() - decimalValue.scale()
            val fractionPart = if (decimalValue.scale() < 0) 0 else decimalValue.scale()
            val rule = AnnotationRule(Digits::class)

            ensure(integerPart <= digits.integer, rule) {
                "Digits integer violation: expected max ${digits.integer} digits but got $integerPart"
            }
            ensure(fractionPart <= digits.fraction, rule) {
                "Digits fraction violation: expected max ${digits.fraction} digits but got $fractionPart"
            }
        }

        if (element.has<Positive>()) {
            ensure(decimalValue > BigDecimal.ZERO, AnnotationRule(Positive::class)) {
                "Positive violation: expected > 0 but got $value"
            }
        }

        if (element.has<PositiveOrZero>()) {
            ensure(decimalValue >= BigDecimal.ZERO, AnnotationRule(PositiveOrZero::class)) {
                "PositiveOrZero violation: expected >= 0 but got $value"
            }
        }

        if (element.has<Negative>()) {
            ensure(decimalValue < BigDecimal.ZERO, AnnotationRule(Negative::class)) {
                "Negative violation: expected < 0 but got $value"
            }
        }

        if (element.has<NegativeOrZero>()) {
            ensure(decimalValue <= BigDecimal.ZERO, AnnotationRule(NegativeOrZero::class)) {
                "NegativeOrZero violation: expected <= 0 but got $value"
            }
        }
    }

    private fun validateString(
        element: KAnnotatedElement,
        value: String,
    ) {
        element.find<StringLength>()?.let { limit ->
            ensure(value.length in limit.min..limit.max, AnnotationRule(StringLength::class)) {
                "StringLength violation: expected length [${limit.min}..${limit.max}] but got ${value.length}"
            }
        }

        if (element.has<NotBlank>()) {
            ensure(value.isNotBlank(), AnnotationRule(NotBlank::class)) { "NotBlank violation: result was blank" }
        }

        element.find<Pattern>()?.let { pattern ->
            ensure(value.matches(Regex(pattern.regexp)), AnnotationRule(Pattern::class)) {
                "Pattern violation: expected regex '${pattern.regexp}' but got '$value'"
            }
        }

        element.find<Email>()?.let { rule ->
            val assertionRule = AnnotationRule(Email::class)
            ensure(value.contains("@") && value.contains("."), assertionRule) {
                "Email violation: '$value' is not a valid email format"
            }

            val domain = value.substringAfter("@")

            if (rule.allow.isNotEmpty()) {
                val isAllowed = rule.allow.any { allowed -> domain == allowed || domain.endsWith(".$allowed") }
                ensure(isAllowed, assertionRule) {
                    "Email domain violation: '$domain' is not in allowed list ${rule.allow.toList()}"
                }
            }

            if (rule.block.isNotEmpty()) {
                val isBlocked = rule.block.any { blocked -> domain == blocked || domain.endsWith(".$blocked") }
                ensure(!isBlocked, assertionRule) { "Email domain violation: '$domain' is blocked" }
            }
        }

        element.find<Url>()?.let { rule ->
            val assertionRule = AnnotationRule(Url::class)
            val isValidProtocol = rule.protocol.any { value.startsWith("$it://") }
            ensure(isValidProtocol, assertionRule) {
                "Url protocol violation: '$value' does not start with ${rule.protocol.toList()}"
            }

            val host = value.substringAfter("://").substringBefore("/").substringBefore("?")

            if (rule.hostAllow.isNotEmpty()) {
                val isAllowed = rule.hostAllow.any { allowed -> host == allowed || host.endsWith(".$allowed") }
                ensure(isAllowed, assertionRule) {
                    "Url host violation: '$host' is not in allowed list ${rule.hostAllow.toList()}"
                }
            }

            if (rule.hostBlock.isNotEmpty()) {
                val isBlocked = rule.hostBlock.any { blocked -> host == blocked || host.endsWith(".$blocked") }
                ensure(!isBlocked, assertionRule) { "Url host violation: '$host' is blocked" }
            }
        }

        if (element.has<Uuid>()) {
            val uuidRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
            ensure(uuidRegex.matches(value), AnnotationRule(Uuid::class)) {
                "Uuid violation: '$value' is not a valid UUID format"
            }
        }
    }

    private fun validateCollection(
        element: KAnnotatedElement,
        value: Collection<*>,
    ) {
        element.find<Size>()?.let { limit ->
            ensure(value.size in limit.min..limit.max, AnnotationRule(Size::class)) {
                "Size violation: expected size [${limit.min}..${limit.max}] but got ${value.size}"
            }
        }

        if (element.has<NotEmpty>()) {
            ensure(value.isNotEmpty(), AnnotationRule(NotEmpty::class)) {
                "NotEmpty violation: collection was empty"
            }
        }
    }

    private fun validateMap(
        element: KAnnotatedElement,
        value: Map<*, *>,
    ) {
        element.find<Size>()?.let { limit ->
            ensure(value.size in limit.min..limit.max, AnnotationRule(Size::class)) {
                "Size violation: expected size [${limit.min}..${limit.max}] but got ${value.size}"
            }
        }
        if (element.has<NotEmpty>()) {
            ensure(value.isNotEmpty(), AnnotationRule(NotEmpty::class)) {
                "NotEmpty violation: map was empty"
            }
        }
    }

    private fun validateArray(
        element: KAnnotatedElement,
        value: Array<*>,
    ) {
        element.find<Size>()?.let { limit ->
            ensure(value.size in limit.min..limit.max, AnnotationRule(Size::class)) {
                "Size violation: expected size [${limit.min}..${limit.max}] but got ${value.size}"
            }
        }
        if (element.has<NotEmpty>()) {
            ensure(value.isNotEmpty(), AnnotationRule(NotEmpty::class)) {
                "NotEmpty violation: array was empty"
            }
        }
    }

    private fun validateTime(
        element: KAnnotatedElement,
        value: Any,
    ) {
        val now = Instant.now(clock)
        val target = toInstant(value) ?: return

        if (element.has<Past>()) {
            ensure(target.isBefore(now), AnnotationRule(Past::class)) {
                "Past violation: expected past date but got $value"
            }
        }
        if (element.has<PastOrPresent>()) {
            ensure(!target.isAfter(now), AnnotationRule(PastOrPresent::class)) {
                "PastOrPresent violation: expected past or present date but got $value"
            }
        }
        if (element.has<Future>()) {
            ensure(target.isAfter(now), AnnotationRule(Future::class)) {
                "Future violation: expected future date but got $value"
            }
        }
        if (element.has<FutureOrPresent>()) {
            ensure(!target.isBefore(now), AnnotationRule(FutureOrPresent::class)) {
                "FutureOrPresent violation: expected future or present date but got $value"
            }
        }
    }

    private fun isTimeType(value: Any): Boolean =
        value is Instant ||
                value is Date ||
                value is ChronoLocalDate ||
                value is ChronoLocalDateTime<*> ||
                value is ChronoZonedDateTime<*>

    private fun toInstant(value: Any): Instant? =
        when (value) {
            is Instant -> value
            is Date -> value.toInstant()
            is ChronoLocalDate ->
                runCatching {
                    value.atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant()
                }.getOrNull()

            is ChronoLocalDateTime<*> -> value.atZone(ZoneId.systemDefault()).toInstant()
            is ChronoZonedDateTime<*> -> value.toInstant()
            else -> null
        }

    private fun toBigDecimal(value: Number): BigDecimal =
        when (value) {
            is BigDecimal -> value
            is Double -> BigDecimal.valueOf(value)
            is Float -> BigDecimal.valueOf(value.toDouble())
            is Long -> BigDecimal.valueOf(value)
            is Int -> BigDecimal.valueOf(value.toLong())
            is Short -> BigDecimal.valueOf(value.toLong())
            is Byte -> BigDecimal.valueOf(value.toLong())
            else -> BigDecimal(value.toString())
        }

    private inline fun <reified T : Annotation> KAnnotatedElement.find(): T? = findAnnotation<T>()

    private inline fun <reified T : Annotation> KAnnotatedElement.has(): Boolean = findAnnotation<T>() != null

    private inline fun ensure(
        condition: Boolean,
        rule: AssertionRule,
        lazyMessage: () -> String,
    ) {
        if (!condition) {
            throw ContractViolationException(rule, lazyMessage())
        }
    }
}

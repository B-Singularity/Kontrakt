package execution.domain.service

import discovery.api.*
import discovery.api.Digits
import discovery.api.IntRange
import discovery.api.LongRange
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoLocalDateTime
import java.time.chrono.ChronoZonedDateTime
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotation

class ContractValidator {

    fun validate(element: KAnnotatedElement, value: Any?) {

        if (value == null) {
            if (element.has<NotNull>()) {
                throw ContractViolationException("NotNull violation: value is null")
            }
            return
        }

        if (element.has<Null>()) {
            throw ContractViolationException("Null violation: value must be null but got '$value'")
        }
    }

    private fun validateBoolean(element: KAnnotatedElement, value: Boolean) {
        if (element.has<AssertTrue>()) {
            ensure(value) { "AssertTrue violation: expected true but got false" }
        }
        if (element.has<AssertFalse>()) {
            ensure(!value) { "AssertFalse violation: expected false but got true" }
        }
    }

    private fun validateNumeric(element: KAnnotatedElement, value: Number) {
        val decimalValue = toBigDecimal(value)

        element.find<IntRange>()?.let { range ->
            if (value is Int) {
                ensure(value in range.min..range.max) {
                    "IntRange violation: expected [${range.min}..${range.max}] but got $value"
                }
            }
        }

        element.find<LongRange>()?.let { range ->
            if (value is Long) {
                ensure(value in range.min..range.max) {
                    "LongRange violation: expected [${range.min}..${range.max}] but got $value"
                }
            }
        }

        element.find<DoubleRange>()?.let { range ->
            if (value is Double) {
                ensure(value >= range.min && value <= range.max) {
                    "DoubleRange violation: expected [${range.min}..${range.max}] but got $value"
                }
            }
        }

        element.find<DecimalMin>()?.let { min ->
            val minVal = BigDecimal(min.value)
            val result = decimalValue.compareTo(minVal)
            if (min.inclusive) {
                ensure(result >= 0) { "DecimalMin violation: expected >= ${min.value} but got $value" }
            } else {
                ensure(result > 0) { "DecimalMin violation: expected > ${min.value} but got $value" }
            }
        }

        element.find<DecimalMax>()?.let { max ->
            val maxVal = BigDecimal(max.value)
            val result = decimalValue.compareTo(maxVal)
            if (max.inclusive) {
                ensure(result <= 0) { "DecimalMax violation: expected <= ${max.value} but got $value" }
            } else {
                ensure(result < 0) { "DecimalMax violation: expected < ${max.value} but got $value" }
            }
        }

        element.find<Digits>()?.let { digits ->
            val integerPart = decimalValue.precision() - decimalValue.scale()
            val fractionPart = if (decimalValue.scale() < 0) 0 else decimalValue.scale()

            ensure(integerPart <= digits.integer) { "Digits integer violation: expected max ${digits.integer} digits but got $integerPart" }
            ensure(fractionPart <= digits.fraction) { "Digits fraction violation: expected max ${digits.fraction} digits but got $fractionPart" }
        }

        if (element.has<Positive>()) {
            ensure(decimalValue > BigDecimal.ZERO) { "Positive violation: expected > 0 but got $value" }
        }

        if (element.has<PositiveOrZero>()) {
            ensure(decimalValue >= BigDecimal.ZERO) { "PositiveOrZero violation: expected >= 0 but got $value" }
        }

        if (element.has<Negative>()) {
            ensure(decimalValue < BigDecimal.ZERO) { "Negative violation: expected < 0 but got $value" }
        }

        if (element.has<NegativeOrZero>()) {
            ensure(decimalValue <= BigDecimal.ZERO) { "NegativeOrZero violation: expected <= 0 but got $value" }
        }
    }

    private fun isTimeType(value: Any): Boolean {
        return value is Instant || value is Date || value is ChronoLocalDate || value is ChronoLocalDateTime<*> || value is ChronoZonedDateTime<*>
    }

    private fun toInstant(value: Any): Instant? = when (value) {
        is Instant -> value
        is Date -> value.toInstant()
        is ChronoLocalDate -> runCatching {
            value.atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant()
        }.getOrNull()

        is ChronoLocalDateTime<*> -> value.atZone(ZoneId.systemDefault()).toInstant()
        is ChronoZonedDateTime<*> -> value.toInstant()
        else -> null
    }

    private fun toBigDecimal(value: Number): BigDecimal = when (value) {
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

    private inline fun ensure(condition: Boolean, lazyMessage: () -> String) {
        if (!condition) {
            throw ContractViolationException(lazyMessage())
        }
    }

    class ContractViolationException(message: String) : RuntimeException(message)
}
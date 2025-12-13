package execution.domain.generator

import discovery.api.Future
import discovery.api.FutureOrPresent
import discovery.api.Past
import discovery.api.PastOrPresent
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class TimeTypeGenerator(
    private val clock: Clock = Clock.systemDefaultZone()
) : TypeGenerator {

    private val defaultDateRangeDays = 3650L

    override fun supports(param: KParameter): Boolean {
        val type = param.type.classifier as? KClass<*> ?: return false
        return type == Instant::class || type == LocalDateTime::class || type == LocalDate::class ||
                type == ZonedDateTime::class || type == Date::class
    }

    override fun generateValidBoundaries(param: KParameter): List<Any?> = buildList {
        val type = param.type.classifier as KClass<*>
        val now = clock.instant()

        if (param.has<PastOrPresent>() || param.has<FutureOrPresent>()) {
            add(convert(now, type))
        }
        if (param.has<Past>()) {
            add(convert(now.minusMillis(1), type))
        }
        if (param.has<Future>()) {
            add(convert(now.plusMillis(1), type))
        }
    }

    override fun generate(param: KParameter): Any? {
        val type = param.type.classifier as KClass<*>

        val now = clock.instant()
        val offsetDays = Random.nextLong(1, defaultDateRangeDays)

        val targetInstant = when {
            param.has<Future>() || param.has<FutureOrPresent>() -> now.plus(offsetDays, ChronoUnit.DAYS)
            param.has<Past>() || param.has<PastOrPresent>() -> now.minus(offsetDays, ChronoUnit.DAYS)
            else -> now
        }
        return convert(targetInstant, type)
    }

    override fun generateInvalid(param: KParameter): List<Any?> = buildList {
        val type = param.type.classifier as KClass<*>
        val now = clock.instant()

        if (param.has<Past>()) add(convert(now.plusSeconds(10), type))
        if (param.has<Future>()) add(convert(now.minusSeconds(10), type))
    }

    private fun convert(instant: Instant, type: KClass<*>): Any? = when (type) {
        Instant::class -> instant
        String::class -> instant.toString()
        Date::class -> Date.from(instant)
        LocalDate::class -> LocalDate.ofInstant(instant, clock.zone)
        LocalDateTime::class -> LocalDateTime.ofInstant(instant, clock.zone)
        ZonedDateTime::class -> ZonedDateTime.ofInstant(instant, clock.zone)
        else -> null
    }

    private inline fun <reified T : Annotation> KParameter.has(): Boolean = findAnnotation<T>() != null
}
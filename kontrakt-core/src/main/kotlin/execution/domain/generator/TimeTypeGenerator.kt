package execution.domain.generator

import discovery.api.Future
import discovery.api.FutureOrPresent
import discovery.api.Past
import discovery.api.PastOrPresent
import execution.exception.ConflictingAnnotationsException
import execution.exception.InvalidAnnotationValueException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.reflect.KClass

class TimeTypeGenerator(
    private val clock: Clock = Clock.systemDefaultZone()
) : TerminalGenerator {

    private val defaultMaxSeconds = 315_360_000L

    override fun supports(request: GenerationRequest): Boolean {
        val type = request.type.classifier as? KClass<*> ?: return false
        return type == Instant::class || type == LocalDateTime::class || type == LocalDate::class ||
                type == ZonedDateTime::class || type == Date::class
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext
    ): List<Any?> = buildList {
        val type = request.type.classifier as KClass<*>
        val currentClock = context.clock

        val past = request.find<Past>()
        val pastOrPresent = request.find<PastOrPresent>()
        val future = request.find<Future>()
        val futureOrPresent = request.find<FutureOrPresent>()

        validateConflict(request.name, past, pastOrPresent, future, futureOrPresent)
        val targetZone = extractZone(request.name, past, pastOrPresent, future, futureOrPresent) ?: currentClock.zone

        fun addBoundaries(
            base: String,
            value: Long,
            unit: ChronoUnit,
            isPast: Boolean,
            allowPresent: Boolean
        ) {
            val anchor = parseBaseTime(base, targetZone.id, request.name, currentClock)
            val maxSeconds = calculateSeconds(value, unit, request.name)

            val nearOffset = if (allowPresent) 0L else 1L

            if (isPast) {
                add(convert(anchor.minusSeconds(nearOffset), type, targetZone))
                add(convert(anchor.minusSeconds(maxSeconds), type, targetZone))
            } else {
                add(convert(anchor.plusSeconds(nearOffset), type, targetZone))
                add(convert(anchor.plusSeconds(maxSeconds), type, targetZone))
            }
        }

        when {
            past != null -> addBoundaries(past.base, past.value, past.unit, isPast = true, allowPresent = false)
            pastOrPresent != null -> addBoundaries(
                pastOrPresent.base,
                pastOrPresent.value,
                pastOrPresent.unit,
                isPast = true,
                allowPresent = true
            )

            future != null -> addBoundaries(
                future.base,
                future.value,
                future.unit,
                isPast = false,
                allowPresent = false
            )

            futureOrPresent != null -> addBoundaries(
                futureOrPresent.base,
                futureOrPresent.value,
                futureOrPresent.unit,
                isPast = false,
                allowPresent = true
            )

            else -> add(convert(currentClock.instant(), type, targetZone))
        }
    }


    override fun generate(
        request: GenerationRequest,
        context: GenerationContext
    ): Any? {
        val type = request.type.classifier as KClass<*>
        val random = context.seededRandom
        val currentClock = context.clock

        val past = request.find<Past>()
        val pastOrPresent = request.find<PastOrPresent>()
        val future = request.find<Future>()
        val futureOrPresent = request.find<FutureOrPresent>()

        validateConflict(request.name, past, pastOrPresent, future, futureOrPresent)

        val targetInstant = when {
            past != null -> {
                val anchor = parseBaseTime(past.base, past.zone, request.name, currentClock)
                val maxSeconds = calculateSeconds(past.value, past.unit, request.name)
                val offset = random.nextLong(0, maxSeconds + 1)
                anchor.minusSeconds(offset)
            }

            pastOrPresent != null -> {
                val anchor = parseBaseTime(pastOrPresent.base, pastOrPresent.zone, request.name, currentClock)
                val maxSeconds = calculateSeconds(pastOrPresent.value, pastOrPresent.unit, request.name)
                val offset = random.nextLong(0, maxSeconds + 1)
                anchor.minusSeconds(offset)
            }

            future != null -> {
                val anchor = parseBaseTime(future.base, future.zone, request.name, currentClock)
                val maxSeconds = calculateSeconds(future.value, future.unit, request.name)
                val offset = random.nextLong(0, maxSeconds + 1)
                anchor.plusSeconds(offset)
            }

            futureOrPresent != null -> {
                val anchor = parseBaseTime(futureOrPresent.base, futureOrPresent.zone, request.name, currentClock)
                val maxSeconds = calculateSeconds(futureOrPresent.value, futureOrPresent.unit, request.name)
                val offset = random.nextLong(0, maxSeconds + 1)
                anchor.plusSeconds(offset)
            }

            else -> {
                val now = currentClock.instant()
                val offset = random.nextLong(0, defaultMaxSeconds + 1)
                if (random.nextBoolean()) now.plusSeconds(offset) else now.minusSeconds(offset)
            }
        }
        val targetZone = extractZone(request.name, past, pastOrPresent, future, futureOrPresent) ?: currentClock.zone
        return convert(targetInstant, type, targetZone)
    }

    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext
    ): List<Any?> = buildList {
        val type = request.type.classifier as KClass<*>
        val currentClock = context.clock

        val past = request.find<Past>()
        val pastOrPresent = request.find<PastOrPresent>()
        val future = request.find<Future>()
        val futureOrPresent = request.find<FutureOrPresent>()

        val targetZone = extractZone(request.name, past, pastOrPresent, future, futureOrPresent) ?: currentClock.zone

        if (past != null || pastOrPresent != null) {
            val baseStr = past?.base ?: pastOrPresent!!.base
            val zoneStr = past?.zone ?: pastOrPresent!!.zone
            val value = past?.value ?: pastOrPresent!!.value
            val unit = past?.unit ?: pastOrPresent!!.unit

            val anchor = parseBaseTime(baseStr, zoneStr, request.name, currentClock)
            val maxSeconds = calculateSeconds(value, unit, request.name)

            add(convert(anchor.plusSeconds(10), type, targetZone))
            add(convert(anchor.minusSeconds(maxSeconds + 86400), type, targetZone))
        }

        if (future != null || futureOrPresent != null) {
            val baseStr = future?.base ?: futureOrPresent!!.base
            val zoneStr = future?.zone ?: futureOrPresent!!.zone
            val value = future?.value ?: futureOrPresent!!.value
            val unit = future?.unit ?: futureOrPresent!!.unit

            val anchor = parseBaseTime(baseStr, zoneStr, request.name, currentClock)
            val maxSeconds = calculateSeconds(value, unit, request.name)

            add(convert(anchor.minusSeconds(10), type, targetZone))
            add(convert(anchor.plusSeconds(maxSeconds + 86400), type, targetZone))
        }
    }

    private fun validateConflict(fieldName: String, vararg annotations: Annotation?) {
        val presentAnnotations = annotations.filterNotNull()
        if (presentAnnotations.size > 1) {
            throw ConflictingAnnotationsException(
                fieldName = fieldName,
                annotations = presentAnnotations.map { "@${it.annotationClass.simpleName}" }
            )
        }
    }

    private fun extractZone(
        fieldName: String,
        past: Past?, pastOrPresent: PastOrPresent?,
        future: Future?, futureOrPresent: FutureOrPresent?
    ): ZoneId? {
        val zoneStr = past?.zone ?: pastOrPresent?.zone ?: future?.zone ?: futureOrPresent?.zone
        return zoneStr?.let {
            try {
                ZoneId.of(it)
            } catch (e: Exception) {
                throw InvalidAnnotationValueException(
                    fieldName = fieldName,
                    value = it,
                    reason = "Invalid Zone ID. Please use valid IDs like 'UTC', 'Asia/Seoul'."
                )
            }
        }
    }

    private fun parseBaseTime(base: String, zoneStr: String, fieldName: String, clock: Clock): Instant {
        if (base.equals("NOW", ignoreCase = true)) return clock.instant()

        val zone = runCatching { ZoneId.of(zoneStr) }.getOrDefault(ZoneId.of("UTC"))

        return runCatching { Instant.parse(base) }.getOrNull()
            ?: runCatching { LocalDate.parse(base).atStartOfDay(zone).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(base).atZone(zone).toInstant() }.getOrNull()
            ?: throw InvalidAnnotationValueException(
                fieldName = fieldName,
                value = base,
                reason = "Invalid date format. Accepted formats: 'NOW', ISO-8601 Instant, or ISO Date/DateTime."
            )
    }

    private fun calculateSeconds(value: Long, unit: ChronoUnit, fieldName: String): Long {
        if (value <= 0) {
            throw InvalidAnnotationValueException(
                fieldName = fieldName,
                value = value,
                reason = "Duration value must be positive."
            )
        }
        return try {
            Math.multiplyExact(value, unit.duration.seconds)
        } catch (e: ArithmeticException) {
            Long.MAX_VALUE
        }
    }


    private fun convert(instant: Instant, type: KClass<*>, zoneId: ZoneId): Any? = when (type) {
        Instant::class -> instant
        String::class -> instant.toString()
        Date::class -> Date.from(instant)
        LocalDate::class -> LocalDate.ofInstant(instant, zoneId)
        LocalDateTime::class -> LocalDateTime.ofInstant(instant, zoneId)
        ZonedDateTime::class -> ZonedDateTime.ofInstant(instant, zoneId)
        else -> null
    }

}
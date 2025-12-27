package execution.domain.generator

import discovery.api.Future
import discovery.api.FutureOrPresent
import discovery.api.Past
import discovery.api.PastOrPresent
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
    private val clock: Clock = Clock.systemDefaultZone(),
) : TerminalGenerator {
    private val defaultMaxSeconds = 315_360_000L

    override fun supports(request: GenerationRequest): Boolean {
        val type = request.type.classifier as? KClass<*> ?: return false
        return type == Instant::class ||
            type == LocalDateTime::class ||
            type == LocalDate::class ||
            type == ZonedDateTime::class ||
            type == Date::class
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext,
    ): List<Any?> =
        buildList {
            val type = request.type.classifier as KClass<*>
            val currentClock = context.clock
            val targetZone = extractZone(request, currentClock)

            fun addBounds(
                base: String,
                value: Long,
                unit: ChronoUnit,
                isPast: Boolean,
                allowPresent: Boolean,
            ) {
                val anchor = parseBaseTime(base, targetZone, request.name, currentClock)
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
                request.has<Past>() -> {
                    val ann = request.find<Past>()!!
                    addBounds(ann.base, ann.value, ann.unit, isPast = true, allowPresent = false)
                }

                request.has<PastOrPresent>() -> {
                    val ann = request.find<PastOrPresent>()!!
                    addBounds(ann.base, ann.value, ann.unit, isPast = true, allowPresent = true)
                }

                request.has<Future>() -> {
                    val ann = request.find<Future>()!!
                    addBounds(ann.base, ann.value, ann.unit, isPast = false, allowPresent = false)
                }

                request.has<FutureOrPresent>() -> {
                    val ann = request.find<FutureOrPresent>()!!
                    addBounds(ann.base, ann.value, ann.unit, isPast = false, allowPresent = true)
                }

                else -> {
                    add(convert(currentClock.instant(), type, targetZone))
                }
            }
        }

    override fun generate(
        request: GenerationRequest,
        context: GenerationContext,
    ): Any? {
        val type = request.type.classifier as KClass<*>
        val random = context.seededRandom
        val currentClock = context.clock
        val targetZone = extractZone(request, currentClock)

        fun randomOffset(
            maxSeconds: Long,
            allowPresent: Boolean,
        ): Long {
            val minOffset = if (allowPresent) 0L else 1L

            val safeMax = maxSeconds.coerceAtLeast(minOffset)

            return random.nextLong(minOffset, safeMax + 1)
        }

        val targetInstant =
            when {
                request.has<Past>() -> {
                    val ann = request.find<Past>()!!
                    val anchor = parseBaseTime(ann.base, targetZone, request.name, currentClock)
                    val maxSeconds = calculateSeconds(ann.value, ann.unit, request.name)
                    anchor.minusSeconds(randomOffset(maxSeconds, allowPresent = false))
                }

                request.has<PastOrPresent>() -> {
                    val ann = request.find<PastOrPresent>()!!
                    val anchor = parseBaseTime(ann.base, targetZone, request.name, currentClock)
                    val maxSeconds = calculateSeconds(ann.value, ann.unit, request.name)
                    anchor.minusSeconds(randomOffset(maxSeconds, allowPresent = true))
                }

                request.has<Future>() -> {
                    val ann = request.find<Future>()!!
                    val anchor = parseBaseTime(ann.base, targetZone, request.name, currentClock)
                    val maxSeconds = calculateSeconds(ann.value, ann.unit, request.name)
                    anchor.plusSeconds(randomOffset(maxSeconds, allowPresent = false))
                }

                request.has<FutureOrPresent>() -> {
                    val ann = request.find<FutureOrPresent>()!!
                    val anchor = parseBaseTime(ann.base, targetZone, request.name, currentClock)
                    val maxSeconds = calculateSeconds(ann.value, ann.unit, request.name)
                    anchor.plusSeconds(randomOffset(maxSeconds, allowPresent = true))
                }

                else -> {
                    val now = currentClock.instant()
                    val offset = randomOffset(defaultMaxSeconds, allowPresent = false)
                    if (random.nextBoolean()) now.plusSeconds(offset) else now.minusSeconds(offset)
                }
            }

        return convert(targetInstant, type, targetZone)
    }

    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext,
    ): List<Any?> =
        buildList {
            val type = request.type.classifier as KClass<*>
            val currentClock = context.clock
            val targetZone = extractZone(request, currentClock)

            fun addInvalid(
                base: String,
                value: Long,
                unit: ChronoUnit,
                shouldBePast: Boolean,
            ) {
                val anchor = parseBaseTime(base, targetZone, request.name, currentClock)
                val maxSeconds = calculateSeconds(value, unit, request.name)

                if (shouldBePast) {
                    add(convert(anchor.plusSeconds(10), type, targetZone))
                    add(convert(anchor.minusSeconds(maxSeconds + 86400), type, targetZone))
                } else {
                    add(convert(anchor.minusSeconds(10), type, targetZone))
                    add(convert(anchor.plusSeconds(maxSeconds + 86400), type, targetZone))
                }
            }

            when {
                request.has<Past>() -> {
                    val ann = request.find<Past>()!!
                    addInvalid(ann.base, ann.value, ann.unit, shouldBePast = true)
                }

                request.has<PastOrPresent>() -> {
                    val ann = request.find<PastOrPresent>()!!
                    addInvalid(ann.base, ann.value, ann.unit, shouldBePast = true)
                }

                request.has<Future>() -> {
                    val ann = request.find<Future>()!!
                    addInvalid(ann.base, ann.value, ann.unit, shouldBePast = false)
                }

                request.has<FutureOrPresent>() -> {
                    val ann = request.find<FutureOrPresent>()!!
                    addInvalid(ann.base, ann.value, ann.unit, shouldBePast = false)
                }
            }
        }

    private fun extractZone(
        request: GenerationRequest,
        currentClock: Clock,
    ): ZoneId {
        val zoneStr =
            request.find<Past>()?.zone
                ?: request.find<PastOrPresent>()?.zone
                ?: request.find<Future>()?.zone
                ?: request.find<FutureOrPresent>()?.zone

        return if (zoneStr.isNullOrBlank() || zoneStr == "UTC") {
            currentClock.zone
        } else {
            try {
                ZoneId.of(zoneStr)
            } catch (e: Exception) {
                throw InvalidAnnotationValueException(
                    fieldName = request.name,
                    value = zoneStr,
                    reason = "Invalid Zone ID. Please use valid IDs like 'UTC', 'Asia/Seoul'.",
                )
            }
        }
    }

    private fun parseBaseTime(
        base: String,
        zone: ZoneId,
        fieldName: String,
        clock: Clock,
    ): Instant {
        if (base.equals("NOW", ignoreCase = true)) return clock.instant()

        return runCatching { Instant.parse(base) }.getOrNull()
            ?: runCatching { LocalDate.parse(base).atStartOfDay(zone).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(base).atZone(zone).toInstant() }.getOrNull()
            ?: throw InvalidAnnotationValueException(
                fieldName = fieldName,
                value = base,
                reason = "Invalid date format. Accepted formats: 'NOW', ISO-8601 Instant, or ISO Date/DateTime.",
            )
    }

    private fun calculateSeconds(
        value: Long,
        unit: ChronoUnit,
        fieldName: String,
    ): Long {
        if (value <= 0) {
            throw InvalidAnnotationValueException(
                fieldName = fieldName,
                value = value,
                reason = "Duration value must be positive.",
            )
        }
        return try {
            Math.multiplyExact(value, unit.duration.seconds)
        } catch (e: ArithmeticException) {
            Long.MAX_VALUE
        }
    }

    private fun convert(
        instant: Instant,
        type: KClass<*>,
        zoneId: ZoneId,
    ): Any? =
        when (type) {
            Instant::class -> instant
            String::class -> instant.toString()
            Date::class -> Date.from(instant)
            LocalDate::class -> LocalDate.ofInstant(instant, zoneId)
            LocalDateTime::class -> LocalDateTime.ofInstant(instant, zoneId)
            ZonedDateTime::class -> ZonedDateTime.ofInstant(instant, zoneId)
            else -> null
        }
}

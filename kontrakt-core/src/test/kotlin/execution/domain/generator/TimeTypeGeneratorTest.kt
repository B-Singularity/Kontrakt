package execution.domain.generator

import discovery.api.Future
import discovery.api.FutureOrPresent
import discovery.api.Past
import discovery.api.PastOrPresent
import execution.exception.InvalidAnnotationValueException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TimeTypeGeneratorTest {
    private val fixedNow = Instant.parse("2025-01-01T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedNow, ZoneId.of("UTC"))
    private val context =
        GenerationContext(
            seededRandom = Random(42),
            clock = fixedClock,
        )
    private val generator = TimeTypeGenerator(fixedClock)

    @Suppress("UNUSED_PARAMETER")
    class TimeTestTargets {
        fun plainInstant(arg: Instant) {}

        fun plainLocalDate(arg: LocalDate) {}

        fun plainZonedDateTime(arg: ZonedDateTime) {}

        fun unsupportedType(arg: String) {}

        // --- Past Constraints ---
        fun pastStrict(
            @Past(value = 10, unit = ChronoUnit.SECONDS) arg: Instant,
        ) {}

        fun pastOrPresent(
            @PastOrPresent(value = 10, unit = ChronoUnit.SECONDS) arg: Instant,
        ) {}

        // --- Future Constraints ---
        fun futureStrict(
            @Future(value = 10, unit = ChronoUnit.SECONDS) arg: Instant,
        ) {}

        fun futureOrPresent(
            @FutureOrPresent(value = 10, unit = ChronoUnit.SECONDS) arg: Instant,
        ) {}

        // --- Complex Parameters (Zone & Base) ---
        // Base: 2000, Zone: Seoul (UTC+9)
        fun complexPast(
            @Past(base = "2000-01-01T00:00:00", value = 1, unit = ChronoUnit.DAYS, zone = "Asia/Seoul")
            arg: ZonedDateTime,
        ) {
        }

        // --- Invalid Configurations ---
        fun invalidZone(
            @Past(value = 1, zone = "Mars/Colony") arg: Instant,
        ) {}

        fun negativeValue(
            @Future(value = -5) arg: Instant,
        ) {}

        fun invalidBaseFormat(
            @Past(base = "NotADate", value = 1) arg: Instant,
        ) {}
    }

    data class TimeScenario(
        val description: String,
        val targetFunction: KFunction<*>,
        val validator: (Any?) -> Unit,
        val boundaryValidator: (List<Any?>) -> Unit,
        val invalidValidator: (List<Any?>) -> Unit,
    )

    private fun request(func: KFunction<*>): GenerationRequest {
        val param = func.parameters.last()
        return GenerationRequest.from(param)
    }

    @Test
    fun `Support Contract - verifies type compatibility`() {
        val targets = TimeTestTargets()

        assertTrue(generator.supports(request(targets::plainInstant)), "Should support Instant")
        assertTrue(generator.supports(request(targets::plainLocalDate)), "Should support LocalDate")
        assertTrue(generator.supports(request(targets::plainZonedDateTime)), "Should support ZonedDateTime")

        assertFalse(generator.supports(request(targets::unsupportedType)), "Should NOT support String")
    }

    @Test
    fun `Generation Contract - verifies branching logic and parameters`() {
        val targets = TimeTestTargets()

        val scenarios =
            listOf(
                // [Case 1] @Past (Strict: < NOW)
                TimeScenario(
                    description = "@Past (Strict)",
                    targetFunction = targets::pastStrict,
                    validator = {
                        val inst = it as Instant
                        // NOW - 10s <= generated <= NOW - 1s (Strict)
                        assertTrue(inst.isBefore(fixedNow), "Strict Past must be before NOW")
                        assertTrue(inst >= fixedNow.minusSeconds(10), "Must be within limit")
                    },
                    boundaryValidator = { list ->
                        // Boundaries: NOW - 1s, NOW - 10s
                        assertTrue(list.contains(fixedNow.minusSeconds(1)), "Should contain near boundary (Now-1s)")
                        assertTrue(list.contains(fixedNow.minusSeconds(10)), "Should contain far boundary (Now-10s)")
                        assertFalse(list.contains(fixedNow), "Strict Past should NOT contain NOW")
                    },
                    invalidValidator = { list ->
                        // Invalids: Future values
                        val inst = list.first() as Instant
                        assertTrue(inst.isAfter(fixedNow), "Invalid for Past should be Future")
                    },
                ),
                // [Case 2] @PastOrPresent (Permissive: <= NOW)
                TimeScenario(
                    description = "@PastOrPresent (Permissive)",
                    targetFunction = targets::pastOrPresent,
                    validator = {
                        val inst = it as Instant
                        // NOW - 10s <= generated <= NOW
                        assertFalse(inst.isAfter(fixedNow), "PastOrPresent must not be after NOW")
                        assertTrue(inst >= fixedNow.minusSeconds(10), "Must be within limit")
                    },
                    boundaryValidator = { list ->
                        // Boundaries: NOW, NOW - 10s
                        assertTrue(list.contains(fixedNow), "Permissive should contain NOW")
                        assertTrue(list.contains(fixedNow.minusSeconds(10)), "Should contain limit")
                    },
                    invalidValidator = { list ->
                        val inst = list.first() as Instant
                        assertTrue(inst.isAfter(fixedNow), "Invalid for PastOrPresent should be Future")
                    },
                ),
                // [Case 3] @Future (Strict: > NOW)
                TimeScenario(
                    description = "@Future (Strict)",
                    targetFunction = targets::futureStrict,
                    validator = {
                        val inst = it as Instant
                        assertTrue(inst.isAfter(fixedNow), "Strict Future must be after NOW")
                        assertTrue(inst <= fixedNow.plusSeconds(10), "Must be within limit")
                    },
                    boundaryValidator = { list ->
                        // Boundaries: NOW + 1s, NOW + 10s
                        assertTrue(list.contains(fixedNow.plusSeconds(1)), "Should contain near boundary (Now+1s)")
                        assertFalse(list.contains(fixedNow), "Strict Future should NOT contain NOW")
                    },
                    invalidValidator = { list ->
                        val inst = list.first() as Instant
                        assertTrue(inst.isBefore(fixedNow), "Invalid for Future should be Past")
                    },
                ),
                // [Case 4] Complex Parameters (Base, Zone, Unit, ZonedDateTime Type)
                TimeScenario(
                    description = "Complex: @Past with Base(2000y), Zone(Seoul), Unit(Days)",
                    targetFunction = targets::complexPast,
                    validator = {
                        val zdt = it as ZonedDateTime
                        val base = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("Asia/Seoul"))

                        // Check Zone
                        assertEquals(ZoneId.of("Asia/Seoul"), zdt.zone, "Should respect Zone parameter")

                        // Check Logic: 2000-01-01 minus 1 DAY
                        assertTrue(zdt.isBefore(base), "Should be before base time")
                        assertTrue(zdt >= base.minusDays(1), "Should be within 1 day range")
                    },
                    boundaryValidator = { list ->
                        val base = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("Asia/Seoul"))
                        // Strict Past: Base - 1s (Near), Base - 1 Day (Far)
                        assertTrue(
                            list.any { (it as ZonedDateTime).isEqual(base.minusSeconds(1)) },
                            "Should contain Base-1s",
                        )
                        assertTrue(
                            list.any { (it as ZonedDateTime).toLocalDate() == LocalDate.of(1999, 12, 31) },
                            "Should contain Base-1Day",
                        )
                    },
                    invalidValidator = { list ->
                        val zdt = list.first() as ZonedDateTime
                        val base = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("Asia/Seoul"))
                        assertTrue(zdt.isAfter(base), "Invalid should be after base time")
                    },
                ),
                // [Case 5] Plain LocalDate (Zone handling check)
                TimeScenario(
                    description = "Plain LocalDate (Default Zone UTC)",
                    targetFunction = targets::plainLocalDate,
                    validator = {
                        it as LocalDate
                        // 2025-01-01T12:00:00Z -> LocalDate is 2025-01-01
                        // Random range roughly 10 years, just check type here mainly
                        assertNotNull(it)
                    },
                    boundaryValidator = { list ->
                        // Should contain conversion of NOW
                        val expected = LocalDate.ofInstant(fixedNow, ZoneId.of("UTC"))
                        assertTrue(list.contains(expected), "Should contain NOW converted to LocalDate")
                    },
                    invalidValidator = {
                        // No annotation -> No specific invalid logic defined in generator for plain type yet
                        // strict logic only applies when constraint exists.
                    },
                ),
            )

        for (scenario in scenarios) {
            val req = request(scenario.targetFunction)
            val prefix = "[${scenario.description}]"

            // 1. Generate
            val value = generator.generate(req, context)
            scenario.validator(value)

            // 2. Boundaries
            val boundaries = generator.generateValidBoundaries(req, context)
            scenario.boundaryValidator(boundaries)

            // 3. Invalid
            if (scenario.description.startsWith("@")) { // Only test invalid for constrained scenarios
                val invalids = generator.generateInvalid(req, context)
                scenario.invalidValidator(invalids)
            }
        }
    }

    @Test
    fun `Exception Contract - verifies validation of parameters`() {
        val targets = TimeTestTargets()

        // 1. Invalid Zone ID
        assertFailsWith<InvalidAnnotationValueException>("Should throw on invalid Zone ID") {
            generator.generate(request(targets::invalidZone), context)
        }.let {
            assertTrue(it.message!!.contains("Invalid Zone ID"), "Message should specify error cause")
        }

        // 2. Negative Duration Value
        assertFailsWith<InvalidAnnotationValueException>("Should throw on negative Value") {
            generator.generate(request(targets::negativeValue), context)
        }.let {
            assertTrue(it.message!!.contains("positive"), "Message should mention positive value requirement")
        }

        // 3. Invalid Base Format
        assertFailsWith<InvalidAnnotationValueException>("Should throw on bad Base format") {
            generator.generate(request(targets::invalidBaseFormat), context)
        }.let {
            assertTrue(it.message!!.contains("date format"), "Message should mention format error")
        }
    }
}

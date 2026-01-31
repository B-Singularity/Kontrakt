package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import java.time.Instant

/**
 * [Generic Executor]
 * Generates an Instant within [min, max] and transforms it using [converter].
 *
 * ### Features
 * 1. **High Precision**: Randomizes both Seconds and Nanoseconds.
 * 2. **Overflow Safe**: Uses [GeneratorUtils.nextLongInclusive] for mathematically uniform seconds.
 * 3. **Type Safe**: Uses Generic <T> and functional converter.
 *
 * @param T Target type (e.g. LocalDate)
 */
class TimeGenerator<T : Any>(
    private val min: Instant,
    private val max: Instant,
    private val converter: (Instant) -> T
) : Generator<T> {

    init {
        require(!min.isAfter(max)) { "Invalid range: $min > $max" }
    }

    override fun generate(context: GenerationContext): T {
        val instant = generateRandomInstant(context)
        return converter(instant)
    }

    override fun generateEdgeCases(context: GenerationContext): List<T> = buildList {
        add(converter(min))
        if (min != max) add(converter(max))

        // Near-Boundaries (Off-by-one second)
        if (min.isBefore(Instant.MAX.minusSeconds(1))) {
            val near = min.plusSeconds(1)
            if (!near.isAfter(max)) add(converter(near))
        }
        if (max.isAfter(Instant.MIN.plusSeconds(1))) {
            val near = max.minusSeconds(1)
            if (!near.isBefore(min)) add(converter(near))
        }
    }

    /**
     * Generates "Logical Invalid" values (Range Violations).
     *
     * ### ⚠️ Note on "Invalid"
     * These values represent **Logical Contract Violations** (e.g., a Past date when Future is required).
     * They do NOT necessarily represent **Type Violations** or Crashes.
     * The [converter] may successfully transform these into valid objects (e.g. LocalDate),
     * but they will be semantically invalid according to the contract.
     */
    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        runCatching { add(converter(min.minusSeconds(1))) }
        runCatching { add(converter(max.plusSeconds(1))) }
    }

    private fun generateRandomInstant(context: GenerationContext): Instant {
        if (min == max) return min
        val random = context.seededRandom

        // Smart Fuzzing (10% bias towards boundaries)
        if (random.nextDouble() < 0.1) return if (random.nextBoolean()) min else max

        // 1. Generate Seconds (Mathematically Uniform & Safe)
        val minSec = min.epochSecond
        val maxSec = max.epochSecond
        val rndSec = GeneratorUtils.nextLongInclusive(random, minSec, maxSec)

        // 2. Generate Nanoseconds (Boundary Aware)
        // If we picked the min/max second, we must constrain the nanos.
        val minNano = if (rndSec == minSec) min.nano else 0
        val maxNano = if (rndSec == maxSec) max.nano else 999_999_999

        // Defensive: Check just in case minSec == maxSec and minNano > maxNano (Should be caught by init)
        val rndNano = if (minNano == maxNano) minNano else random.nextInt(minNano, maxNano + 1)

        return Instant.ofEpochSecond(rndSec, rndNano.toLong())
    }
}
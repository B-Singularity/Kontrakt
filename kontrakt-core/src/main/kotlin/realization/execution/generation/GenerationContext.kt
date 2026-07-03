package realization.execution.generation

import diagnostic.retention.material.retained.TraceSink
import java.time.Clock
import kotlin.random.Random

data class GenerationContext(
    val seededRandom: Random,
    val clock: Clock,
    val traceSink: TraceSink,
)

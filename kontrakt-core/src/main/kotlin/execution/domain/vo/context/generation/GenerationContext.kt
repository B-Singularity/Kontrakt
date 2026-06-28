package execution.domain.vo.context.generation

import stage.diagnostic.evidence.TraceSink
import java.time.Clock
import kotlin.random.Random

data class GenerationContext(
    val seededRandom: Random,
    val clock: Clock,
    val traceSink: TraceSink,
)

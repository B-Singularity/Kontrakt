package execution.domain.generator

import java.time.Clock
import kotlin.random.Random
import kotlin.reflect.KClass

data class GenerationContext(
    val seededRandom: Random,
    val clock: Clock,
    val history: Set<KClass<*>> = emptySet(),

    )
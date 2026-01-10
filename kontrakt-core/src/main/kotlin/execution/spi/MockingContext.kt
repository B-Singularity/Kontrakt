package execution.spi

import execution.domain.service.generation.FixtureGenerator
import execution.spi.trace.ScenarioTrace

/**
 * [Context Object] Encapsulates the execution context required for mocking operations.
 *
 * This allows the [MockingEngine] to be stateless and thread-safe by receiving
 * all necessary dependencies (Generator, Trace) via this context.
 */
data class MockingContext(
    val generator: FixtureGenerator,
    val trace: ScenarioTrace,
)

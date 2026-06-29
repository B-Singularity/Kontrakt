package realization.runtime.cache

import realization.runtime.policy.PolicyTelemetryStore
import realization.runtime.policy.RuntimeTelemetrySample

/**
 * Adapter-local, best-effort, non-throwing telemetry sink for L2 hot-path events.
 *
 * This is intentionally not a Domain Core outbound port.
 * It is an adapter-internal abstraction.
 */
interface L2TelemetrySink {
    fun record(sample: RuntimeTelemetrySample)
}

/**
 * Default sink that drops all samples.
 */
object NoopL2TelemetrySink : L2TelemetrySink {
    override fun record(sample: RuntimeTelemetrySample) = Unit
}

/**
 * Store-backed sink adapter.
 *
 * Failures are swallowed intentionally so telemetry never becomes
 * a semantic dependency of the planner/L2 hot path.
 */
class StoreBackedL2TelemetrySink(
    private val store: PolicyTelemetryStore,
) : L2TelemetrySink {
    override fun record(sample: RuntimeTelemetrySample) {
        try {
            store.record(sample)
        } catch (_: Throwable) {
            // Best-effort only.
        }
    }
}

package planning.infrastructure.runtime.policy

/**
 * Slow-path telemetry store used by policy resolution.
 *
 * Hot-path adapter code should not depend directly on this interface.
 * Instead, hot-path code should emit into an adapter-local sink.
 */
interface PolicyTelemetryStore {
    fun snapshot(): RecentTelemetry?

    fun record(sample: RuntimeTelemetrySample)
}

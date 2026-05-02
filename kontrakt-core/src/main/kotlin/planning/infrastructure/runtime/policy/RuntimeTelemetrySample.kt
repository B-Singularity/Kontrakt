package planning.infrastructure.runtime.policy

/**
 * Numeric/event-oriented telemetry sample emitted by adapter/runtime layers.
 *
 * This payload must not retain planner object graphs or mutable worker-local state.
 */
sealed interface RuntimeTelemetrySample {
    data class JoinTimedOut(
        val waitedNanos: Long,
        val policyEpochId: Long,
    ) : RuntimeTelemetrySample

    data class WaiterAttachRejected(
        val policyEpochId: Long,
    ) : RuntimeTelemetrySample

    data class SpeculativeQuotaExhausted(
        val policyEpochId: Long,
    ) : RuntimeTelemetrySample

    data class CircuitOpened(
        val policyEpochId: Long,
    ) : RuntimeTelemetrySample
}

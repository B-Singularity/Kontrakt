package planning.infrastructure.runtime.policy

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Aggregated telemetry snapshot used by adaptive runtime policy resolution.
 *
 * This telemetry is an input to the next policy snapshot only.
 * It must never mutate an already-running session policy.
 */
data class RecentTelemetry(
    val avgJoinWaitNanos: Long,
    val maxJoinWaitNanos: Long,
    val joinTimeoutCount: Long,
    val waiterAttachRejectedCount: Long,
    val speculativeQuotaExhaustCount: Long,
    val circuitOpenCount: Long,
    val hotKeyRate: Double,
    val duplicateBuildRatio: Double,
) {
    init {
        if (avgJoinWaitNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.avgJoinWaitNanos must be >= 0: $avgJoinWaitNanos",
            )
        }
        if (maxJoinWaitNanos < 0L) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.maxJoinWaitNanos must be >= 0: $maxJoinWaitNanos",
            )
        }
        if (joinTimeoutCount < 0L) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.joinTimeoutCount must be >= 0: $joinTimeoutCount",
            )
        }
        if (waiterAttachRejectedCount < 0L) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.waiterAttachRejectedCount must be >= 0: $waiterAttachRejectedCount",
            )
        }
        if (speculativeQuotaExhaustCount < 0L) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.speculativeQuotaExhaustCount must be >= 0: $speculativeQuotaExhaustCount",
            )
        }
        if (circuitOpenCount < 0L) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.circuitOpenCount must be >= 0: $circuitOpenCount",
            )
        }
        if (hotKeyRate < 0.0 || hotKeyRate > 1.0) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.hotKeyRate must be in [0, 1]: $hotKeyRate",
            )
        }
        if (duplicateBuildRatio < 0.0) {
            throw PlanningProtocolIntegrityException(
                "RecentTelemetry.duplicateBuildRatio must be >= 0: $duplicateBuildRatio",
            )
        }
    }
}

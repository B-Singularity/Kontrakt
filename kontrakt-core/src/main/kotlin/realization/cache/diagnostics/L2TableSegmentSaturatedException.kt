package realization.cache.diagnostics

import realization.planning.diagnostics.PlanningInfrastructureException

/**
 * Infrastructure-local governance signal emitted by [realization.cache.storage.LongKeyTable].
 *
 * Meaning:
 * - this is NOT a order failure
 * - this is NOT a domain corruption
 * - adapter/region should translate this into capacity fault / circuit-open / bypass
 *
 * Keep this internal to the cache subsystem.
 */
internal class L2TableSegmentSaturatedException(
    private val segmentIndex: Int,
    private val tableCapacity: Int,
    private val stripeCount: Int,
    private val approxOccupiedCount: Long,
    cause: Throwable? = null,
) : PlanningInfrastructureException(
    message = "L2 table segment saturated: segmentIndex=$segmentIndex",
    cause = cause,
) {
    override val payload: Map<String, Any?> =
        mapOf(
            "segmentIndex" to segmentIndex,
            "tableCapacity" to tableCapacity,
            "stripeCount" to stripeCount,
            "approxOccupiedCount" to approxOccupiedCount,
        )
}
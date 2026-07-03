package migration.quarantine

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Immutable snapshot of L2 storage governance.
 *
 * This contract governs retention, survivability, and storage-triggered
 * degradation only. It does NOT alter semantic output, exact-match law,
 * routing identity, or planner-core structural caps.
 *
 * ADR alignment:
 * - ADR-0033 formally adds ResolvedStorageGovernance to the v1 runtime-policy surface.
 * - Storage governance is distinct from both L1 structural sizing and L2 join governance.
 */
class ResolvedStorageGovernance private constructor(
    val maxApproxBytesPerPartition: Long,
    val maxEntriesPerPartition: Int,
    val circuitOpenOnStorageExhaustion: Boolean,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxApproxBytesPerPartition: Long,
            maxEntriesPerPartition: Int,
            circuitOpenOnStorageExhaustion: Boolean,
        ): ResolvedStorageGovernance {
            if (maxApproxBytesPerPartition <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedStorageGovernance.maxApproxBytesPerPartition must be > 0: $maxApproxBytesPerPartition",
                )
            }
            if (maxEntriesPerPartition <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedStorageGovernance.maxEntriesPerPartition must be > 0: $maxEntriesPerPartition",
                )
            }

            return ResolvedStorageGovernance(
                maxApproxBytesPerPartition = maxApproxBytesPerPartition,
                maxEntriesPerPartition = maxEntriesPerPartition,
                circuitOpenOnStorageExhaustion = circuitOpenOnStorageExhaustion,
            )
        }
    }
}

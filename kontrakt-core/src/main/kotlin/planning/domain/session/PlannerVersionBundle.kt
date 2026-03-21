package planning.domain.session

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Immutable version tuple for deterministic planning, cache stability,
 * protocol traceability, and explicit semantic/version boundaries.
 *
 * This bundle intentionally preserves the multi-axis version tuple instead
 * of collapsing it into a single opaque string.
 */
class PlannerVersionBundle private constructor(
    val normalizationSpecVersion: Int,
    val edgeOrderingVersion: Int,
    val capabilityProfileVersion: Int,
    val workAccountingVersion: Int,
    val entropyVersion: Int,
    val entropySeed: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            normalizationSpecVersion: Int,
            edgeOrderingVersion: Int,
            capabilityProfileVersion: Int,
            workAccountingVersion: Int,
            entropyVersion: Int,
            entropySeed: Long,
        ): PlannerVersionBundle {
            if (normalizationSpecVersion < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerVersionBundle.normalizationSpecVersion must be >= 0: $normalizationSpecVersion"
                )
            }
            if (edgeOrderingVersion < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerVersionBundle.edgeOrderingVersion must be >= 0: $edgeOrderingVersion"
                )
            }
            if (capabilityProfileVersion < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerVersionBundle.capabilityProfileVersion must be >= 0: $capabilityProfileVersion"
                )
            }
            if (workAccountingVersion < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerVersionBundle.workAccountingVersion must be >= 0: $workAccountingVersion"
                )
            }
            if (entropyVersion < 0) {
                throw PlanningProtocolIntegrityException(
                    "PlannerVersionBundle.entropyVersion must be >= 0: $entropyVersion"
                )
            }

            return PlannerVersionBundle(
                normalizationSpecVersion = normalizationSpecVersion,
                edgeOrderingVersion = edgeOrderingVersion,
                capabilityProfileVersion = capabilityProfileVersion,
                workAccountingVersion = workAccountingVersion,
                entropyVersion = entropyVersion,
                entropySeed = entropySeed,
            )
        }
    }
}
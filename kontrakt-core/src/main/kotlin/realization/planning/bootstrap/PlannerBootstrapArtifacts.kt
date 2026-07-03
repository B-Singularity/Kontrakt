package realization.planning.bootstrap

import migration.quarantine.L2GovernanceConfig
import migration.quarantine.L2StorageGovernanceConfig
import realization.planning.session.PlannerSessionConfig

/**
 * Fully assembled session-fixed runtime artifacts derived from one pinned policy snapshot.
 *
 * This object exists to keep bootstrap assembly explicit and to avoid hidden side channels
 * between L1 structural sizing and L2 governance wiring.
 */
class PlannerBootstrapArtifacts private constructor(
    val sessionConfig: PlannerSessionConfig,
    val l2GovernanceConfig: L2GovernanceConfig,
    val l2StorageGovernanceConfig: L2StorageGovernanceConfig,
) {
    companion object {
        @JvmStatic
        fun issue(
            sessionConfig: PlannerSessionConfig,
            l2GovernanceConfig: L2GovernanceConfig,
            l2StorageGovernanceConfig: L2StorageGovernanceConfig,
        ): PlannerBootstrapArtifacts =
            PlannerBootstrapArtifacts(
                sessionConfig = sessionConfig,
                l2GovernanceConfig = l2GovernanceConfig,
                l2StorageGovernanceConfig = l2StorageGovernanceConfig,
            )
    }
}

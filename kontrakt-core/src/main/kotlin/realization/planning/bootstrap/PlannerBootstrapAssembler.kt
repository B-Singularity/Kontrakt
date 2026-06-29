package realization.planning.bootstrap

import governance.policy.PlannerCapacityResolver
import realization.planning.session.PlannerSessionConfig
import realization.runtime.cache.L2GovernanceConfig
import realization.runtime.cache.L2StorageGovernanceConfig
import realization.runtime.policy.RuntimePolicyRegistry
import versioning.coordinate.contract.planning.PlannerVersionBundle

/**
 * Runtime-boundary assembler that:
 * 1. pins the current policy epoch,
 * 2. resolves planner structural caps deterministically from the pinned budget,
 * 3. constructs the core session config,
 * 4. materializes adapter-local L2 governance bridges.
 *
 * This is the physical implementation of the session-fixed snapshot rule.
 */
object PlannerBootstrapAssembler {
    @JvmStatic
    fun assemble(
        registry: RuntimePolicyRegistry,
        capacityResolver: PlannerCapacityResolver,
        versions: PlannerVersionBundle,
    ): PlannerBootstrapArtifacts {
        val pinnedPolicy = registry.currentEpoch().policy
        val pinnedBudget = pinnedPolicy.sessionBudget

        val resolvedCaps = capacityResolver.resolve(pinnedBudget)

        val sessionConfig =
            PlannerSessionConfig.issue(
                versions = versions,
                budget = pinnedBudget,
                caps = resolvedCaps,
            )

        val l2GovernanceConfig = L2GovernanceConfig.from(pinnedPolicy.joinGovernance)
        val l2StorageGovernanceConfig = L2StorageGovernanceConfig.from(pinnedPolicy.storageGovernance)

        return PlannerBootstrapArtifacts.issue(
            sessionConfig = sessionConfig,
            l2GovernanceConfig = l2GovernanceConfig,
            l2StorageGovernanceConfig = l2StorageGovernanceConfig,
        )
    }
}

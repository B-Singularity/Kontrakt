package migration.quarantine

import stage.lowering.material.candidate.ResolvedSpec
import stage.lowering.material.candidate.ScenarioRequirements
import java.util.SortedSet

/**
 * Strategy interface for resolving abstract requirements into concrete specifications.
 *
 * @contract Implementations MUST return a [SortedSet] strictly governed by
 * [ResolvedSpec.Companion.CANONICAL_ORDER] to guarantee deterministic provisioning.
 */
interface BindingStrategy {
    fun resolve(requirements: ScenarioRequirements): SortedSet<ResolvedSpec>
}
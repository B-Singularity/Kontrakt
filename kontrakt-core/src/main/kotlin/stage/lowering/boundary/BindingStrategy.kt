package stage.lowering.boundary

import stage.lowering.material.ResolvedSpec
import stage.lowering.material.ScenarioRequirements
import java.util.SortedSet

/**
 * Strategy interface for resolving abstract requirements into concrete specifications.
 *
 * @contract Implementations MUST return a [java.util.SortedSet] strictly governed by
 * [ResolvedSpec.Companion.CANONICAL_ORDER] to guarantee deterministic provisioning.
 */
interface BindingStrategy {
    fun resolve(requirements: ScenarioRequirements): SortedSet<ResolvedSpec>
}
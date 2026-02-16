package linking.domain.port

import linking.domain.model.ResolvedSpec
import linking.domain.model.ScenarioRequirements
import java.util.SortedSet

/**
 * Strategy interface for resolving abstract requirements into concrete specifications.
 *
 * @contract Implementations MUST return a [SortedSet] strictly governed by
 * [ResolvedSpec.CANONICAL_ORDER] to guarantee deterministic provisioning.
 */
interface BindingStrategy {
    fun resolve(requirements: ScenarioRequirements): SortedSet<ResolvedSpec>
}
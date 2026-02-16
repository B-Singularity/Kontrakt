package linking.domain.port.outgoing

import linking.domain.model.ResolvedSpec
import linking.domain.model.ScenarioRequirements
import java.util.SortedSet

/**
 * Strategy interface for resolving abstract requirements into concrete specifications.
 *
 * @contract Implementations MUST return a [java.util.SortedSet] strictly governed by
 * [linking.domain.model.ResolvedSpec.Companion.CANONICAL_ORDER] to guarantee deterministic provisioning.
 */
interface BindingStrategy {
    fun resolve(requirements: ScenarioRequirements): SortedSet<ResolvedSpec>
}
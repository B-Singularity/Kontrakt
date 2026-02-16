package linking.domain.phase

import linking.domain.model.ResolvedSpec
import linking.domain.model.ScenarioRequirements
import linking.domain.port.outgoing.BindingStrategy
import java.util.SortedSet

/**
 * Phase 3A: Resolution.
 *
 * Pure functional phase responsible for mapping requirements to specifications
 * via the provided strategy.
 */
class ResolutionPhase(
    private val bindingStrategy: BindingStrategy
) {
    fun execute(requirements: ScenarioRequirements): SortedSet<ResolvedSpec> {
        return bindingStrategy.resolve(requirements)
    }
}
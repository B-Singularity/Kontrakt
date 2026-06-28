package linking.domain.phase

import stage.lowering.material.ResolvedSpec
import stage.lowering.material.ScenarioRequirements
import stage.lowering.boundary.BindingStrategy
import java.util.SortedSet

/**
 * Phase 3A: Resolution.
 *
 * Pure functional phase responsible for mapping requirements to specifications
 * via the provided strategy.
 */
class ResolutionPhase(
    private val bindingStrategy: BindingStrategy,
) {
    fun execute(requirements: ScenarioRequirements): SortedSet<ResolvedSpec> = bindingStrategy.resolve(requirements)
}

package statemachine.state.material.condition

import migration.quarantine.BindingStrategy
import stage.lowering.material.candidate.ResolvedSpec
import stage.lowering.material.candidate.ScenarioRequirements
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
package realization.graph.linking

import stage.lowering.material.candidate.ResolvedSpec
import stage.lowering.material.candidate.ScenarioRequirements
import statemachine.state.material.condition.IntegrityPhase
import statemachine.state.material.condition.ResolutionPhase
import java.util.SortedSet

/**
 * The main entry point for the Linking Step (Step 3).
 *
 * Orchestrates the resolution and integrity check phases.
 * Returns a strictly ordered, validated set of specifications.
 */
class LinkerService(
    private val resolutionPhase: ResolutionPhase,
    private val integrityPhase: IntegrityPhase,
) {
    fun link(requirements: ScenarioRequirements): SortedSet<ResolvedSpec> {
        // Step 3A: Resolve Requirements to Specs
        val rawSpecs = resolutionPhase.execute(requirements)

        // Step 3B: Validate Integrity (Fail-Fast)
        integrityPhase.validate(rawSpecs)

        return rawSpecs
    }
}
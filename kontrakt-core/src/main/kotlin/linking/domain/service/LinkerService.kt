package linking.domain.service

import linking.domain.model.ResolvedSpec
import linking.domain.model.ScenarioRequirements
import linking.domain.phase.IntegrityPhase
import linking.domain.phase.ResolutionPhase
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

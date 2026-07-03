package stage.lowering.material.candidate

import stage.lowering.diagnostics.LinkingInputException
import java.util.Collections
import java.util.SortedSet
import java.util.TreeSet

/**
 * Represents the raw requirements collected from a test scenario.
 *
 * Guarantees immutability, determinism, and non-null content.
 * Actively performs silent deduplication of features via SortedSet.
 */
class ScenarioRequirements(
    val scenarioId: ScenarioId,
    rawFeatures: Collection<String?>,
) {
    // VO based SortedSet
    val requiredFeatures: SortedSet<FeatureName>

    init {
        val safeSortedSet = TreeSet<FeatureName>()

        rawFeatures.forEach { feature ->
            if (feature == null) {
                throw LinkingInputException(
                    "Null feature detected.",
                    mapOf("scenarioId" to scenarioId.value),
                )
            }
            // VO creation implicitly validates 'isNotBlank'
            // We catch potential VO validation errors if needed, or let them propagate.
            try {
                safeSortedSet.add(FeatureName(feature))
            } catch (e: LinkingInputException) {
                throw LinkingInputException(
                    "Invalid feature name detected.",
                    mapOf("scenarioId" to scenarioId.value, "cause" to e.message),
                )
            }
        }

        this.requiredFeatures = Collections.unmodifiableSortedSet(safeSortedSet)
    }
}

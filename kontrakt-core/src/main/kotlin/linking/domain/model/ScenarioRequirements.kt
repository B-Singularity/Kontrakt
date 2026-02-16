package linking.domain.model

import linking.domain.exception.LinkingInputException
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
    val scenarioId: String,
    rawFeatures: Collection<String?>
) {
    /**
     * The set of required features, guaranteed to be non-null, non-blank, and sorted.
     */
    val requiredFeatures: SortedSet<String>

    init {
        if (scenarioId.isBlank()) {
            throw LinkingInputException("Scenario ID cannot be blank")
        }

        // Active Deduplication & Sorting & Fail-Fast
        val safeSortedSet = TreeSet<String>()

        rawFeatures.forEach { feature ->
            if (feature == null) {
                throw LinkingInputException(
                    "Null feature detected.",
                    mapOf("scenarioId" to scenarioId)
                )
            }
            if (feature.isBlank()) {
                throw LinkingInputException(
                    "Blank feature detected.",
                    mapOf("scenarioId" to scenarioId)
                )
            }
            safeSortedSet.add(feature)
        }

        // Enforce Immutability
        this.requiredFeatures = Collections.unmodifiableSortedSet(safeSortedSet)
    }
}
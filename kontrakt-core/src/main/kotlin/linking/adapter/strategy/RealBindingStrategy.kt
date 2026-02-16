package linking.adapter.strategy

import linking.domain.exception.LinkingInputException
import linking.domain.model.ResolvedSpec
import linking.domain.model.ScenarioRequirements
import linking.domain.port.BindingStrategy
import java.util.Collections
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet

/**
 * Default implementation of [BindingStrategy].
 * * Ensures determinism by using [ResolvedSpec.CANONICAL_ORDER] and [Locale.ROOT].
 */
class RealBindingStrategy : BindingStrategy {

    override fun resolve(requirements: ScenarioRequirements): SortedSet<ResolvedSpec> {
        // Enforce the Constitutional Comparator
        val resolvedSpecs = TreeSet(ResolvedSpec.CANONICAL_ORDER)

        requirements.requiredFeatures.forEach { feature ->
            resolvedSpecs.add(resolveSingle(feature))
        }

        return Collections.unmodifiableSortedSet(resolvedSpecs)
    }

    private fun resolveSingle(feature: String): ResolvedSpec {
        // Double Defense: Strategy-level validation to protect against direct calls bypassing the model.
        if (feature.isBlank()) {
            throw LinkingInputException(
                "Cannot resolve blank feature key.",
                mapOf("input_feature" to feature)
            )
        }

        // Use Locale.ROOT to ensure deterministic behavior across different OS environments (e.g., Turkey).
        val normalizedFeature = feature.lowercase(Locale.ROOT)

        // Placeholder for Registry lookup logic
        return ResolvedSpec(
            name = feature,
            implementationClass = "com.acme.impl.default.$normalizedFeature.${feature}Impl"
        )
    }
}
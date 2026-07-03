package realization.graph.binding

import migration.quarantine.BindingStrategy
import stage.lowering.material.candidate.FeatureName
import stage.lowering.material.candidate.ResolvedSpec
import stage.lowering.material.candidate.ScenarioRequirements
import java.util.Collections
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet

/**
 * Default implementation of [BindingStrategy].
 * * Ensures determinism by using [ResolvedSpec.Companion.CANONICAL_ORDER] and [java.util.Locale.ROOT].
 */
class RealBindingStrategy : BindingStrategy {
    override fun resolve(requirements: ScenarioRequirements): SortedSet<ResolvedSpec> {
        val resolvedSpecs = TreeSet(ResolvedSpec.CANONICAL_ORDER)

        requirements.requiredFeatures.forEach { featureName ->
            resolvedSpecs.add(resolveSingle(featureName))
        }

        return Collections.unmodifiableSortedSet(resolvedSpecs)
    }

    private fun resolveSingle(featureName: FeatureName): ResolvedSpec {
        // Deterministic normalization using Locale.ROOT
        val rawName = featureName.value
        val normalizedFeature = rawName.lowercase(Locale.ROOT)

        return ResolvedSpec(
            featureName = featureName,
            implementationClass = "com.acme.impl.default.$normalizedFeature.${rawName}Impl",
        )
    }
}
package stage.lowering.material.candidate

import stage.lowering.diagnostics.LinkingProtocolException
import stage.lowering.material.candidate.ResolvedSpec.Companion.CANONICAL_ORDER

/**
 * Represents a fully resolved specification ready for provisioning.
 *
 * ## CONSTITUTION
 * This class serves as the fundamental data carrier for the Linking Phase.
 * **CRITICAL:** Sorting and Deduplication MUST rely exclusively on [CANONICAL_ORDER].
 */
data class ResolvedSpec(
    val featureName: FeatureName,
    val implementationClass: String,
) {
    init {
        // Enforce invariants using Domain Exceptions (Fail-Fast)
        if (implementationClass.isBlank()) {
            throw LinkingProtocolException(
                "ResolvedSpec invariant violated: implementationClass is blank",
                context = mapOf("featureName" to featureName.value),
            )
        }
    }

    companion object {
        /**
         * [The Law] The single source of truth for sorting and equality in the Linking domain.
         * Used by BindingStrategy and IntegrityPhase to guarantee determinism.
         */
        val CANONICAL_ORDER: Comparator<ResolvedSpec> =
            compareBy(
                { it.featureName },
                { it.implementationClass },
            )
    }
}

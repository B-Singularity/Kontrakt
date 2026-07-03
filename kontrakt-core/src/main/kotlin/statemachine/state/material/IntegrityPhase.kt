package statemachine.state.material

import stage.lowering.diagnostics.LinkingAmbiguityException
import stage.lowering.diagnostics.LinkingInputException
import stage.lowering.diagnostics.LinkingProtocolException
import stage.lowering.material.candidate.FeatureName
import stage.lowering.material.candidate.ResolvedSpec
import java.util.SortedSet

/**
 * Phase 3B: Integrity Check.
 *
 * Validates the logical consistency and structural integrity of the resolved specifications.
 * strictly enforces the framework's "Constitution" (Canonical Order).
 */
class IntegrityPhase {
    fun validate(specs: SortedSet<ResolvedSpec>) {
        if (specs.isEmpty()) {
            throw LinkingInputException("Linking failed: empty specifications.")
        }

        // 1. STRICT CONSTITUTION CHECK
        if (specs.comparator() !== ResolvedSpec.CANONICAL_ORDER) {
            val actual = specs.comparator()
            throw LinkingProtocolException(
                "Framework Invariant Violation: Specs must be governed by CANONICAL_ORDER.",
                context =
                    mapOf(
                        "expected" to "ResolvedSpec.CANONICAL_ORDER",
                        "actual_class" to (actual?.javaClass?.name ?: "Natural"),
                        "actual_string" to (actual?.toString() ?: "null"),
                    ),
            )
        }

        val violations = mutableListOf<String>()
        var previousSpec: ResolvedSpec? = null
        var currentFeatureName: FeatureName? = null

        // Deterministic error reporting
        val currentGroupImpls = linkedSetOf<String>()

        for (spec in specs) {
            // 2. RUNTIME PROTOCOL CHECK (Monotonicity)
            if (previousSpec != null && ResolvedSpec.CANONICAL_ORDER.compare(previousSpec, spec) > 0) {
                throw LinkingProtocolException(
                    "Internal Protocol Violation: Specs are not monotonically increasing.",
                    context =
                        mapOf(
                            "prev_name" to previousSpec.featureName.value,
                            "prev_impl" to previousSpec.implementationClass,
                            "curr_name" to spec.featureName.value,
                            "curr_impl" to spec.implementationClass,
                        ),
                )
            }
            previousSpec = spec

            // 3. CONTRADICTION CHECK (Ambiguity)
            if (spec.featureName != currentFeatureName) {
                if (currentGroupImpls.size > 1) {
                    violations.add("Contradictory contracts for feature '$currentFeatureName': $currentGroupImpls")
                }

                currentFeatureName = spec.featureName
                currentGroupImpls.clear()
            }
            currentGroupImpls.add(spec.implementationClass)
        }

        if (currentGroupImpls.size > 1) {
            violations.add("Contradictory contracts for feature '$currentFeatureName': $currentGroupImpls")
        }

        if (violations.isNotEmpty()) {
            throw LinkingAmbiguityException(
                "Integrity check failed. Contradictory annotations detected.",
                violations = violations,
            )
        }
    }
}
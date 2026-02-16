package linking.domain.phase

import linking.domain.exception.LinkingAmbiguityException
import linking.domain.exception.LinkingInputException
import linking.domain.exception.LinkingProtocolException
import linking.domain.model.ResolvedSpec
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

        // 1. STRICT CONSTITUTION CHECK (Framework Invariant)
        // Verify that the Set is governed by the EXACT Constitutional Comparator object via reference check (===).
        if (specs.comparator() !== ResolvedSpec.CANONICAL_ORDER) {
            val actual = specs.comparator()
            throw LinkingProtocolException(
                "Framework Invariant Violation: Specs must be governed by CANONICAL_ORDER.",
                context = mapOf(
                    "expected" to "ResolvedSpec.CANONICAL_ORDER",
                    "actual_class" to (actual?.javaClass?.name ?: "Natural"),
                    "actual_string" to (actual?.toString() ?: "null")
                )
            )
        }

        val violations = mutableListOf<String>()
        var previousSpec: ResolvedSpec? = null
        var currentName: String? = null

        // Use LinkedHashSet to ensure deterministic order in error reports.
        val currentGroupImpls = linkedSetOf<String>()

        for (spec in specs) {
            // 2. RUNTIME PROTOCOL CHECK (Monotonicity)
            // Ensures strict adherence to the sorting contract during iteration.
            if (previousSpec != null && ResolvedSpec.CANONICAL_ORDER.compare(previousSpec, spec) > 0) {
                throw LinkingProtocolException(
                    "Internal Protocol Violation: Specs are not monotonically increasing.",
                    context = mapOf(
                        "prev_name" to previousSpec.name,
                        "prev_impl" to previousSpec.implementationClass,
                        "curr_name" to spec.name,
                        "curr_impl" to spec.implementationClass
                    )
                )
            }
            previousSpec = spec

            // 3. CONTRADICTION CHECK (Ambiguity Detection)
            if (spec.name != currentName) {
                // New group detected. Check previous group for ambiguity.
                if (currentGroupImpls.size > 1) {
                    violations.add("Contradictory contracts for feature '$currentName': $currentGroupImpls")
                }

                // Reset for new group
                currentName = spec.name
                currentGroupImpls.clear()
            }
            currentGroupImpls.add(spec.implementationClass)
        }

        // Flush the last group
        if (currentGroupImpls.size > 1) {
            violations.add("Contradictory contracts for feature '$currentName': $currentGroupImpls")
        }

        if (violations.isNotEmpty()) {
            throw LinkingAmbiguityException(
                "Integrity check failed. Contradictory annotations detected.",
                violations = violations
            )
        }
    }
}
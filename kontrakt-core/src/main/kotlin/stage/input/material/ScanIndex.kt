package stage.input.material

import stage.input.material.ScanIndex.Companion.of
import java.util.Collections
import java.util.SortedMap
import java.util.TreeMap
import java.util.TreeSet

/**
 * [Symbol Table]
 * The immutable result of the Discovery Phase.
 * Contains purely static metadata (TypeIds) without loading actual classes.
 *
 * ## Self-Enforced Determinism & Immutability (Closed Structure)
 * - **Construction**: Restricted via Factory [of] only.
 * - **No Copy**: 'data class' is avoided to prevent the 'copy()' backdoor which bypasses validation.
 * - **Determinism**: Internally enforces sorting (TreeSet/TreeMap).
 * - **Immutability**: Internally enforces unmodifiable wrappers.
 */
class ScanIndex private constructor(
    val scenarios: List<TypeId>,
    val contracts: SortedMap<TypeId, List<TypeId>>,
) {
    companion object {
        /**
         * Safe Factory.
         * Accepts raw collections and enforces order invariants (Sort, Dedupe, Freeze).
         * This is the ONLY way to create a ScanIndex instance.
         */
        fun of(
            rawScenarios: Collection<TypeId>,
            rawContracts: Map<TypeId, Collection<TypeId>>,
        ): ScanIndex {
            // 1. Scenarios: Dedupe + Sort + Freeze
            // TreeSet ensures Sort & Dedupe logic is consistent
            val safeScenarios =
                Collections.unmodifiableList(
                    ArrayList(TreeSet(rawScenarios)),
                )

            // 2. Contracts: Dedupe Keys/Values + Sort + Freeze
            // TreeMap ensures Key Sort
            val safeContracts = TreeMap<TypeId, List<TypeId>>()

            for ((key, values) in rawContracts) {
                // TreeSet ensures Value Sort & Dedupe
                val sortedValues =
                    Collections.unmodifiableList(
                        ArrayList(TreeSet(values)),
                    )
                safeContracts[key] = sortedValues
            }

            return ScanIndex(
                safeScenarios,
                Collections.unmodifiableSortedMap(safeContracts),
            )
        }
    }

    // --- Value Object Semantics (Manually implemented to prevent 'copy' backdoor) ---

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanIndex

        if (scenarios != other.scenarios) return false
        if (contracts != other.contracts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scenarios.hashCode()
        result = 31 * result + contracts.hashCode()
        return result
    }

    override fun toString(): String = "ScanIndex(scenarios=$scenarios, contracts=$contracts)"
}

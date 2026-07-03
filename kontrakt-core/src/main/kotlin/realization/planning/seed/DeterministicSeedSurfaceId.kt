package realization.planning.seed

import stage.canonicalization.contract.meaning.CanonicalTextLaw

/**
 * Lightweight identifier for a run-ratified DeterministicSeedSurface.
 *
 * TypeExpansionContext carries this id instead of carrying the full seed surface.
 * The full surface belongs to the run/session ratification state.
 */
class DeterministicSeedSurfaceId private constructor(
    val value: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeterministicSeedSurfaceId) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "DeterministicSeedSurfaceId(value=$value)"

    companion object {
        @JvmStatic
        fun issue(value: String): DeterministicSeedSurfaceId {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "DeterministicSeedSurfaceId.value",
                value = value,
            )

            return DeterministicSeedSurfaceId(value)
        }
    }
}

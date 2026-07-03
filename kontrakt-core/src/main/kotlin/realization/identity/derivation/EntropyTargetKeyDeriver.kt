package realization.identity.derivation

import stage.canonicalization.material.representation.TypeReference

/**
 * Deterministic entropy-target ordering key.
 */
interface EntropyTargetKeyProvider {
    fun deriveEntropyKey(
        name: String,
        type: TypeReference,
    ): Long
}

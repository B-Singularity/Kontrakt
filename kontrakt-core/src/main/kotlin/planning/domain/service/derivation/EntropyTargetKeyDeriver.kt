package planning.domain.service.derivation

import stage.canonicalization.material.TypeReference

/**
 * Deterministic entropy-target ordering key.
 */
interface EntropyTargetKeyProvider {
    fun deriveEntropyKey(
        name: String,
        type: TypeReference,
    ): Long
}

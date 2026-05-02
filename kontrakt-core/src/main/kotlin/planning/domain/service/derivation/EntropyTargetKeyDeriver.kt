package planning.domain.service.derivation

import metamodel.domain.vo.TypeReference

/**
 * Deterministic entropy-target ordering key.
 */
interface EntropyTargetKeyProvider {
    fun deriveEntropyKey(
        name: String,
        type: TypeReference,
    ): Long
}

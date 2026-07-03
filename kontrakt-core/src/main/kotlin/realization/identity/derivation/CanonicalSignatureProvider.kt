package realization.identity.derivation

import stage.canonicalization.material.representation.CanonicalSignature
import stage.input.presentation.dto.TypeFactsDTO

/**
 * Deterministic signature derivation bound to normalization/version rules.
 */
interface CanonicalSignatureProvider {
    fun deriveSignature(facts: TypeFactsDTO): CanonicalSignature
}

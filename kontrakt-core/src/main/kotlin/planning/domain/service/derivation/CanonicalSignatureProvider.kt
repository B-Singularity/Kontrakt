package planning.domain.service.derivation

import ir.identity.CanonicalSignature
import stage.input.material.TypeFactsDTO

/**
 * Deterministic signature derivation bound to normalization/version rules.
 */
interface CanonicalSignatureProvider {
    fun deriveSignature(facts: TypeFactsDTO): CanonicalSignature
}

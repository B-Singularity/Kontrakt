package planning.domain.service.derivation

import ir.identity.CanonicalSignature
import metamodel.domain.dto.TypeFactsDTO

/**
 * Deterministic signature derivation bound to normalization/version rules.
 */
interface CanonicalSignatureProvider {
    fun deriveSignature(facts: TypeFactsDTO): CanonicalSignature
}
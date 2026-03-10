package planning.domain.port.outgoing

import ir.identity.CanonicalSignature

import metamodel.domain.dto.MemberOrigin
import metamodel.domain.dto.TypeFactsDTO
import metamodel.domain.vo.TypeReference

/**
 * Outbound fact port.
 *
 * The core MUST consume normalized fact DTOs rather than raw reflection
 * or bytecode APIs.
 */
interface TypeFactsProvider {
    fun resolveFacts(reference: TypeReference): TypeFactsDTO
}

/**
 * Deterministic signature derivation bound to normalization/version rules.
 */
interface CanonicalSignatureProvider {
    fun deriveSignature(facts: TypeFactsDTO): CanonicalSignature
}

/**
 * Deterministic edge-key lowering bound to edgeOrderingVersion.
 */
interface CanonicalEdgeKeyProvider {
    fun deriveEdgeKey(name: String, origin: MemberOrigin): Long
}

/**
 * Deterministic entropy-target ordering key.
 */
interface EntropyTargetKeyProvider {
    fun deriveEntropyKey(name: String, type: TypeReference): Long
}

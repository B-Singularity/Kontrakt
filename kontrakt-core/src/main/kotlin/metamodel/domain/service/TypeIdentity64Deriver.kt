package metamodel.domain.service

import metamodel.domain.vo.TypeReference

/**
 * Canonical 64-bit type identity derivation service.
 *
 * This is a metamodel-domain service boundary, not a reflection-specific helper.
 *
 * Reflection, KSP, bytecode, or static-source adapters must not invent their own
 * identity hash algorithms. They must delegate to the same ratified identity law.
 *
 * The version is part of the protocol surface. If the algorithm changes, the
 * version must change so cache identity drift can be diagnosed and invalidated.
 */
interface TypeIdentity64Deriver {
    val identityAlgorithmId: String

    val identityAlgorithmVersion: Long

    fun deriveIdentity64(reference: TypeReference): Long
}

package metamodel.adapter.reflection

import metamodel.domain.vo.OrderedUseSiteAnnotations
import metamodel.domain.vo.TypeReference

/**
 * Public wiring surface used by the reflection adapter to request a
 * domain-issued TypeReference.
 *
 * This is public because ReflectionTypeReferenceFactory.issue(...) is public and
 * Kotlin forbids exposing internal parameter types from public functions.
 *
 * This is not:
 *
 * - a reflection implementation detail;
 * - a TypeReference implementation;
 * - a cache;
 * - a registry;
 * - or a persistence boundary.
 *
 * Responsibility:
 *
 * ReflectionTypeReferenceFactory renders reflection-specific KType material into
 * canonical text surfaces. It must not manually assemble TypeReference because
 * TypeReference is a final domain-issued VO.
 *
 * A concrete issuer implementation must perform the domain issuance pipeline:
 *
 * - CanonicalTypeText ratification;
 * - TypeShapeSummary classification;
 * - TypeShapeRatification;
 * - CanonicalTypeId issuance;
 * - TypeCycleKey issuance;
 * - CanonicalTypeSignature issuance;
 * - TypeIdentityCoherenceProof issuance;
 * - TypeReference.issue(...).
 *
 * Visibility law:
 *
 * The interface is public only as an adapter wiring contract.
 * Implementations may remain internal/private to the composition root.
 */
interface ReflectionTypeReferenceIssuer {
    fun issue(
        idText: String,
        cycleText: String,
        signatureText: String,
        useSiteAnnotations: OrderedUseSiteAnnotations,
        typeNestingDepth: Int,
    ): TypeReference
}
package adapter.reflection

import stage.canonicalization.material.CanonicalSignature
import stage.canonicalization.material.TypeReference

/**
 * Adapter-side bridge for the exact cycle-signature material required by
 * TypeCycleIdentity.
 *
 * Why this exists:
 *
 * The current codebase has two signature concepts:
 *
 * - stage.canonicalization.material.CanonicalTypeSignature
 * - stage.canonicalization.material.CanonicalSignature
 *
 * TypeReference carries the metamodel-domain signature material.
 * TypeCycleIdentity currently requires the IR-layer CanonicalSignature value.
 *
 * This provider makes the conversion explicit instead of hiding it inside
 * ReflectionTypeCycleIdentityProvider.
 *
 * This provider must not:
 *
 * - use KType;
 * - use KClass;
 * - access ReflectionTypeHandleRegistry;
 * - enumerate constructors;
 * - enumerate properties;
 * - inspect active members;
 * - order members;
 * - use wall clock;
 * - use randomness;
 * - use object identity;
 * - use classloader identity;
 * - use cache state as semantic authority.
 *
 * Current phase law:
 *
 * This is not the final canonical byte-encoding phase.
 *
 * However, even the temporary V1 bridge must be namespace-safe. Therefore the
 * default implementation must include a local schema/version/encoding prefix so
 * a later V2 canonical layout cannot collide with V1 raw UTF-8 cycle-key bytes.
 *
 * Required semantic source:
 *
 * ```text
 * TypeReference.cycleKey.value
 * ```
 *
 * not:
 *
 * ```text
 * TypeReference.signature.value
 * ```
 *
 * Rationale:
 *
 * Cycle identity must strip usage-site nullability while preserving the generic
 * type structure already captured during TypeReference issuance.
 */
interface ReflectionCycleSignatureProvider {
    fun deriveCycleSignature(
        reference: TypeReference,
    ): CanonicalSignature
}
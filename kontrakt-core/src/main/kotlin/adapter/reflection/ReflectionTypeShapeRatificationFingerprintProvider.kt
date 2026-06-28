package adapter.reflection

import stage.canonicalization.material.TypeShapeRatificationFingerprint
import stage.input.material.TypeShapeSummary

/**
 * Reflection-adapter provider for TypeShapeRatificationFingerprint material.
 *
 * This is adapter-side material production, not domain authority.
 *
 * The domain still validates the fingerprint through:
 *
 * - TypeShapeIdentityIssuancePolicy;
 * - TypeShapeRatificationVerifier;
 * - TypeShapeIdentityIssuer.
 *
 * This provider must produce the same fingerprint law expected by the active
 * TypeShapeRatificationVerifier.
 */
interface ReflectionTypeShapeRatificationFingerprintProvider {
    fun fingerprintFor(
        idText: String,
        shapeSummary: TypeShapeSummary,
        classifierId: String,
        classifierVersion: String,
    ): TypeShapeRatificationFingerprint
}
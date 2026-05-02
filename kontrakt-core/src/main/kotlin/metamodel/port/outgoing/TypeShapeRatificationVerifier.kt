package metamodel.domain.port.outgoing

import metamodel.domain.vo.CanonicalTypeText
import metamodel.domain.vo.FingerprintTokenEncoding
import metamodel.domain.vo.TypeShapeRatificationFingerprint
import metamodel.domain.vo.TypeShapeRatificationVerification
import metamodel.domain.vo.TypeShapeSummary

/**
 * Outbound port for verifying type-shape ratification fingerprints.
 *
 * Domain core defines what must be verified.
 * Infrastructure implements the cryptographic/digest primitive.
 *
 * This keeps the core free from:
 *
 * - MessageDigest;
 * - HMAC implementation details;
 * - BLAKE3/native libraries;
 * - reflection;
 * - KSP;
 * - adapter-specific classifier internals.
 *
 * The verifier must validate that ratificationFingerprint is mathematically
 * bound to the exact tuple:
 *
 * - text.value;
 * - shapeSummary protocol material;
 * - classifierId;
 * - classifierVersion;
 * - ratification law version.
 *
 * The verifier must not accept static dummy tokens.
 */
interface TypeShapeRatificationVerifier {
    val verifierId: String
    val verifierVersion: String

    /**
     * Expected fingerprint algorithm law.
     *
     * This lets TypeShapeIdentityIssuer block algorithm drift before relying on
     * the verifier result.
     */
    val expectedFingerprintAlgorithmId: String

    val expectedFingerprintAlgorithmVersion: String

    val expectedFingerprintValueEncoding: FingerprintTokenEncoding

    fun verify(
        text: CanonicalTypeText,
        shapeSummary: TypeShapeSummary,
        classifierId: String,
        classifierVersion: String,
        ratificationFingerprint: TypeShapeRatificationFingerprint,
    ): TypeShapeRatificationVerification
}

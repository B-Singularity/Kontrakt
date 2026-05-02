package metamodel.domain.port.outgoing

import metamodel.domain.vo.PolicyFingerprint

/**
 * Outbound port for deriving protocol-governed policy fingerprints.
 *
 * This is an adapter boundary.
 *
 * Domain code defines:
 *
 * - purpose token;
 * - algorithm id;
 * - algorithm law version;
 * - encoding id;
 * - ordered field material;
 * - length-prefix material law;
 * - golden-vector expectations.
 *
 * Adapter code provides:
 *
 * - the concrete digest primitive;
 * - implementation provenance;
 * - thread-safety discipline;
 * - golden-vector verification.
 *
 * Implementations may use java.security.MessageDigest, BLAKE3, native code, or
 * another primitive later, but they must produce the same PolicyFingerprint for
 * the same protocol material and must pass golden vectors.
 *
 * Reflection and KSP implementations must call this through the port. The
 * domain core must not import MessageDigest or any concrete digest library.
 */
interface PolicyFingerprintDeriver {
    val deriverId: String
    val deriverVersion: String

    /**
     * Derives a fingerprint from ordered, length-prefixed field material.
     *
     * The deriver must not reinterpret field order.
     * The deriver must not sort fields.
     * The deriver must not drop unknown fields silently.
     * The deriver must not use delimiter-concatenated material.
     */
    fun derive(
        purpose: String,
        algorithmId: String,
        algorithmVersion: String,
        encodingId: String,
        fields: List<Pair<String, String>>,
    ): PolicyFingerprint
}

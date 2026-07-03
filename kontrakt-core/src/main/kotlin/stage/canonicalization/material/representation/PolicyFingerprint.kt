package stage.canonicalization.material.representation

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.contract.representative.CanonicalTextPolicyFingerprintSpec

/**
 * Fixed-size governance fingerprint.
 *
 * This is not:
 *
 * - canonical type identity;
 * - a cache key;
 * - a hashCode cache;
 * - a canonical byte encoding;
 * - a reflection/KSP artifact;
 * - or an adapter implementation detail.
 *
 * It is a order-governed proof token that says:
 *
 *   "These policy fields were fingerprinted under this algorithm law."
 *
 * The actual digest primitive is implemented outside the domain core through
 * PolicyFingerprintDeriver.
 */
class PolicyFingerprint private constructor(
    val algorithmId: String,
    val algorithmVersion: String,
    val encodingId: String,
    val hex: String,
) {
    fun renderProtocolToken(): String = "$algorithmId@$algorithmVersion:$encodingId:$hex"

    fun requireCanonicalTextPolicySpec() {
        if (algorithmId != CanonicalTextPolicyFingerprintSpec.ALGORITHM_ID) {
            throw MetamodelFactContractViolationException(
                "PolicyFingerprint algorithm mismatch: " +
                        "expected=${CanonicalTextPolicyFingerprintSpec.ALGORITHM_ID}, actual=$algorithmId",
            )
        }

        if (algorithmVersion != CanonicalTextPolicyFingerprintSpec.FINGERPRINT_LAW_VERSION) {
            throw MetamodelFactContractViolationException(
                "PolicyFingerprint law version mismatch: " +
                        "expected=${CanonicalTextPolicyFingerprintSpec.FINGERPRINT_LAW_VERSION}, " +
                        "actual=$algorithmVersion",
            )
        }

        if (encodingId != CanonicalTextPolicyFingerprintSpec.ENCODING_ID) {
            throw MetamodelFactContractViolationException(
                "PolicyFingerprint encoding mismatch: " +
                        "expected=${CanonicalTextPolicyFingerprintSpec.ENCODING_ID}, actual=$encodingId",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PolicyFingerprint) return false

        return algorithmId == other.algorithmId &&
                algorithmVersion == other.algorithmVersion &&
                encodingId == other.encodingId &&
                hex == other.hex
    }

    override fun hashCode(): Int {
        var result = algorithmId.hashCode()
        result = 31 * result + algorithmVersion.hashCode()
        result = 31 * result + encodingId.hashCode()
        result = 31 * result + hex.hashCode()
        return result
    }

    override fun toString(): String = renderProtocolToken()

    companion object {
        @JvmStatic
        fun issue(
            algorithmId: String,
            algorithmVersion: String,
            encodingId: String,
            hex: String,
        ): PolicyFingerprint {
            requireToken("algorithmId", algorithmId)
            requireToken("algorithmVersion", algorithmVersion)
            requireToken("encodingId", encodingId)
            requireLowerHexSha256Like(hex)

            return PolicyFingerprint(
                algorithmId = algorithmId,
                algorithmVersion = algorithmVersion,
                encodingId = encodingId,
                hex = hex,
            )
        }

        private fun requireToken(
            field: String,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "$field must not be empty.",
                )
            }

            if (
                value.indexOf('\u0000') >= 0 ||
                value.indexOf('\n') >= 0 ||
                value.indexOf('\r') >= 0 ||
                value.indexOf('\t') >= 0
            ) {
                throw MetamodelFactContractViolationException(
                    "$field must not contain NUL, newline, carriage return, or tab.",
                )
            }
        }

        /**
         * SHA-256 rendered as lowercase hex is 64 ASCII hex characters.
         *
         * This check intentionally avoids Regex and Character.*. It is a small
         * ASCII-only guard.
         */
        private fun requireLowerHexSha256Like(hex: String) {
            if (hex.length != 64) {
                throw MetamodelFactContractViolationException(
                    "PolicyFingerprint.hex must be 64 lowercase hex characters for SHA-256: length=${hex.length}",
                )
            }

            for (index in hex.indices) {
                val c = hex[index]
                val ok = (c in '0'..'9') || (c in 'a'..'f')
                if (!ok) {
                    throw MetamodelFactContractViolationException(
                        "PolicyFingerprint.hex must be lowercase ASCII hex at index=$index.",
                    )
                }
            }
        }
    }
}

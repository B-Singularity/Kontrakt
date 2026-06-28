package metamodel.domain.policy

import metamodel.domain.vo.FingerprintTokenEncoding
import metamodel.domain.vo.TypeShapeRatificationFingerprint
import stage.input.boundary.TypeShapeRatificationVerifier
import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Pinned policy for issuing CanonicalTypeId.
 *
 * This is the active law of one metamodel identity issuance scope.
 *
 * It prevents TypeShapeIdentityIssuer from blindly accepting caller-supplied:
 *
 * - classifierId;
 * - classifierVersion;
 * - ratification fingerprint algorithm;
 * - fingerprint value encoding.
 *
 * This policy is not adaptive.
 * It must be created at a stable resolver/session/policy boundary and then
 * reused immutably for all identities issued in that scope.
 *
 * Value-object law:
 *
 * This object uses structural equality. Reference equality is not acceptable
 * because policy drift checks, policy caches, and deterministic scope guards may
 * compare policy objects issued at different times.
 *
 * Identifier canonicalization law:
 *
 * activeClassifierId is canonicalized to lowercase ASCII order form at issue
 * time. requireAllowsClassifier(...) also canonicalizes incoming classifierId
 * before comparison.
 *
 * This must match TypeShapeRatification's classifierId canonicalization law.
 *
 * Algorithm-id law:
 *
 * expectedRatificationAlgorithmId is canonicalized to lowercase ASCII order
 * form at issue time. This must match TypeShapeRatificationFingerprint's
 * algorithm-id canonicalization law.
 *
 * Version law:
 *
 * activeClassifierVersion, policyVersion, and
 * expectedRatificationAlgorithmVersion are not silently normalized. They are
 * order law versions and must be supplied exactly.
 *
 * Resource law:
 *
 * Policy tokens are capped in length to prevent configuration/adapter mistakes
 * from creating large resident strings in long-lived policy snapshots.
 *
 * Diagnostic law:
 *
 * toString() and renderSummary() expose only policy metadata and expected law
 * identifiers. They do not expose fingerprint values because this object does
 * not own fingerprint values.
 */
class TypeShapeIdentityIssuancePolicy private constructor(
    val policyId: String,
    val policyVersion: String,
    val activeClassifierId: String,
    val activeClassifierVersion: String,
    val expectedRatificationAlgorithmId: String,
    val expectedRatificationAlgorithmVersion: String,
    val expectedFingerprintEncoding: FingerprintTokenEncoding,
) {
    fun requireAllowsClassifier(
        classifierId: String,
        classifierVersion: String,
    ) {
        val canonicalClassifierId =
            canonicalizeProtocolId(
                field = "classifierId",
                value = classifierId,
            )

        requireProtocolToken(
            field = "classifierVersion",
            value = classifierVersion,
        )

        if (
            canonicalClassifierId != activeClassifierId ||
            classifierVersion != activeClassifierVersion
        ) {
            throw MetamodelFactContractViolationException(
                "TypeShapeIdentityIssuancePolicy rejected classifier law: " +
                        "expected=$activeClassifierId@$activeClassifierVersion, " +
                        "actual=$canonicalClassifierId@$classifierVersion",
            )
        }
    }

    fun requireAllowsFingerprint(ratificationFingerprint: TypeShapeRatificationFingerprint) {
        ratificationFingerprint.requireAlgorithm(
            expectedAlgorithmId = expectedRatificationAlgorithmId,
            expectedAlgorithmVersion = expectedRatificationAlgorithmVersion,
        )

        if (ratificationFingerprint.valueEncoding != expectedFingerprintEncoding) {
            throw MetamodelFactContractViolationException(
                "TypeShapeIdentityIssuancePolicy rejected fingerprint encoding: " +
                        "expected=${expectedFingerprintEncoding.protocolToken}, " +
                        "actual=${ratificationFingerprint.valueEncoding.protocolToken}",
            )
        }
    }

    /**
     * Verifier compatibility guard.
     *
     * The verifier is the adapter-side authority that checks the mathematical
     * binding of the fingerprint. The issuer policy is the domain-side authority
     * that declares which law is currently active. Both must agree.
     */
    fun requireCompatibleVerifier(verifier: TypeShapeRatificationVerifier) {
        val verifierAlgorithmId =
            canonicalizeAlgorithmId(
                field = "verifier.expectedFingerprintAlgorithmId",
                value = verifier.expectedFingerprintAlgorithmId,
            )

        requireProtocolToken(
            field = "verifier.expectedFingerprintAlgorithmVersion",
            value = verifier.expectedFingerprintAlgorithmVersion,
        )

        if (
            verifierAlgorithmId != expectedRatificationAlgorithmId ||
            verifier.expectedFingerprintAlgorithmVersion != expectedRatificationAlgorithmVersion
        ) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatificationVerifier algorithm law does not match active issuance policy: " +
                        "policy=$expectedRatificationAlgorithmId@$expectedRatificationAlgorithmVersion, " +
                        "verifier=$verifierAlgorithmId@${verifier.expectedFingerprintAlgorithmVersion}",
            )
        }

        if (verifier.expectedFingerprintValueEncoding != expectedFingerprintEncoding) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatificationVerifier fingerprint encoding does not match active issuance policy: " +
                        "policy=${expectedFingerprintEncoding.protocolToken}, " +
                        "verifier=${verifier.expectedFingerprintValueEncoding.protocolToken}",
            )
        }
    }

    fun renderSummary(): String =
        "TypeShapeIdentityIssuancePolicy(" +
                "policy=$policyId@$policyVersion, " +
                "classifier=$activeClassifierId@$activeClassifierVersion, " +
                "fingerprintLaw=$expectedRatificationAlgorithmId@$expectedRatificationAlgorithmVersion, " +
                "encoding=${expectedFingerprintEncoding.protocolToken}" +
                ")"

    override fun toString(): String = renderSummary()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeIdentityIssuancePolicy) return false

        return policyId == other.policyId &&
                policyVersion == other.policyVersion &&
                activeClassifierId == other.activeClassifierId &&
                activeClassifierVersion == other.activeClassifierVersion &&
                expectedRatificationAlgorithmId == other.expectedRatificationAlgorithmId &&
                expectedRatificationAlgorithmVersion == other.expectedRatificationAlgorithmVersion &&
                expectedFingerprintEncoding == other.expectedFingerprintEncoding
    }

    override fun hashCode(): Int {
        var result = policyId.hashCode()
        result = 31 * result + policyVersion.hashCode()
        result = 31 * result + activeClassifierId.hashCode()
        result = 31 * result + activeClassifierVersion.hashCode()
        result = 31 * result + expectedRatificationAlgorithmId.hashCode()
        result = 31 * result + expectedRatificationAlgorithmVersion.hashCode()
        result = 31 * result + expectedFingerprintEncoding.protocolOrder
        return result
    }

    companion object {
        private const val MAX_POLICY_TOKEN_CHARS: Int = 192

        @JvmStatic
        fun issue(
            policyId: String,
            policyVersion: String,
            activeClassifierId: String,
            activeClassifierVersion: String,
            expectedRatificationAlgorithmId: String,
            expectedRatificationAlgorithmVersion: String,
            expectedFingerprintEncoding: FingerprintTokenEncoding,
        ): TypeShapeIdentityIssuancePolicy {
            requireProtocolToken("policyId", policyId)
            requireProtocolToken("policyVersion", policyVersion)
            requireProtocolToken("activeClassifierVersion", activeClassifierVersion)
            requireProtocolToken(
                field = "expectedRatificationAlgorithmVersion",
                value = expectedRatificationAlgorithmVersion,
            )

            val canonicalClassifierId =
                canonicalizeProtocolId(
                    field = "activeClassifierId",
                    value = activeClassifierId,
                )

            val canonicalAlgorithmId =
                canonicalizeAlgorithmId(
                    field = "expectedRatificationAlgorithmId",
                    value = expectedRatificationAlgorithmId,
                )

            return TypeShapeIdentityIssuancePolicy(
                policyId = policyId,
                policyVersion = policyVersion,
                activeClassifierId = canonicalClassifierId,
                activeClassifierVersion = activeClassifierVersion,
                expectedRatificationAlgorithmId = canonicalAlgorithmId,
                expectedRatificationAlgorithmVersion = expectedRatificationAlgorithmVersion,
                expectedFingerprintEncoding = expectedFingerprintEncoding,
            )
        }

        /**
         * Canonicalizes classifier-like order ids.
         *
         * This must stay aligned with TypeShapeRatification's classifierId and
         * verifierId canonicalization law.
         */
        private fun canonicalizeProtocolId(
            field: String,
            value: String,
        ): String {
            requireProtocolToken(
                field = field,
                value = value,
            )

            var requiresLowercaseCopy = false

            for (index in value.indices) {
                val c = value[index]

                when (c) {
                    in 'A'..'Z' -> {
                        requiresLowercaseCopy = true
                    }

                    in 'a'..'z',
                    in '0'..'9',
                    '-',
                    '_',
                    '.',
                        -> {
                        // Already canonical-safe ASCII order material.
                    }

                    else -> {
                        throw MetamodelFactContractViolationException(
                            "$field contains a non-canonical order-id character.",
                        )
                    }
                }
            }

            if (!requiresLowercaseCopy) {
                return value
            }

            val builder = StringBuilder(value.length)

            for (index in value.indices) {
                val c = value[index]
                val lowered =
                    if (c in 'A'..'Z') {
                        (c.code + 32).toChar()
                    } else {
                        c
                    }

                builder.append(lowered)
            }

            return builder.toString()
        }

        /**
         * Canonicalizes fingerprint algorithm ids.
         *
         * Kept separate from canonicalizeProtocolId for semantic clarity. The
         * current allowed character surface is intentionally the same.
         */
        private fun canonicalizeAlgorithmId(
            field: String,
            value: String,
        ): String {
            requireProtocolToken(
                field = field,
                value = value,
            )

            var requiresLowercaseCopy = false

            for (index in value.indices) {
                val c = value[index]

                when (c) {
                    in 'A'..'Z' -> {
                        requiresLowercaseCopy = true
                    }

                    in 'a'..'z',
                    in '0'..'9',
                    '-',
                    '_',
                    '.',
                        -> {
                        // Already canonical-safe ASCII algorithm material.
                    }

                    else -> {
                        throw MetamodelFactContractViolationException(
                            "$field contains a non-canonical algorithm-id character.",
                        )
                    }
                }
            }

            if (!requiresLowercaseCopy) {
                return value
            }

            val builder = StringBuilder(value.length)

            for (index in value.indices) {
                val c = value[index]
                val lowered =
                    if (c in 'A'..'Z') {
                        (c.code + 32).toChar()
                    } else {
                        c
                    }

                builder.append(lowered)
            }

            return builder.toString()
        }

        private fun requireProtocolToken(
            field: String,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "$field must not be empty.",
                )
            }

            if (value.length > MAX_POLICY_TOKEN_CHARS) {
                throw MetamodelFactContractViolationException(
                    "$field exceeds maximum allowed policy token length.",
                )
            }

            if (
                value.indexOf('|') >= 0 ||
                value.indexOf('\u0000') >= 0 ||
                value.indexOf('\n') >= 0 ||
                value.indexOf('\r') >= 0 ||
                value.indexOf('\t') >= 0
            ) {
                throw MetamodelFactContractViolationException(
                    "$field contains a reserved order/control character.",
                )
            }
        }
    }
}

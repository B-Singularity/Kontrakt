package realization.identity

import stage.canonicalization.contract.TypeShapeIdentityIssuancePolicy
import stage.canonicalization.material.CanonicalTypeId
import stage.canonicalization.material.CanonicalTypeText
import stage.canonicalization.material.TypeShapeRatification
import stage.canonicalization.material.TypeShapeRatificationFingerprint
import stage.input.boundary.TypeShapeRatificationVerifier
import stage.input.material.TypeShapeSummary

/**
 * Domain service that issues CanonicalTypeId through the complete ratification
 * and coherence path.
 *
 * Preferred flow:
 *
 *     Active TypeShapeIdentityIssuancePolicy
 *     -> classifier law check
 *     -> fingerprint law check
 *     -> verifier compatibility check
 *     -> TypeShapeRatificationVerifier
 *     -> TypeShapeRatification
 *     -> TypeShapeCoherenceScope
 *     -> TypeShapeCoherenceReceipt
 *     -> CanonicalTypeId
 *
 * This service prevents:
 *
 * - raw-string identity issuance;
 * - phantom ratification;
 * - same-text/different-shape collapse;
 * - classifier version drift;
 * - fingerprint algorithm drift;
 * - receipt/scope mismatch;
 * - token-only trust.
 */
class TypeShapeIdentityIssuer(
    private val issuancePolicy: TypeShapeIdentityIssuancePolicy,
    private val verifier: TypeShapeRatificationVerifier,
    private val coherenceScope: TypeShapeCoherenceScope,
) {
    fun issue(
        text: CanonicalTypeText,
        shapeSummary: TypeShapeSummary,
        classifierId: String,
        classifierVersion: String,
        ratificationFingerprint: TypeShapeRatificationFingerprint,
    ): CanonicalTypeId {
        issuancePolicy.requireAllowsClassifier(
            classifierId = classifierId,
            classifierVersion = classifierVersion,
        )

        issuancePolicy.requireAllowsFingerprint(
            ratificationFingerprint = ratificationFingerprint,
        )

        issuancePolicy.requireCompatibleVerifier(
            verifier = verifier,
        )

        val ratification =
            TypeShapeRatification.issueVerified(
                text = text,
                shapeSummary = shapeSummary,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
                ratificationFingerprint = ratificationFingerprint,
                verifier = verifier,
            )

        val receipt = coherenceScope.registerOrVerify(ratification)

        receipt.requireIssuedBy(coherenceScope)
        receipt.requireAccepts(ratification)

        return CanonicalTypeId.issueVerified(
            ratification = ratification,
            coherenceReceipt = receipt,
        )
    }
}

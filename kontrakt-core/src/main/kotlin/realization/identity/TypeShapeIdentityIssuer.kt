package realization.identity

import migration.quarantine.TypeShapeRatificationVerifier
import stage.canonicalization.contract.meaning.TypeShapeIdentityIssuancePolicy
import stage.canonicalization.material.representation.CanonicalTypeId
import stage.canonicalization.material.representation.CanonicalTypeText
import stage.canonicalization.material.representation.TypeShapeRatification
import stage.canonicalization.material.representation.TypeShapeRatificationFingerprint
import stage.input.presentation.raw.TypeShapeSummary

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

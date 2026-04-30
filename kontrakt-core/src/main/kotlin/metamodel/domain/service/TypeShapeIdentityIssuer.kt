package metamodel.domain.service

import metamodel.domain.policy.TypeShapeIdentityIssuancePolicy
import metamodel.domain.port.outgoing.TypeShapeRatificationVerifier
import metamodel.domain.vo.CanonicalTypeId
import metamodel.domain.vo.CanonicalTypeText
import metamodel.domain.vo.TypeShapeRatification
import metamodel.domain.vo.TypeShapeRatificationFingerprint
import metamodel.domain.vo.TypeShapeSummary

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

        val ratification = TypeShapeRatification.issueVerified(
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
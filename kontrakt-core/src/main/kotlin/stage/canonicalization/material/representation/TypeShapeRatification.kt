package stage.canonicalization.material.representation

import migration.quarantine.TypeShapeRatificationVerifier
import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.input.presentation.raw.TypeShapeSummary

/**
 * Verified proof that one CanonicalTypeText has been classified into one
 * TypeShapeSummary under a pinned classifier law.
 *
 * This is not a casual DTO. It is the entry-boundary proof that allows the
 * metamodel domain to issue CanonicalTypeId.
 *
 * Phantom-ratification defense:
 *
 * A ratification object cannot be issued from a naked string token. It must pass
 * TypeShapeRatificationVerifier. The verifier is an outbound port so digest/HMAC
 * implementation details stay outside the domain core.
 *
 * Value-object law:
 *
 * This object uses structural equality. Reference equality is not acceptable
 * because two independently issued ratifications with the same semantic proof
 * and the same verifier provenance must behave as the same value.
 *
 * Provenance law:
 *
 * verifierId and verifierVersion are part of this proof value. Two
 * ratifications verified by different verifier laws are not the same proof,
 * even if their text, shape, classifier, and fingerprint match.
 */
class TypeShapeRatification private constructor(
    val text: CanonicalTypeText,
    val shapeSummary: TypeShapeSummary,
    val classifierId: String,
    val classifierVersion: String,
    val ratificationFingerprint: TypeShapeRatificationFingerprint,
    val verifierId: String,
    val verifierVersion: String,
) {
    /**
     * Requires the other ratification to be the same verified proof.
     *
     * This is intentionally stronger than "same semantic payload". It also
     * checks verifier provenance because the verifier law is part of the proof
     * boundary.
     */
    fun requireSameContentAs(other: TypeShapeRatification) {
        if (text != other.text) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatification text mismatch: " +
                        "expected=${text.value}, actual=${other.text.value}",
            )
        }

        if (shapeSummary != other.shapeSummary) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatification shape mismatch: " +
                        "expected=$shapeSummary, actual=${other.shapeSummary}",
            )
        }

        if (classifierId != other.classifierId || classifierVersion != other.classifierVersion) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatification classifier mismatch: " +
                        "expected=$classifierId@$classifierVersion, " +
                        "actual=${other.classifierId}@${other.classifierVersion}",
            )
        }

        if (ratificationFingerprint != other.ratificationFingerprint) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatification fingerprint mismatch for text=${text.value}. " +
                        "Same content under the same classifier law must produce the same fingerprint.",
            )
        }

        if (verifierId != other.verifierId || verifierVersion != other.verifierVersion) {
            throw MetamodelFactContractViolationException(
                "TypeShapeRatification verifier provenance mismatch: " +
                        "expected=$verifierId@$verifierVersion, " +
                        "actual=${other.verifierId}@${other.verifierVersion}",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeRatification) return false

        return text == other.text &&
                shapeSummary == other.shapeSummary &&
                classifierId == other.classifierId &&
                classifierVersion == other.classifierVersion &&
                ratificationFingerprint == other.ratificationFingerprint &&
                verifierId == other.verifierId &&
                verifierVersion == other.verifierVersion
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + shapeSummary.hashCode()
        result = 31 * result + classifierId.hashCode()
        result = 31 * result + classifierVersion.hashCode()
        result = 31 * result + ratificationFingerprint.hashCode()
        result = 31 * result + verifierId.hashCode()
        result = 31 * result + verifierVersion.hashCode()
        return result
    }

    override fun toString(): String =
        "TypeShapeRatification(" +
                "text=${text.value}, " +
                "shapeSummary=$shapeSummary, " +
                "classifier=$classifierId@$classifierVersion, " +
                "fingerprint=${ratificationFingerprint.redacted()}, " +
                "verifier=$verifierId@$verifierVersion" +
                ")"

    companion object {
        @JvmStatic
        fun issueVerified(
            text: CanonicalTypeText,
            shapeSummary: TypeShapeSummary,
            classifierId: String,
            classifierVersion: String,
            ratificationFingerprint: TypeShapeRatificationFingerprint,
            verifier: TypeShapeRatificationVerifier,
        ): TypeShapeRatification {
            requireProtocolComponent("classifierId", classifierId)
            requireProtocolComponent("classifierVersion", classifierVersion)
            requireProtocolComponent("verifierId", verifier.verifierId)
            requireProtocolComponent("verifierVersion", verifier.verifierVersion)

            val verification =
                verifier.verify(
                    text = text,
                    shapeSummary = shapeSummary,
                    classifierId = classifierId,
                    classifierVersion = classifierVersion,
                    ratificationFingerprint = ratificationFingerprint,
                )

            when (verification) {
                TypeShapeRatificationVerification.Accepted -> Unit

                is TypeShapeRatificationVerification.Rejected -> {
                    throw MetamodelFactContractViolationException(
                        "TypeShapeRatification verification failed: " +
                                "text=${text.value}, " +
                                "shapeSummary=$shapeSummary, " +
                                "classifier=$classifierId@$classifierVersion, " +
                                "fingerprint=${ratificationFingerprint.redacted()}, " +
                                "reason=${verification.reason}",
                    )
                }
            }

            return TypeShapeRatification(
                text = text,
                shapeSummary = shapeSummary,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
                ratificationFingerprint = ratificationFingerprint,
                verifierId = verifier.verifierId,
                verifierVersion = verifier.verifierVersion,
            )
        }

        private fun requireProtocolComponent(
            field: String,
            value: String,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "$field must not be empty.",
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

/**
 * Verification result for type-shape ratification.
 */
sealed interface TypeShapeRatificationVerification {
    data object Accepted : TypeShapeRatificationVerification

    class Rejected(
        val reason: String,
    ) : TypeShapeRatificationVerification {
        init {
            if (reason.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeRatificationVerification.Rejected.reason must not be empty.",
                )
            }

            if (
                reason.indexOf('\u0000') >= 0 ||
                reason.indexOf('\n') >= 0 ||
                reason.indexOf('\r') >= 0 ||
                reason.indexOf('\t') >= 0
            ) {
                throw MetamodelFactContractViolationException(
                    "TypeShapeRatificationVerification.Rejected.reason must not contain control characters.",
                )
            }
        }
    }
}

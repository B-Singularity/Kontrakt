package versioning.coordinate.contract

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Canonical version tuple carried by canonical signatures and canonical byte
 * material.
 *
 * This value is a value object.
 *
 * It must compare by value, not by reference, because cache, replay, and
 * interning boundaries use this tuple to decide whether canonical material is
 * comparable under the same order.
 */
class CanonicalVersionTuple private constructor(
    val canonicalEncodingVersion: String,
    val normalizationVersion: String,
    val signatureSchemaVersion: String,
    val typeIdentityAlgorithmVersion: String,
    val edgeOrderingVersion: String,
    val hashDerivationVersion: String,
) {
    fun sameProtocolAs(other: CanonicalVersionTuple): Boolean = this == other

    fun renderForDiagnostics(): String = toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CanonicalVersionTuple) {
            return false
        }

        return canonicalEncodingVersion == other.canonicalEncodingVersion &&
                normalizationVersion == other.normalizationVersion &&
                signatureSchemaVersion == other.signatureSchemaVersion &&
                typeIdentityAlgorithmVersion == other.typeIdentityAlgorithmVersion &&
                edgeOrderingVersion == other.edgeOrderingVersion &&
                hashDerivationVersion == other.hashDerivationVersion
    }

    override fun hashCode(): Int {
        var result = canonicalEncodingVersion.hashCode()
        result = 31 * result + normalizationVersion.hashCode()
        result = 31 * result + signatureSchemaVersion.hashCode()
        result = 31 * result + typeIdentityAlgorithmVersion.hashCode()
        result = 31 * result + edgeOrderingVersion.hashCode()
        result = 31 * result + hashDerivationVersion.hashCode()
        return result
    }

    override fun toString(): String =
        buildString {
            append("CanonicalVersionTuple(")
            append("encoding=")
            append(canonicalEncodingVersion)
            append(", normalization=")
            append(normalizationVersion)
            append(", signatureSchema=")
            append(signatureSchemaVersion)
            append(", typeIdentity=")
            append(typeIdentityAlgorithmVersion)
            append(", edgeOrdering=")
            append(edgeOrderingVersion)
            append(", hashDerivation=")
            append(hashDerivationVersion)
            append(')')
        }

    companion object {
        @JvmStatic
        fun issue(
            canonicalEncodingVersion: String,
            normalizationVersion: String,
            signatureSchemaVersion: String,
            typeIdentityAlgorithmVersion: String,
            edgeOrderingVersion: String,
            hashDerivationVersion: String,
        ): CanonicalVersionTuple {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "canonicalEncodingVersion",
                value = canonicalEncodingVersion,
            )
            CanonicalTextLaw.validateCanonicalComponent(
                field = "normalizationVersion",
                value = normalizationVersion,
            )
            CanonicalTextLaw.validateCanonicalComponent(
                field = "signatureSchemaVersion",
                value = signatureSchemaVersion,
            )
            CanonicalTextLaw.validateCanonicalComponent(
                field = "typeIdentityAlgorithmVersion",
                value = typeIdentityAlgorithmVersion,
            )
            CanonicalTextLaw.validateCanonicalComponent(
                field = "edgeOrderingVersion",
                value = edgeOrderingVersion,
            )
            CanonicalTextLaw.validateCanonicalComponent(
                field = "hashDerivationVersion",
                value = hashDerivationVersion,
            )

            return CanonicalVersionTuple(
                canonicalEncodingVersion = canonicalEncodingVersion,
                normalizationVersion = normalizationVersion,
                signatureSchemaVersion = signatureSchemaVersion,
                typeIdentityAlgorithmVersion = typeIdentityAlgorithmVersion,
                edgeOrderingVersion = edgeOrderingVersion,
                hashDerivationVersion = hashDerivationVersion,
            )
        }
    }
}
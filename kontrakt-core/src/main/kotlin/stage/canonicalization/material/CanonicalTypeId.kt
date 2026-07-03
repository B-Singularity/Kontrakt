package stage.canonicalization.material

import realization.identity.TypeShapeCoherenceReceipt
import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.input.presentation.raw.TypeShapeSummary

/**
 * Canonical type identity issued by the metamodel identity boundary.
 *
 * This is not:
 *
 * - a JVM class name;
 * - a JVM descriptor;
 * - a reflection handle;
 * - a KSP symbol;
 * - an adapter binary name;
 * - a runtime handle registry key;
 * - or a string-only identity.
 *
 * Identity law:
 *
 * CanonicalTypeId equality is not text-only.
 *
 * It uses ratified identity equality:
 *
 * - canonical text;
 * - shape summary;
 * - classifier id;
 * - classifier version;
 * - ratification fingerprint.
 *
 * This prevents accidental ATOMIC / COLLECTION / COMPOSITE conflation before a
 * dedicated metamodel interner or singleton identity authority exists.
 *
 * Receipt law:
 *
 * TypeShapeCoherenceReceipt does not own semantic facts. It only proves that
 * the supplied TypeShapeRatification was admitted by a coherence scope.
 *
 * Hashing note:
 *
 * hashCode is intentionally not precomputed in this version. If profiling later
 * proves CanonicalTypeId is a hot map key, a constructor-time precomputedHash
 * may be introduced without changing equality semantics.
 */
class CanonicalTypeId private constructor(
    val text: CanonicalTypeText,
    val shapeSummary: TypeShapeSummary,
    val classifierId: String,
    val classifierVersion: String,
    val ratificationFingerprint: TypeShapeRatificationFingerprint,
) {
    val value: String
        get() = text.value

    /**
     * Explicit text-only comparison for diagnostics and coherence checks.
     *
     * Do not use this as object equality.
     */
    fun hasSameCanonicalTextAs(other: CanonicalTypeId): Boolean = text.value == other.text.value

    /**
     * Guard for persisted/cached/replayed identity material.
     */
    fun requireClassifier(
        expectedClassifierId: String,
        expectedClassifierVersion: String,
    ) {
        if (classifierId != expectedClassifierId || classifierVersion != expectedClassifierVersion) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeId classifier mismatch: " +
                        "expected=$expectedClassifierId@$expectedClassifierVersion, " +
                        "actual=$classifierId@$classifierVersion, value=$value",
            )
        }
    }

    /**
     * Guard for algorithm-law drift.
     *
     * classifierVersion should normally include the classifier's semantic law,
     * but fingerprint algorithm law is still checked independently because
     * digest/HMAC order changes may evolve separately from classifier logic.
     */
    fun requireRatificationAlgorithm(
        expectedAlgorithmId: String,
        expectedAlgorithmVersion: String,
    ) {
        if (
            ratificationFingerprint.algorithmId != expectedAlgorithmId ||
            ratificationFingerprint.algorithmVersion != expectedAlgorithmVersion
        ) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeId ratification algorithm mismatch: " +
                        "expected=$expectedAlgorithmId@$expectedAlgorithmVersion, " +
                        "actual=${ratificationFingerprint.algorithmId}@${ratificationFingerprint.algorithmVersion}, " +
                        "value=$value",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanonicalTypeId) return false

        return text == other.text &&
                shapeSummary == other.shapeSummary &&
                classifierId == other.classifierId &&
                classifierVersion == other.classifierVersion &&
                ratificationFingerprint == other.ratificationFingerprint
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + shapeSummary.hashCode()
        result = 31 * result + classifierId.hashCode()
        result = 31 * result + classifierVersion.hashCode()
        result = 31 * result + ratificationFingerprint.hashCode()
        return result
    }

    override fun toString(): String = value

    companion object {
        /**
         * Issues an id from verified ratification and scope admission receipt.
         *
         * The semantic fact source is TypeShapeRatification.
         * The receipt only proves coherence-scope admission.
         */
        @JvmStatic
        fun issueVerified(
            ratification: TypeShapeRatification,
            coherenceReceipt: TypeShapeCoherenceReceipt,
        ): CanonicalTypeId {
            coherenceReceipt.requireAccepts(ratification)

            return CanonicalTypeId(
                text = ratification.text,
                shapeSummary = ratification.shapeSummary,
                classifierId = ratification.classifierId,
                classifierVersion = ratification.classifierVersion,
                ratificationFingerprint = ratification.ratificationFingerprint,
            )
        }
    }
}

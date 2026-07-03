package stage.canonicalization.material.frozen.record

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.material.TypeReference
import stage.normalization.contract.MetamodelProtocolTextGuards
import stage.normalization.material.AnnotationQualifiedName

/**
 * Backend-neutral annotation identity key.
 *
 * This key identifies one frozen annotation record inside a deterministic
 * frozen annotation sequence.
 *
 * It is intentionally a Level 1 transitional key shape.
 *
 * Long-term target:
 *
 * ```text
 * annotationType
 * annotationQualifiedName
 * FrozenAnnotationUseSiteTarget
 * FrozenAnnotationPayloadKey
 * ```
 *
 * Current transitional shape:
 *
 * ```text
 * annotationType
 * annotationQualifiedName
 * useSiteTarget: String
 * canonicalPayloadKey: String
 * ```
 *
 * Transitional string law:
 *
 * [useSiteTarget] and [canonicalPayloadKey] are still strings, but they are not
 * arbitrary strings.
 *
 * They must be ASCII order-id tokens:
 *
 * - non-empty;
 * - length-bounded;
 * - composed only of `A-Z`, `a-z`, `0-9`, `-`, `_`, `.`;
 * - free of whitespace;
 * - free of pipe delimiter;
 * - free of NUL;
 * - free of C0/C1 control characters;
 * - free of backend-native object identifiers;
 * - free of local filesystem paths;
 * - free of classloader identities;
 * - free of registry ids;
 * - free of discovery append ordinals.
 *
 * Reason:
 *
 * These fields participate in frozen annotation identity. Even while they are
 * transitional strings, they must be clean order material so V5 declarative
 * metadata, JSON export, diagnostics, and future canonical payload encoding do
 * not inherit polluted text.
 *
 * Identity law:
 *
 * The key is structural over:
 *
 * - annotationType;
 * - annotationQualifiedName;
 * - useSiteTarget;
 * - canonicalPayloadKey.
 *
 * Availability is intentionally not part of this key.
 *
 * Repeatable annotation law:
 *
 * Repeatable annotation semantics are not encoded by backend enumeration order.
 *
 * If repeatable annotations are later ratified, their disambiguation must be
 * derived from canonical payload ordering or an explicitly ratified repeatable
 * ordinal law, not from reflection/KSP/source enumeration order.
 *
 * Equality law:
 *
 * Equality is structural and is required for deterministic sequence duplicate
 * detection.
 *
 * Hash law:
 *
 * hashCode is a transitional in-memory equality-collection companion.
 *
 * It currently composes existing metamodel VO/string hashCode surfaces and must
 * not become:
 *
 * - canonical fingerprint;
 * - persistent frozen-image identity;
 * - route key;
 * - L1/L2 partition key;
 * - PlanCacheKey material;
 * - cross-runtime order digest.
 *
 * The later BLAKE3 / metadata-hash refactoring may replace this hashCode
 * strategy globally. Do not introduce a local hash family in this key.
 */
class FrozenAnnotationRecordKey private constructor(
    val annotationType: TypeReference,
    val annotationQualifiedName: AnnotationQualifiedName,
    val useSiteTarget: String,
    val canonicalPayloadKey: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenAnnotationRecordKey(" +
                "annotationType=${annotationType.renderSummary()}, " +
                "annotationQualifiedName=$annotationQualifiedName, " +
                "useSiteTarget=$useSiteTarget, " +
                "canonicalPayloadKey=$canonicalPayloadKey" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenAnnotationRecordKey) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return annotationType == other.annotationType &&
                annotationQualifiedName == other.annotationQualifiedName &&
                useSiteTarget == other.useSiteTarget &&
                canonicalPayloadKey == other.canonicalPayloadKey
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        fun issue(
            annotationType: TypeReference,
            annotationQualifiedName: AnnotationQualifiedName,
            useSiteTarget: String,
            canonicalPayloadKey: String,
        ): FrozenAnnotationRecordKey {
            requireTransitionalProtocolIdKeyMaterial(
                field = "FrozenAnnotationRecordKey.useSiteTarget",
                value = useSiteTarget,
                maxChars = MAX_USE_SITE_TARGET_CHARS,
            )

            requireTransitionalProtocolIdKeyMaterial(
                field = "FrozenAnnotationRecordKey.canonicalPayloadKey",
                value = canonicalPayloadKey,
                maxChars = MAX_CANONICAL_PAYLOAD_KEY_CHARS,
            )

            return FrozenAnnotationRecordKey(
                annotationType = annotationType,
                annotationQualifiedName = annotationQualifiedName,
                useSiteTarget = useSiteTarget,
                canonicalPayloadKey = canonicalPayloadKey,
                precomputedHashCode = computeHashCode(
                    annotationType = annotationType,
                    annotationQualifiedName = annotationQualifiedName,
                    useSiteTarget = useSiteTarget,
                    canonicalPayloadKey = canonicalPayloadKey,
                ),
            )
        }

        /**
         * Validates transitional string key material through the shared
         * metamodel order-token authority.
         *
         * This helper exists only to add field-specific limits and commentary.
         * It must not weaken MetamodelProtocolTextGuards.
         *
         * It deliberately throws MetamodelFactContractViolationException,
         * directly or through MetamodelProtocolTextGuards.
         *
         * Do not replace this with IllegalArgumentException.
         */
        private fun requireTransitionalProtocolIdKeyMaterial(
            field: String,
            value: String,
            maxChars: Int,
        ) {
            if (field.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "FrozenAnnotationRecordKey validation field name must not be empty.",
                )
            }

            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = field,
                value = value,
                maxChars = maxChars,
            )
        }

        /**
         * Computes the transitional JVM hashCode companion.
         *
         * This deliberately follows the current metamodel VO family until the
         * later BLAKE3 / metadata-hash refactoring replaces hash policy
         * globally.
         *
         * Do not treat this value as order material.
         */
        private fun computeHashCode(
            annotationType: TypeReference,
            annotationQualifiedName: AnnotationQualifiedName,
            useSiteTarget: String,
            canonicalPayloadKey: String,
        ): Int {
            var result = annotationType.hashCode()
            result = 31 * result + annotationQualifiedName.hashCode()
            result = 31 * result + useSiteTarget.hashCode()
            result = 31 * result + canonicalPayloadKey.hashCode()
            return result
        }

        private const val MAX_USE_SITE_TARGET_CHARS: Int = 64

        /**
         * Transitional cap for the backend-neutral canonical payload key.
         *
         * This is intentionally larger than use-site target because payload
         * identity may later include an algorithm label plus a digest-like token.
         *
         * If future canonical annotation payload material needs a wider grammar
         * than ASCII order-id tokens, introduce FrozenAnnotationPayloadKey or
         * FrozenAnnotationPayload instead of weakening this transitional key.
         */
        private const val MAX_CANONICAL_PAYLOAD_KEY_CHARS: Int = 2_048
    }
}
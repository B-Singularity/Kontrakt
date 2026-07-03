package stage.canonicalization.material.frozen.record

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.material.TypeReference
import stage.normalization.contract.MetamodelProtocolTextGuards

/**
 * Backend-neutral property identity key.
 *
 * This key identifies one property candidate inside the deterministic property
 * sequence of one frozen raw-fact record.
 *
 * Current Level 1 shape:
 *
 * ```text
 * ownerType: TypeReference
 * propertyName: String
 * propertyType: TypeReference
 * visibilityRank: Int
 * ```
 *
 * Long-term target:
 *
 * ```text
 * ownerType: TypeReference
 * propertyName: CanonicalPropertyName
 * propertyType: TypeReference
 * visibilityRank: FrozenVisibilityRank
 * ```
 *
 * Physical-layout target:
 *
 * Later ordinal/slab lowering may replace [ownerType] and [propertyType] with
 * image-local frozen type ordinals:
 *
 * ```text
 * ownerFrozenTypeOrdinal: Int
 * propertyFrozenTypeOrdinal: Int
 * ```
 *
 * That is intentionally not done in this patch.
 *
 * Rationale:
 *
 * This class is part of the Level 1 frozen object-record foundation. The current
 * goal is deterministic identity, validation, and backend-handle erasure. The
 * physical TypeReference object chain will be removed later when property and
 * raw-fact records are lowered into owner-addressed tables or slabs.
 *
 * Backend-erasure law:
 *
 * This key must not contain:
 *
 * - reflection property handles;
 * - KProperty;
 * - KCallable;
 * - KType;
 * - KClass;
 * - KSP KSPropertyDeclaration;
 * - KSP KSType;
 * - bytecode field handles;
 * - bytecode method handles;
 * - source AST/PSI handles;
 * - adapter-local registry ids;
 * - discovery append ordinals;
 * - object identity;
 * - classloader identity.
 *
 * [ownerType] and [propertyType] are TypeReference values and must already be
 * adapter-neutral metamodel identity material.
 *
 * Transitional property-name law:
 *
 * [propertyName] is still a String, but it is not arbitrary text.
 *
 * It must be an ASCII order-id token:
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
 * If Kotlin/Java source property names later require a wider grammar than
 * ASCII order-id tokens, do not weaken this key. Introduce
 * CanonicalPropertyName with an explicit canonical text law.
 *
 * Visibility-rank law:
 *
 * [visibilityRank] is not a raw adapter ordinal.
 *
 * It must already be lowered into a backend-neutral order rank before this
 * key is issued.
 *
 * Forbidden visibility-rank sources:
 *
 * - enum.ordinal from reflection/KSP/backend APIs;
 * - source declaration order;
 * - adapter-local priority numbers;
 * - discovery append ordinals;
 * - backend-specific visibility codes.
 *
 * Allowed visibility-rank source:
 *
 * - an explicitly ratified visibility normalization policy that maps backend
 *   visibility observations into Kontrakt-owned order ranks.
 *
 * This key currently validates only the local numeric boundary:
 *
 * ```text
 * 0 <= visibilityRank <= MAX_VISIBILITY_RANK
 * ```
 *
 * A future FrozenVisibilityRank or CanonicalVisibilityRank VO should own the
 * complete visibility vocabulary and normalization law.
 *
 * Identity law:
 *
 * The key is structural over:
 *
 * - ownerType;
 * - propertyName;
 * - propertyType;
 * - visibilityRank.
 *
 * Availability is intentionally not part of this key.
 *
 * Availability drift is record-state drift, not property identity drift.
 *
 * Equality law:
 *
 * Equality is structural and is required for deterministic property sequence
 * duplicate detection.
 *
 * Reference equality is forbidden because separately issued but structurally
 * identical property keys must be treated as the same property identity.
 *
 * Hash law:
 *
 * hashCode is precomputed at issuance time as a cheap negative filter for local
 * JVM equality collections.
 *
 * The current implementation deliberately follows the existing transitional
 * metamodel hashCode policy and uses Kotlin/JVM string hashCode surfaces until
 * the later BLAKE3 / metadata-hash refactoring replaces hash policy globally.
 *
 * This hashCode value is not:
 *
 * - canonical fingerprint;
 * - persistent frozen-image identity;
 * - route key;
 * - L1/L2 partition key;
 * - PlanCacheKey material;
 * - cross-runtime order digest.
 *
 * Do not locally introduce a separate hash family in this key.
 */
class FrozenPropertyRecordKey private constructor(
    val ownerType: TypeReference,
    val propertyName: String,
    val propertyType: TypeReference,
    val visibilityRank: Int,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenPropertyRecordKey(" +
                "ownerType=${ownerType.renderSummary()}, " +
                "propertyName=$propertyName, " +
                "propertyType=${propertyType.renderSummary()}, " +
                "visibilityRank=$visibilityRank" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenPropertyRecordKey) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * not property identity authority.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return ownerType == other.ownerType &&
                propertyName == other.propertyName &&
                propertyType == other.propertyType &&
                visibilityRank == other.visibilityRank
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
            ownerType: TypeReference,
            propertyName: String,
            propertyType: TypeReference,
            visibilityRank: Int,
        ): FrozenPropertyRecordKey {
            requirePropertyName(
                propertyName = propertyName,
            )

            requireVisibilityRank(
                visibilityRank = visibilityRank,
            )

            return FrozenPropertyRecordKey(
                ownerType = ownerType,
                propertyName = propertyName,
                propertyType = propertyType,
                visibilityRank = visibilityRank,
                precomputedHashCode = computeHashCode(
                    ownerType = ownerType,
                    propertyName = propertyName,
                    propertyType = propertyType,
                    visibilityRank = visibilityRank,
                ),
            )
        }

        /**
         * Validates the transitional property-name token.
         *
         * This guard is intentionally strict because this field participates in
         * frozen property identity.
         *
         * Do not replace this with a local isNotEmpty() check.
         * Do not throw IllegalArgumentException from this boundary.
         */
        private fun requirePropertyName(
            propertyName: String,
        ) {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenPropertyRecordKey.propertyName",
                value = propertyName,
                maxChars = MAX_PROPERTY_NAME_CHARS,
            )
        }

        /**
         * Validates the local numeric boundary for the transitional visibility
         * rank.
         *
         * This does not define the visibility vocabulary. It only prevents raw,
         * negative, unbounded, or obviously polluted numeric material from
         * entering the frozen property key.
         *
         * The backend-neutral visibility mapping must already have happened
         * before this key is issued.
         */
        private fun requireVisibilityRank(
            visibilityRank: Int,
        ) {
            if (visibilityRank in MIN_VISIBILITY_RANK..MAX_VISIBILITY_RANK) {
                return
            }

            throw MetamodelFactContractViolationException(
                "FrozenPropertyRecordKey.visibilityRank is outside the ratified transitional range: " +
                        "visibilityRank=$visibilityRank, " +
                        "min=$MIN_VISIBILITY_RANK, max=$MAX_VISIBILITY_RANK",
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
            ownerType: TypeReference,
            propertyName: String,
            propertyType: TypeReference,
            visibilityRank: Int,
        ): Int {
            var result = ownerType.hashCode()
            result = 31 * result + propertyName.hashCode()
            result = 31 * result + propertyType.hashCode()
            result = 31 * result + visibilityRank
            return result
        }

        private const val MAX_PROPERTY_NAME_CHARS: Int = 256

        private const val MIN_VISIBILITY_RANK: Int = 0

        /**
         * Transitional bound.
         *
         * The current property key stores visibility as an Int only because the
         * frozen visibility vocabulary has not yet been ratified as a dedicated
         * VO.
         *
         * Keep this bound small enough to reject arbitrary backend numeric
         * material, but large enough to leave room for public/internal/protected/
         * private/synthetic-demoted/backend-unavailable ranks during the
         * transitional phase.
         */
        private const val MAX_VISIBILITY_RANK: Int = 16
    }
}
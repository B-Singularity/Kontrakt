package stage.canonicalization.material.frozen.records

import stage.canonicalization.contract.representative.MetamodelProtocolTextGuards
import stage.canonicalization.material.representation.TypeReference

/**
 * Backend-neutral constructor identity key.
 *
 * This key identifies one constructor candidate inside the deterministic
 * constructor sequence of one frozen raw-fact records.
 *
 * Current Level 1 shape:
 *
 * ```text
 * ownerType: TypeReference
 * constructorSignature: String
 * parameterShapeSignature: String
 * ```
 *
 * Long-term target:
 *
 * ```text
 * ownerType: TypeReference
 * constructorSignature: CanonicalConstructorSignature
 * parameterShapeSignature: CanonicalParameterShapeSignature
 * ```
 *
 * Physical-layout target:
 *
 * Later ordinal/slab lowering may replace [ownerType] with an image-local
 * frozen type ordinal or owner-type ordinal reference:
 *
 * ```text
 * ownerFrozenTypeOrdinal: Int
 * ```
 *
 * That is intentionally not done in this patch.
 *
 * Rationale:
 *
 * This class is part of the Level 1 frozen object-records foundation. The current
 * goal is deterministic identity, validation, and backend-handle erasure. The
 * physical owner-reference chain will be removed later when constructor and raw
 * fact records are lowered into owner-addressed tables or slabs.
 *
 * Backend-erasure law:
 *
 * This key must not contain:
 *
 * - reflection constructor handles;
 * - KFunction;
 * - KParameter;
 * - KSType;
 * - KSFunctionDeclaration;
 * - bytecode method nodes;
 * - source AST/PSI handles;
 * - adapter-local registry ids;
 * - discovery append ordinals;
 * - object identity;
 * - classloader identity.
 *
 * [ownerType] is a TypeReference and must already be adapter-neutral metamodel
 * identity material.
 *
 * Transitional string law:
 *
 * [constructorSignature] and [parameterShapeSignature] are still strings, but
 * they are not arbitrary strings.
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
 * If constructor signature material later needs a richer canonical grammar than
 * ASCII order-id tokens, do not weaken this key. Introduce
 * CanonicalConstructorSignature and CanonicalParameterShapeSignature with their
 * own canonical text law.
 *
 * Identity law:
 *
 * The key is structural over:
 *
 * - ownerType;
 * - constructorSignature;
 * - parameterShapeSignature.
 *
 * Availability is intentionally not part of this key.
 *
 * Availability drift is records-state drift, not constructor identity drift.
 *
 * Equality law:
 *
 * Equality is structural and is required for deterministic constructor sequence
 * duplicate detection.
 *
 * Reference equality is forbidden because separately issued but structurally
 * identical constructor keys must be treated as the same constructor identity.
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
class FrozenConstructorRecordKey private constructor(
    val ownerType: TypeReference,
    val constructorSignature: String,
    val parameterShapeSignature: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenConstructorRecordKey(" +
                "ownerType=${ownerType.renderSummary()}, " +
                "constructorSignature=$constructorSignature, " +
                "parameterShapeSignature=$parameterShapeSignature" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenConstructorRecordKey) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * not constructor identity authority.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return ownerType == other.ownerType &&
                constructorSignature == other.constructorSignature &&
                parameterShapeSignature == other.parameterShapeSignature
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
            constructorSignature: String,
            parameterShapeSignature: String,
        ): FrozenConstructorRecordKey {
            requireConstructorSignature(
                constructorSignature = constructorSignature,
            )

            requireParameterShapeSignature(
                parameterShapeSignature = parameterShapeSignature,
            )

            return FrozenConstructorRecordKey(
                ownerType = ownerType,
                constructorSignature = constructorSignature,
                parameterShapeSignature = parameterShapeSignature,
                precomputedHashCode = computeHashCode(
                    ownerType = ownerType,
                    constructorSignature = constructorSignature,
                    parameterShapeSignature = parameterShapeSignature,
                ),
            )
        }

        /**
         * Validates the transitional constructor signature token.
         *
         * This guard is intentionally strict because this field participates in
         * frozen constructor identity.
         *
         * Do not replace this with a local isNotEmpty() check.
         * Do not throw IllegalArgumentException from this boundary.
         */
        private fun requireConstructorSignature(
            constructorSignature: String,
        ) {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenConstructorRecordKey.constructorSignature",
                value = constructorSignature,
                maxChars = MAX_CONSTRUCTOR_SIGNATURE_CHARS,
            )
        }

        /**
         * Validates the transitional parameter-shape signature token.
         *
         * This value is not a backend descriptor. It is frozen key material and
         * must already be lowered into backend-neutral order text.
         */
        private fun requireParameterShapeSignature(
            parameterShapeSignature: String,
        ) {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenConstructorRecordKey.parameterShapeSignature",
                value = parameterShapeSignature,
                maxChars = MAX_PARAMETER_SHAPE_SIGNATURE_CHARS,
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
            constructorSignature: String,
            parameterShapeSignature: String,
        ): Int {
            var result = ownerType.hashCode()
            result = 31 * result + constructorSignature.hashCode()
            result = 31 * result + parameterShapeSignature.hashCode()
            return result
        }

        private const val MAX_CONSTRUCTOR_SIGNATURE_CHARS: Int = 512
        private const val MAX_PARAMETER_SHAPE_SIGNATURE_CHARS: Int = 512
    }
}
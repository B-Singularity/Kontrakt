package metamodel.domain.frozen.record

import metamodel.domain.frozen.availability.FrozenMetadataAvailability
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import metamodel.domain.vo.TypeReference

/**
 * Frozen constructor-parameter record.
 *
 * This is the Level 1 frozen object-record representation for one constructor
 * parameter after backend-neutral lowering.
 *
 * It is not:
 *
 * - a reflection KParameter;
 * - a KSP parameter symbol;
 * - a bytecode parameter node;
 * - a source AST/PSI parameter node;
 * - an adapter-local slot;
 * - a discovery append ordinal;
 * - a primitive/slab row.
 *
 * Identity split:
 *
 * [key] identifies the parameter structurally by:
 *
 * ```text
 * ownerConstructorKey + parameterIndex
 * ```
 *
 * [parameterName] is deliberately record payload, not key material.
 *
 * Rationale:
 *
 * Parameter names may be unavailable, synthetic, compiler-option-dependent, or
 * backend-capability-dependent. Allowing names to participate in identity would
 * make otherwise equivalent frozen images diverge across reflection, KSP,
 * bytecode, and source acquisition paths.
 *
 * Name lowering law:
 *
 * [parameterName] is still required to be deterministic, sanitized,
 * backend-neutral order material.
 *
 * It must not be arbitrary backend text.
 *
 * The acquisition adapter must lower missing or backend-synthetic names into a
 * deterministic token before this record is issued. That token must not encode:
 *
 * - object identity;
 * - registry id;
 * - discovery append ordinal;
 * - backend-local slot id;
 * - classloader identity;
 * - local filesystem path.
 *
 * If future design needs to distinguish declared names from synthetic fallback
 * names, introduce a dedicated parameter-name availability/value object instead
 * of adding parameterName back into the key.
 *
 * Backend-erasure law:
 *
 * [parameterType] is a TypeReference. It must already be adapter-neutral
 * metamodel identity material.
 *
 * This record must not store:
 *
 * - KType;
 * - KClass;
 * - KParameter;
 * - KSType;
 * - KSValueParameter;
 * - bytecode handles;
 * - source AST/PSI handles;
 * - adapter registry ids;
 * - closures, suppliers, or lazy delegates that can recover backend handles.
 *
 * Type-index membership law:
 *
 * This record does not validate that [parameterType] belongs to a particular
 * FrozenMetamodelImage.
 *
 * Reason:
 *
 * A single parameter record is image-agnostic. It does not own the type index,
 * shape table, cycle table, raw fact table, or publication gate.
 *
 * Membership and complete-slot coverage must be validated by the frozen image
 * publication validator:
 *
 * ```text
 * parameterType -> typeIndex.ordinalOf(parameterType)
 *               -> shape/cycle/raw slot coverage
 * ```
 *
 * Parameter-index compactness law:
 *
 * This record can validate only local key shape.
 *
 * It cannot prove that all parameters of the owning constructor form:
 *
 * ```text
 * 0, 1, 2, ..., N - 1
 * ```
 *
 * That compactness law belongs to FrozenConstructorParameterRecordSequence
 * construction.
 *
 * Availability law:
 *
 * [declarationAvailability] is record state.
 *
 * It is not part of FrozenConstructorParameterRecordKey. Availability drift is
 * not identity drift. If two records have the same semantic key but conflicting
 * availability payload, the sequence builder must fail closed unless a future
 * ADR ratifies an availability merge law.
 *
 * Equality law:
 *
 * Equality is structural over the full frozen record payload:
 *
 * - key;
 * - parameterName;
 * - parameterType;
 * - declarationAvailability.
 *
 * Primary duplicate detection in deterministic parameter sequences must still
 * be key-driven:
 *
 * ```text
 * key == key
 * ```
 *
 * not full-record equality.
 *
 * Hash law:
 *
 * hashCode is a transitional in-memory equality-collection companion.
 *
 * It currently composes the existing metamodel VO hashCode surfaces and must
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
 * strategy globally. Do not introduce a local hash family in this record.
 */
class FrozenConstructorParameterRecord private constructor(
    val key: FrozenConstructorParameterRecordKey,
    val parameterName: String,
    val parameterType: TypeReference,
    val declarationAvailability: FrozenMetadataAvailability,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenConstructorParameterRecord(" +
                "key=${key.renderSummary()}, " +
                "parameterName=$parameterName, " +
                "parameterType=${parameterType.renderSummary()}, " +
                "declarationAvailability=$declarationAvailability" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenConstructorParameterRecord) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return key == other.key &&
                parameterName == other.parameterName &&
                parameterType == other.parameterType &&
                declarationAvailability == other.declarationAvailability
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
            key: FrozenConstructorParameterRecordKey,
            parameterName: String,
            parameterType: TypeReference,
            declarationAvailability: FrozenMetadataAvailability,
        ): FrozenConstructorParameterRecord {
            requireParameterName(
                parameterName = parameterName,
            )

            return FrozenConstructorParameterRecord(
                key = key,
                parameterName = parameterName,
                parameterType = parameterType,
                declarationAvailability = declarationAvailability,
                precomputedHashCode = computeHashCode(
                    key = key,
                    parameterName = parameterName,
                    parameterType = parameterType,
                    declarationAvailability = declarationAvailability,
                ),
            )
        }

        /**
         * Validates the transitional constructor parameter name.
         *
         * This intentionally delegates to the shared metamodel order-token
         * guard instead of using a local isNotEmpty() check.
         *
         * Do not replace this with IllegalArgumentException.
         */
        private fun requireParameterName(
            parameterName: String,
        ) {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenConstructorParameterRecord.parameterName",
                value = parameterName,
                maxChars = MAX_PARAMETER_NAME_CHARS,
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
            key: FrozenConstructorParameterRecordKey,
            parameterName: String,
            parameterType: TypeReference,
            declarationAvailability: FrozenMetadataAvailability,
        ): Int {
            var result = key.hashCode()
            result = 31 * result + parameterName.hashCode()
            result = 31 * result + parameterType.hashCode()
            result = 31 * result + declarationAvailability.hashCode()
            return result
        }

        private const val MAX_PARAMETER_NAME_CHARS: Int = 128
    }
}
package stage.canonicalization.material.frozen.record

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException

/**
 * Backend-neutral constructor-parameter identity key.
 *
 * This key identifies one constructor parameter inside one frozen constructor
 * record.
 *
 * Identity authority:
 *
 * ```text
 * ownerConstructorKey + parameterIndex
 * ```
 *
 * Parameter name is intentionally not part of this key.
 *
 * Rationale:
 *
 * Parameter names are backend-capability-sensitive:
 *
 * - source/KSP acquisition may preserve declared names;
 * - reflection may expose names only when metadata is available;
 * - bytecode may expose synthetic or compiler-generated names;
 * - Java/Kotlin compiler flags may affect whether names are available.
 *
 * If parameterName participated in the key, the same logical constructor
 * parameter could become a different frozen parameter merely because one
 * backend preserved the name while another backend lowered a synthetic name.
 *
 * That would make frozen image comparison, diagnostics, and future image merge
 * behavior depend on source adapter capability rather than constructor
 * structure.
 *
 * Therefore:
 *
 * - parameterIndex is the identity axis inside the owner constructor;
 * - parameterName is record payload/state;
 * - parameterName must still be lowered deterministically and validated by the
 *   record, but it must not decide key equality.
 *
 * Parameter-index law:
 *
 * [parameterIndex] must be non-negative.
 *
 * This key cannot prove compactness by itself because compactness is a property
 * of the whole parameter sequence.
 *
 * The sequence builder must validate:
 *
 * ```text
 * indexes == 0, 1, 2, ..., N - 1
 * ```
 *
 * and must fail closed on:
 *
 * - duplicate parameter indexes;
 * - missing parameter indexes;
 * - non-compact indexes;
 * - backend enumeration order being treated as semantic order.
 *
 * Object-reference law:
 *
 * This Level 1 object-record key keeps [ownerConstructorKey] as an object
 * reference.
 *
 * This is acceptable for the current frozen foundation stage because the
 * current target is object-array / ordinal-friendly correctness, not primitive
 * slab compression.
 *
 * Later lowering may replace this with:
 *
 * ```text
 * ownerConstructorFrozenOrdinal
 * ```
 *
 * or another image-local constructor ordinal to break parent-reference chains.
 *
 * That change belongs to a later physical-layout refactoring. It must not be
 * introduced here by weakening the semantic key law.
 *
 * Equality law:
 *
 * Equality is structural over:
 *
 * - ownerConstructorKey;
 * - parameterIndex.
 *
 * Availability and parameterName are intentionally excluded from this key.
 *
 * Hash law:
 *
 * hashCode is a transitional in-memory equality-collection companion.
 *
 * It currently composes existing metamodel VO hashCode surfaces and must not
 * become:
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
class FrozenConstructorParameterRecordKey private constructor(
    val ownerConstructorKey: FrozenConstructorRecordKey,
    val parameterIndex: Int,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenConstructorParameterRecordKey(" +
                "ownerConstructorKey=${ownerConstructorKey.renderSummary()}, " +
                "parameterIndex=$parameterIndex" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenConstructorParameterRecordKey) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return ownerConstructorKey == other.ownerConstructorKey &&
                parameterIndex == other.parameterIndex
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
            ownerConstructorKey: FrozenConstructorRecordKey,
            parameterIndex: Int,
        ): FrozenConstructorParameterRecordKey {
            requireParameterIndex(
                parameterIndex = parameterIndex,
            )

            return FrozenConstructorParameterRecordKey(
                ownerConstructorKey = ownerConstructorKey,
                parameterIndex = parameterIndex,
                precomputedHashCode = computeHashCode(
                    ownerConstructorKey = ownerConstructorKey,
                    parameterIndex = parameterIndex,
                ),
            )
        }

        private fun requireParameterIndex(
            parameterIndex: Int,
        ) {
            if (parameterIndex >= 0) {
                return
            }

            throw MetamodelFactContractViolationException(
                "FrozenConstructorParameterRecordKey.parameterIndex must be non-negative: " +
                        "parameterIndex=$parameterIndex",
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
            ownerConstructorKey: FrozenConstructorRecordKey,
            parameterIndex: Int,
        ): Int {
            var result = ownerConstructorKey.hashCode()
            result = 31 * result + parameterIndex
            return result
        }
    }
}
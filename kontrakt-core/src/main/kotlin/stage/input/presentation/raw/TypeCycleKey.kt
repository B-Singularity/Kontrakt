package stage.input.presentation.raw

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.material.representation.CanonicalTypeTextGuards

/**
 * Cycle-detection structural key.
 *
 * This is not:
 *
 * - a source type string;
 * - a JVM descriptor;
 * - a binary class name;
 * - a reflection/KSP handle;
 * - a type-shape classifier;
 * - a cache key;
 * - or a canonical byte encoding.
 *
 * Cycle identity law:
 *
 * - all nullability markers must already be stripped before this value is issued;
 * - reified generic structure remains;
 * - backend/binary names are forbidden by CanonicalTypeTextGuards;
 * - source variance syntax must already be lowered or rejected;
 * - star projection must already be lowered or rejected;
 * - value is the equality axis.
 *
 * Shape law:
 *
 * shapeSummary is coherence metadata.
 *
 * It is intentionally not an additional equality axis. If the same cycle value
 * is issued with different shape metadata, that is upstream metamodel drift and
 * must be rejected by the factory/ratifier/coherence scope that owns issuance.
 *
 * This VO does not infer shape from hardcoded strings such as:
 *
 * - "void";
 * - "kotlin.Unit";
 * - "java.util.List";
 * - "[]".
 *
 * Reason:
 *
 * String-based platform interpretation belongs to adapter/classifier policy.
 * TypeCycleKey must remain a pure structural key and must not know JVM/Kotlin
 * platform vocabulary.
 *
 * Equality law:
 *
 * Equality is value-primary.
 *
 * This is intentional for cycle detection. The question is:
 *
 *     "Have we reached the same structural cycle key?"
 *
 * not:
 *
 *     "Did the adapter attach the same metadata object?"
 *
 * Hash law:
 *
 * hashCode is precomputed at issuance time because TypeCycleKey is expected to
 * be used in hot visited-set / active-cycle-set paths.
 *
 * The precomputed hash is still only for in-memory equality collections.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime order hash;
 * - serialized order digest.
 */
class TypeCycleKey private constructor(
    val value: String,
    val shapeSummary: TypeShapeSummary,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "TypeCycleKey(" +
                "value=$value, " +
                "shape=${shapeSummary.kind.protocolToken}, " +
                "arrayRank=${shapeSummary.arrayRank}, " +
                "genericArity=${shapeSummary.genericArity}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is TypeCycleKey) return false

        /*
         * Cheap negative filter.
         * Structural equality remains value-only.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return value
    }

    companion object {
        @JvmStatic
        fun issue(
            value: String,
            shapeSummary: TypeShapeSummary,
        ): TypeCycleKey {
            /*
             * Cycle keys must be nullability-erased structural material.
             *
             * allowNullableMarker = false is the important cycle-key distinction
             * from CanonicalTypeId / CanonicalTypeSignature surfaces.
             */
            CanonicalTypeTextGuards.validateInspectedSnapshot(
                field = "TypeCycleKey.value",
                snapshot = value,
                allowNullableMarker = false,
                allowStarProjection = false,
            )

            requireShapeSummaryBoundary(
                value = value,
                shapeSummary = shapeSummary,
            )

            return TypeCycleKey(
                value = value,
                shapeSummary = shapeSummary,
                precomputedHashCode = value.hashCode(),
            )
        }

        /**
         * Local boundary check only.
         *
         * Do not infer semantic shape from the text value here.
         * Do not check same-value/same-shape global coherence here.
         *
         * Same-value/different-shape drift must be rejected by the issuing
         * coherence scope, because only the scope can observe previously issued
         * values.
         */
        private fun requireShapeSummaryBoundary(
            value: String,
            shapeSummary: TypeShapeSummary,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "TypeCycleKey.value must not be empty.",
                )
            }

            /*
             * TypeShapeSummary owns its own cardinality law.
             *
             * This boundary intentionally does not duplicate checks such as:
             *
             * - ARRAY must have arrayRank > 0;
             * - ATOMIC must not have generic arity;
             * - MAP must have valid arity.
             */
            if (shapeSummary.kind.protocolToken.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "TypeCycleKey.shapeSummary kind must expose order token.",
                )
            }
        }
    }
}
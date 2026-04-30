package metamodel.domain.vo

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Cycle-detection structural key.
 *
 * Cycle identity law:
 * - all nullability markers are stripped before this value is issued;
 * - reified generic structure remains;
 * - backend/binary names are forbidden;
 * - source variance syntax must already be lowered;
 * - star projection must already be lowered.
 *
 * This class does not parse raw type syntax.
 * It accepts only already-lowered structural cycle material.
 *
 * Equality is value-primary.
 *
 * shapeSummary is coherence metadata, not an additional equality axis.
 * If the same value is issued with a different shapeSummary, that is upstream
 * metamodel drift and must fail closed at issue time or factory ratification
 * time.
 */
class TypeCycleKey private constructor(
    val value: String,
    val shapeSummary: TypeShapeSummary,
) {
    override fun equals(other: Any?): Boolean {
        return other is TypeCycleKey && value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
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
            CanonicalTypeTextGuards.validateCanonicalTypeText(
                field = "TypeCycleKey.value",
                value = value,
                allowNullableMarker = false,
                allowStarProjection = false,
            )

            requireKnownCycleCoherence(
                value = value,
                shapeSummary = shapeSummary,
            )

            return TypeCycleKey(
                value = value,
                shapeSummary = shapeSummary,
            )
        }

        private fun requireKnownCycleCoherence(
            value: String,
            shapeSummary: TypeShapeSummary,
        ) {
            if (value == "void") {
                requireKind(
                    value = value,
                    actual = shapeSummary.kind,
                    expected = CanonicalTypeShapeKind.VOID,
                )
            }

            if (value == "kotlin.Unit") {
                requireKind(
                    value = value,
                    actual = shapeSummary.kind,
                    expected = CanonicalTypeShapeKind.UNIT,
                )
            }

            if (value.endsWith("[]")) {
                requireKind(
                    value = value,
                    actual = shapeSummary.kind,
                    expected = CanonicalTypeShapeKind.ARRAY,
                )
            }
        }

        private fun requireKind(
            value: String,
            actual: CanonicalTypeShapeKind,
            expected: CanonicalTypeShapeKind,
        ) {
            if (actual != expected) {
                throw TypeExpansionContractViolationException(
                    reason = "TypeCycleKey shape mismatch: value=$value, expected=${expected.protocolToken}, actual=${actual.protocolToken}",
                )
            }
        }
    }
}
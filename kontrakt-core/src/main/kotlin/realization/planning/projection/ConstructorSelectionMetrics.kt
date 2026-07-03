package realization.planning.projection

import stage.lowering.diagnostics.ActiveMemberProjectionException

/**
 * Frozen evidence for deterministic constructor selection.
 *
 * These values are not constructor identity.
 * They are not a weighted score.
 *
 * They are the observable numeric components of the constructor-selection
 * priority tuple:
 *
 * 1. strongSatisfiableCount      descending
 * 2. defaultAvailableCount       descending
 * 3. nullableAvailableCount      descending
 * 4. totalParameterCount         ascending
 *
 * The remaining tie-break dimensions live outside this metrics object:
 * - constructorSignature
 * - constructorSignatureNormalizationVersion
 *
 * Rationale:
 * SelectedConstructor should preserve why the constructor was selected without
 * requiring semantic recomputation during diagnostics, rollback, restart, or
 * trace rendering.
 */
class ConstructorSelectionMetrics private constructor(
    val strongSatisfiableCount: Int,
    val defaultAvailableCount: Int,
    val nullableAvailableCount: Int,
    val totalParameterCount: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            strongSatisfiableCount: Int,
            defaultAvailableCount: Int,
            nullableAvailableCount: Int,
            totalParameterCount: Int,
        ): ConstructorSelectionMetrics {
            requireNonNegativeMetric(
                name = "strongSatisfiableCount",
                value = strongSatisfiableCount,
            )
            requireNonNegativeMetric(
                name = "defaultAvailableCount",
                value = defaultAvailableCount,
            )
            requireNonNegativeMetric(
                name = "nullableAvailableCount",
                value = nullableAvailableCount,
            )
            requireNonNegativeMetric(
                name = "totalParameterCount",
                value = totalParameterCount,
            )

            if (strongSatisfiableCount > totalParameterCount) {
                throw ActiveMemberProjectionException(
                    "ConstructorSelectionMetrics.strongSatisfiableCount must be <= totalParameterCount: " +
                            "strongSatisfiableCount=$strongSatisfiableCount, totalParameterCount=$totalParameterCount",
                )
            }

            if (nullableAvailableCount > totalParameterCount) {
                throw ActiveMemberProjectionException(
                    "ConstructorSelectionMetrics.nullableAvailableCount must be <= totalParameterCount: " +
                            "nullableAvailableCount=$nullableAvailableCount, totalParameterCount=$totalParameterCount",
                )
            }

            if (defaultAvailableCount > totalParameterCount) {
                throw ActiveMemberProjectionException(
                    "ConstructorSelectionMetrics.defaultAvailableCount must be <= totalParameterCount: " +
                            "defaultAvailableCount=$defaultAvailableCount, totalParameterCount=$totalParameterCount",
                )
            }

            /*
             * Current nullability partition law:
             *
             * NON_NULL  -> strong
             * UNKNOWN   -> strong
             * NULLABLE  -> nullable
             *
             * Therefore strong + nullable must partition the whole parameter set.
             *
             * defaultAvailableCount is intentionally excluded from this partition
             * because default availability is an orthogonal property. A parameter can
             * be both strong and default-available.
             */
            if (strongSatisfiableCount + nullableAvailableCount != totalParameterCount) {
                throw ActiveMemberProjectionException(
                    "ConstructorSelectionMetrics nullability metrics must partition total parameters: " +
                            "strongSatisfiableCount=$strongSatisfiableCount, " +
                            "nullableAvailableCount=$nullableAvailableCount, " +
                            "totalParameterCount=$totalParameterCount",
                )
            }

            return ConstructorSelectionMetrics(
                strongSatisfiableCount = strongSatisfiableCount,
                defaultAvailableCount = defaultAvailableCount,
                nullableAvailableCount = nullableAvailableCount,
                totalParameterCount = totalParameterCount,
            )
        }

        private fun requireNonNegativeMetric(
            name: String,
            value: Int,
        ) {
            if (value < 0) {
                throw ActiveMemberProjectionException(
                    "ConstructorSelectionMetrics.$name must be >= 0: $value",
                )
            }
        }
    }
}

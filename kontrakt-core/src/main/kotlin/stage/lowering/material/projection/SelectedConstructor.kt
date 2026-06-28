package stage.lowering.material.projection

import stage.lowering.diagnostics.ActiveMemberProjectionException
import stage.input.material.ConstructorCandidateFact

/**
 * Deterministically selected constructor for one node-expansion episode.
 *
 * This is a Core-owned semantic result.
 * It must not be produced by metamodel adapters.
 *
 * This object preserves:
 * - which constructor was selected,
 * - the frozen selection metrics explaining why it won the numeric portion of
 *   the constructor-selection tuple.
 *
 * The metrics are decision evidence, not constructor identity.
 */
class SelectedConstructor private constructor(
    val ownerTypeFqcn: String,
    val candidate: ConstructorCandidateFact,
    val metrics: ConstructorSelectionMetrics,
) {
    companion object {
        @JvmStatic
        fun issue(
            candidate: ConstructorCandidateFact,
            metrics: ConstructorSelectionMetrics,
        ): SelectedConstructor {
            if (candidate.parameters.size != metrics.totalParameterCount) {
                throw ActiveMemberProjectionException(
                    "SelectedConstructor.metrics.totalParameterCount must match candidate.parameters.size: " +
                            "totalParameterCount=${metrics.totalParameterCount}, " +
                            "candidateParameterCount=${candidate.parameters.size}",
                )
            }

            return SelectedConstructor(
                ownerTypeFqcn = candidate.ownerTypeFqcn,
                candidate = candidate,
                metrics = metrics,
            )
        }
    }
}

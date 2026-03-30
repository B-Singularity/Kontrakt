package planning.domain.runtime.orchestration

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Immutable continuity identifier for one logical end-to-end planning run.
 *
 * One PlanningRunEpoch may span:
 * - one initial worker-local PlannerSession,
 * - zero or more joined-wait suspensions,
 * - zero or more fresh-session restarts,
 * - and exactly one terminal run outcome.
 *
 * This type is intentionally NOT:
 * - a runtime-policy version,
 * - a worker-backing freshness/version marker,
 * - or an L2 lifecycle-host generation token.
 */
class PlanningRunEpoch private constructor(
    val id: Long,
) {

    override fun toString(): String {
        return "PlanningRunEpoch(id=$id)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanningRunEpoch) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        @JvmStatic
        fun issue(
            id: Long,
        ): PlanningRunEpoch {
            if (id <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "PlanningRunEpoch.id must be > 0: $id"
                )
            }
            return PlanningRunEpoch(id = id)
        }
    }
}
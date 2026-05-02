package planning.infrastructure.runtime.worker

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Freshness/version marker for reusable worker-local planner backing.
 *
 * This epoch belongs to worker-local backing reuse only.
 *
 * It exists to support logical-freshness-on-reuse semantics for:
 * - primitive arrays,
 * - slabs,
 * - index tables,
 * - undo logs,
 * - and other worker-local planner substrates.
 *
 * It must remain distinct from:
 * - PlanningRunEpoch
 * - RuntimePolicyEpoch
 * - L2HostGeneration
 */
class WorkerBackingEpoch private constructor(
    val id: Long,
) {
    override fun toString(): String = "WorkerBackingEpoch(id=$id)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorkerBackingEpoch) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        @JvmStatic
        fun issue(id: Long): WorkerBackingEpoch {
            if (id <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "WorkerBackingEpoch.id must be > 0: $id",
                )
            }

            return WorkerBackingEpoch(id = id)
        }
    }
}

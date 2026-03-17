package planning.infrastructure.runtime.policy

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.session.policy.ResolvedRuntimePolicy

/**
 * Immutable runtime-policy snapshot tagged with a monotonic epoch identifier.
 *
 * This type is intentionally factory-issued:
 * - no public primary constructor
 * - no data-class copy() backdoor
 *
 * Equality is structural:
 * - same epoch id
 * - same immutable policy payload
 *
 * This is required so that the registry can distinguish:
 * - benign duplicate delivery of the exact same snapshot
 * - same-id / different-payload integrity violations
 */
class PolicyEpoch private constructor(
    val id: Long,
    val policy: ResolvedRuntimePolicy,
) {

    override fun toString(): String {
        return "PolicyEpoch(id=$id, policy=$policy)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PolicyEpoch) return false

        return id == other.id && policy == other.policy
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + policy.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun issue(
            id: Long,
            policy: ResolvedRuntimePolicy,
        ): PolicyEpoch {
            if (id < 0L) {
                throw PlanningProtocolIntegrityException(
                    "PolicyEpoch.id must be >= 0: $id"
                )
            }

            return PolicyEpoch(
                id = id,
                policy = policy,
            )
        }
    }
}
package planning.domain.port.outgoing

import ir.plan.node.CanonicalPlanNode
import ir.plan.signature.PlanCacheKey
import planning.domain.fault.L2FaultKind
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Outbound port for Tier-2 structural interning.
 *
 * The Domain Core owns the policy:
 * - Hit(node)              -> reuse the exact canonical instance
 * - Miss(handle)           -> build locally, then publish through the handle
 * - Fault(TRANSIENT)       -> degrade to miss
 * - Fault(CIRCUIT_OPEN)    -> bypass Tier-2 for the remainder of the session
 *
 * Implementations MUST preserve:
 * - exact-instance return
 * - safe publication
 * - bounded in-flight joining
 * - partition-scoped lifecycle governance
 */
interface PlanInternRepository {
    fun resolveOrIntern(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
    ): PlanInternResult
}

/**
 * Result of a Tier-2 interning attempt.
 *
 * This is intentionally modeled as protocol state, not as a data record.
 * We do NOT want copy()/componentN()/structural value semantics here.
 */
sealed interface PlanInternResult {

    /**
     * Exact canonical instance already exists in Tier-2.
     */
    class Hit internal constructor(
        val node: CanonicalPlanNode,
    ) : PlanInternResult {
        override fun toString(): String = "PlanInternResult.Hit(node=$node)"
    }

    /**
     * The caller won the in-flight gate and is now the designated builder.
     *
     * The caller MUST eventually invoke exactly one of:
     * - handle.commit(localNode)
     * - handle.abort(reason)
     */
    class Miss internal constructor(
        val handle: InternHandle,
    ) : PlanInternResult {
        override fun toString(): String = "PlanInternResult.Miss"
    }

    /**
     * Governance signal from the adapter.
     *
     * Semantics are defined solely by [L2FaultKind].
     */
    class Fault internal constructor(
        val kind: L2FaultKind,
    ) : PlanInternResult {
        override fun toString(): String = "PlanInternResult.Fault(kind=$kind)"
    }

    companion object {
        internal fun hit(node: CanonicalPlanNode): PlanInternResult = Hit(node)
        internal fun miss(handle: InternHandle): PlanInternResult = Miss(handle)
        internal fun fault(kind: L2FaultKind): PlanInternResult = Fault(kind)
    }
}

/**
 * Builder-owned publication handle.
 *
 * The caller builds outside adapter locks, then publishes through this handle.
 */
interface InternHandle {
    /**
     * Publishes the locally built node.
     *
     * @return the exact canonical winner instance.
     *         This MAY differ from [localNode] if another exact canonical instance
     *         is already present in the target bucket.
     */
    fun commit(localNode: CanonicalPlanNode): CanonicalPlanNode

    /**
     * Aborts the in-flight build and wakes any joiners exceptionally.
     */
    fun abort(reason: Throwable)
}
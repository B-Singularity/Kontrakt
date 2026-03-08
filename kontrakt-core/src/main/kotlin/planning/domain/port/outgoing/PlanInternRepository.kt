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
 * - Hit(node)              -> reuse exact canonical instance
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
 * Result of an interning attempt.
 */
sealed interface PlanInternResult {
    /**
     * Exact canonical instance already exists in Tier-2.
     */
    data class Hit(
        val node: CanonicalPlanNode,
    ) : PlanInternResult

    /**
     * The caller won the in-flight gate and is now the designated builder.
     *
     * The caller MUST eventually invoke exactly one of:
     * - handle.commit(localNode)
     * - handle.abort(reason)
     */
    data class Miss(
        val handle: InternHandle,
    ) : PlanInternResult

    /**
     * Governance signal from the adapter.
     *
     * Semantics are defined solely by [L2FaultKind].
     */
    data class Fault(
        val kind: L2FaultKind,
    ) : PlanInternResult
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
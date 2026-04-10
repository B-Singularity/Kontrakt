package planning.domain.port.outgoing

import ir.plan.node.CanonicalPlanNode
import planning.domain.fault.L2FaultKind
import planning.domain.interner.PlanCacheKey
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Outbound port for Tier-2 structural interning.
 *
 * -----------------------------------------------------------------------------
 * ARCHITECTURAL ROLE
 * -----------------------------------------------------------------------------
 *
 * This is a hexagonal outbound port.
 *
 * The Domain Core asks the adapter:
 *
 *   "For this semantic cache key, what is the next lawful interning step?"
 *
 * The adapter answers with a closed, immutable next-step algebra:
 *
 * - Hit   : exact canonical winner already exists
 * - Build : caller owns build/publication authority for this episode
 * - Join  : caller must suspend and later restart through continuation delivery
 * - Fault : governance reaction is required
 *
 * -----------------------------------------------------------------------------
 * IMPORTANT DISTINCTION
 * -----------------------------------------------------------------------------
 *
 * This sealed result family is NOT a mutable lifecycle state machine.
 *
 * It is:
 * - immutable
 * - one-shot
 * - boundary-local
 * - action-oriented
 *
 * Lifecycle state machines remain elsewhere:
 * - shared-slot lifecycle
 * - waiter lifecycle
 * - builder-handle lifecycle
 * - commit-right lifecycle
 * - region lifecycle
 * - speculative-lease lifecycle
 *
 * -----------------------------------------------------------------------------
 * NON-BLOCKING JOIN REQUIREMENT
 * -----------------------------------------------------------------------------
 *
 * Implementations MUST preserve non-blocking join semantics.
 *
 * In particular:
 * - Join MUST NOT imply worker-thread monopolization
 * - Join MUST NOT require wait()/get()/join()/poll()-style blocking
 * - completion delivery belongs to adapter-owned dispatch infrastructure
 */
interface PlanInternRepository {

    /**
     * Resolves the next Tier-2 interning step for the given semantic plan-cache key.
     *
     * This method itself remains synchronous as a boundary call.
     * "Synchronous" here means only that the next-step result is returned immediately.
     *
     * It does NOT mean that joined waiting is blocking.
     */
    fun resolveOrIntern(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
    ): PlanInternStep
}

sealed interface PlanInternStep {

    class Hit internal constructor(
        val node: CanonicalPlanNode,
    ) : PlanInternStep {
        override fun toString(): String = "PlanInternStep.Hit(node=$node)"
    }

    class Build internal constructor(
        val handle: BuildHandle,
    ) : PlanInternStep {
        override fun toString(): String = "PlanInternStep.Build"
    }

    class Join internal constructor(
        val handle: JoinHandle,
    ) : PlanInternStep {
        override fun toString(): String = "PlanInternStep.Join"
    }

    class Fault internal constructor(
        val kind: L2FaultKind,
    ) : PlanInternStep {
        override fun toString(): String = "PlanInternStep.Fault(kind=$kind)"
    }

    companion object {
        internal fun hit(node: CanonicalPlanNode): PlanInternStep = Hit(node)
        internal fun build(handle: BuildHandle): PlanInternStep = Build(handle)
        internal fun join(handle: JoinHandle): PlanInternStep = Join(handle)
        internal fun fault(kind: L2FaultKind): PlanInternStep = Fault(kind)
    }
}

/**
 * Builder-owned publication handle.
 *
 * -----------------------------------------------------------------------------
 * REQUEST-SCOPE RULE
 * -----------------------------------------------------------------------------
 *
 * This handle represents publication authority for one build episode.
 *
 * It MUST be converged within the same request scope that issued it.
 * The handle itself intentionally does NOT retain a PlannerSession reference.
 *
 * Instead, the live request-scope session is supplied explicitly at commit time.
 */
interface BuildHandle {

    fun commit(
        localNode: CanonicalPlanNode,
        session: PlannerSession,
    ): CanonicalPlanNode

    fun abort(reason: Throwable)
}

/**
 * Non-blocking join handle returned by the adapter.
 *
 * This interface intentionally does NOT expose blocking wait primitives.
 */
interface JoinHandle {

    fun registerContinuation(
        continuation: JoinContinuation,
    ): JoinRegistrationDecision

    /**
     * Consumes the ready join result through a fresh planner session.
     *
     * -----------------------------------------------------------------------------
     * FRESH-SESSION RULE
     * -----------------------------------------------------------------------------
     *
     * The caller MUST supply a fresh or freshly-reset PlannerSession.
     *
     * Rationale:
     * - resumed work is request-level continuation, not worker-local state retention
     * - cost accounting on resumed bucket re-verification belongs to the resumed session
     * - the previously active worker-local session must already have exited through
     *   ordinary cleanup
     */
    fun consumeReadyResult(
        session: PlannerSession,
    ): JoinResumeStep

    fun cancel(reason: Throwable): Boolean

    fun deadlineNanos(): Long
}

sealed interface JoinRegistrationDecision {
    data object Registered : JoinRegistrationDecision
    data object AlreadyReady : JoinRegistrationDecision
}

fun interface JoinContinuation {
    fun resume(signal: JoinResumeSignal)
}

sealed interface JoinResumeSignal {
    data object ReadyForRestart : JoinResumeSignal
}

sealed interface JoinResumeStep {

    class Hit internal constructor(
        val node: CanonicalPlanNode,
    ) : JoinResumeStep {
        override fun toString(): String = "JoinResumeStep.Hit(node=$node)"
    }

    class Fault internal constructor(
        val kind: L2FaultKind,
    ) : JoinResumeStep {
        override fun toString(): String = "JoinResumeStep.Fault(kind=$kind)"
    }

    companion object {
        internal fun hit(node: CanonicalPlanNode): JoinResumeStep = Hit(node)
        internal fun fault(kind: L2FaultKind): JoinResumeStep = Fault(kind)
    }
}
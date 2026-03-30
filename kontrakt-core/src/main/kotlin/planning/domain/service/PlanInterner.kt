package planning.domain.service

import ir.plan.node.CanonicalPlanNode
import ir.plan.node.RawCycleBreakPayload
import ir.plan.node.RawPayloadNode
import ir.plan.signature.PlanCacheKey
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.BuildHandle
import planning.domain.port.outgoing.CanonicalPayloadSealer
import planning.domain.port.outgoing.JoinContinuation
import planning.domain.port.outgoing.JoinHandle
import planning.domain.port.outgoing.JoinRegistrationDecision
import planning.domain.port.outgoing.JoinResumeStep
import planning.domain.port.outgoing.PlanInternRepository
import planning.domain.port.outgoing.PlanInternStep
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Domain service orchestrating Tier-2 structural interning.
 *
 * -----------------------------------------------------------------------------
 * DESIGN POSITION
 * -----------------------------------------------------------------------------
 *
 * This service sits at the Domain / Port boundary.
 *
 * It consumes the adapter-owned next-step algebra:
 * - Hit
 * - Build
 * - Join
 * - Fault
 *
 * and lifts it into a domain-oriented orchestration result:
 * - Completed
 * - SuspendedOnJoin
 *
 * -----------------------------------------------------------------------------
 * WHY THIS FILE KEEPS TWO PUBLIC ENTRYPOINTS
 * -----------------------------------------------------------------------------
 *
 * The repository port is intentionally payload-agnostic.
 * It knows only:
 * - PartitionId
 * - PlanCacheKey
 * - PlannerSession
 * - interning next-step semantics
 *
 * It must NOT know whether the caller is sealing:
 * - RawPayloadNode
 * - RawCycleBreakPayload
 *
 * That distinction belongs here because the domain service owns:
 * - canonical sealing entrypoint choice
 * - fallback replay safety
 * - fault-degrade policy
 *
 * Therefore this service intentionally keeps:
 * - resolve(...)
 * - resolveCycleBreak(...)
 *
 * while its internal execution is unified through ReplaySafeCanonicalFallbackPlan.
 *
 * -----------------------------------------------------------------------------
 * CRITICAL SAFETY RULE
 * -----------------------------------------------------------------------------
 *
 * A suspended join may outlive the current worker-local planner session.
 *
 * Therefore PendingJoin MUST NOT retain:
 * - PlannerSession references
 * - worker-local primitive arrays
 * - frame-stack state
 * - arbitrary executable closures that may capture mutable request/session state
 *
 * For that reason this service accepts already-materialized immutable raw payload
 * values at the public boundary and stores only replay-safe fallback-plan values
 * across suspension.
 *
 * -----------------------------------------------------------------------------
 * IMPORTANT API DECISION
 * -----------------------------------------------------------------------------
 *
 * This final form intentionally does NOT accept:
 * - builder: () -> RawPayloadNode
 * - builder: () -> RawCycleBreakPayload
 *
 * The reason is consistency and suspension safety:
 * - lambdas/closures may capture worker-local or request-local mutable state
 * - immutable raw protocol payload values do not
 *
 * RawPayloadNode itself is not the problem.
 * Closure retention is the problem.
 */
class PlanInterner private constructor(
    private val repository: PlanInternRepository,
    private val sealer: CanonicalPayloadSealer,
) {

    /**
     * Resolves an ordinary raw payload through Tier-2 interning.
     *
     * The caller supplies an already-materialized immutable raw payload value.
     * This value is immediately wrapped into a replay-safe fallback plan.
     */
    fun resolve(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
        rawPayload: RawPayloadNode,
    ): InternerStepResult {
        return resolveInternal(
            partitionId = partitionId,
            key = key,
            session = session,
            fallbackPlan = ReplaySafeCanonicalFallbackPlan.RawPayload.issue(rawPayload),
        )
    }

    /**
     * Resolves a deterministic cycle-break raw payload through Tier-2 interning.
     *
     * This remains a distinct public entry because cycle-break sealing uses a
     * different canonical sealing path from ordinary payload sealing.
     */
    fun resolveCycleBreak(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
        rawPayload: RawCycleBreakPayload,
    ): InternerStepResult {
        return resolveInternal(
            partitionId = partitionId,
            key = key,
            session = session,
            fallbackPlan = ReplaySafeCanonicalFallbackPlan.CycleBreak.issue(rawPayload),
        )
    }

    /**
     * Resumes a previously suspended join through a fresh planner session.
     *
     * The caller MUST supply a fresh or freshly-reset PlannerSession.
     * This method intentionally does not reuse stale worker-local state.
     */
    fun resume(
        pendingJoin: PendingJoin,
        session: PlannerSession,
    ): InternerStepResult.Completed {
        return pendingJoin.resume(
            session = session,
            sealer = sealer,
        )
    }

    /**
     * Internal unified interning core.
     *
     * Public facade methods remain payload-typed.
     * Internal suspension-safe logic is unified here.
     */
    private fun resolveInternal(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
        fallbackPlan: ReplaySafeCanonicalFallbackPlan,
    ): InternerStepResult {
        if (session.isL2Bypassed()) {
            session.step(CostCenter.L2_BYPASS_READ)
            return InternerStepResult.Completed(fallbackPlan.build(sealer))
        }

        return when (val step = repository.resolveOrIntern(partitionId, key, session)) {
            is PlanInternStep.Hit -> {
                session.step(CostCenter.L2_HIT)
                InternerStepResult.Completed(step.node)
            }

            is PlanInternStep.Build -> {
                InternerStepResult.Completed(
                    completeBuild(
                        handle = step.handle,
                        session = session,
                        fallbackPlan = fallbackPlan,
                    )
                )
            }

            is PlanInternStep.Join -> {
                InternerStepResult.SuspendedOnJoin(
                    PendingJoin.issue(
                        handle = step.handle,
                        fallbackPlan = fallbackPlan,
                    )
                )
            }

            is PlanInternStep.Fault -> {
                InternerStepResult.Completed(
                    completeFaultFallback(
                        kind = step.kind,
                        session = session,
                        fallbackPlan = fallbackPlan,
                    )
                )
            }
        }
    }

    /**
     * Domain-owned build completion path.
     *
     * The caller supplies the current live request-scope session to commit().
     * The BuildHandle itself does not retain session state.
     */
    private fun completeBuild(
        handle: BuildHandle,
        session: PlannerSession,
        fallbackPlan: ReplaySafeCanonicalFallbackPlan,
    ): CanonicalPlanNode {
        val canonical = fallbackPlan.build(sealer)
        return try {
            handle.commit(
                localNode = canonical,
                session = session,
            )
        } catch (t: Throwable) {
            handle.abort(t)
            throw t
        }
    }

    /**
     * Domain-owned degrade/bypass policy for adapter fault signals.
     *
     * TRANSIENT:
     * - degrade to local canonical build
     *
     * CIRCUIT_OPEN:
     * - mark session-remainder bypass
     * - degrade to local canonical build
     */
    private fun completeFaultFallback(
        kind: L2FaultKind,
        session: PlannerSession,
        fallbackPlan: ReplaySafeCanonicalFallbackPlan,
    ): CanonicalPlanNode {
        return when (kind) {
            L2FaultKind.TRANSIENT -> {
                session.step(CostCenter.L2_FAULT_TRANSIENT)
                fallbackPlan.build(sealer)
            }

            L2FaultKind.CIRCUIT_OPEN -> {
                session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                session.markL2Bypassed()
                fallbackPlan.build(sealer)
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            repository: PlanInternRepository,
            sealer: CanonicalPayloadSealer,
        ): PlanInterner {
            return PlanInterner(
                repository = repository,
                sealer = sealer,
            )
        }
    }
}

/**
 * Domain-level orchestration result returned by PlanInterner.
 *
 * This surface is intentionally minimal:
 * - Completed
 * - SuspendedOnJoin
 *
 * The runtime boundary's real concern is:
 * - did the work complete now?
 * - or must the request suspend and later restart?
 */
sealed interface InternerStepResult {

    /**
     * The interning operation completed immediately and produced the final
     * canonical winner node.
     */
    class Completed internal constructor(
        val node: CanonicalPlanNode,
    ) : InternerStepResult {
        override fun toString(): String = "InternerStepResult.Completed(node=$node)"
    }

    /**
     * The interning operation must suspend on a non-blocking joined wait.
     *
     * The runtime boundary must not block a worker.
     * It must hand this pending join to orchestration / continuation machinery.
     */
    class SuspendedOnJoin internal constructor(
        val pendingJoin: PendingJoin,
    ) : InternerStepResult {
        override fun toString(): String = "InternerStepResult.SuspendedOnJoin"
    }
}

/**
 * Suspended join episode lifted into domain/service space.
 *
 * This object retains only:
 * - JoinHandle
 * - ReplaySafeCanonicalFallbackPlan
 *
 * It deliberately retains:
 * - no lambda closure
 * - no PlannerSession reference
 * - no worker-local mutable planning state
 */
class PendingJoin private constructor(
    private val handle: JoinHandle,
    private val fallbackPlan: ReplaySafeCanonicalFallbackPlan,
) {

    /**
     * Registers a continuation for non-blocking completion delivery.
     */
    fun registerContinuation(
        continuation: JoinContinuation,
    ): JoinRegistrationDecision {
        return handle.registerContinuation(continuation)
    }

    /**
     * Exposes the monotonic waiter deadline for orchestration / diagnostics.
     */
    fun deadlineNanos(): Long = handle.deadlineNanos()

    /**
     * Requests waiter-local cancellation.
     */
    fun cancel(reason: Throwable): Boolean = handle.cancel(reason)

    /**
     * Resumes through a fresh planner session.
     *
     * The supplied session is the session that pays resumed work accounting.
     * No stale pre-suspension session is reused here.
     */
    fun resume(
        session: PlannerSession,
        sealer: CanonicalPayloadSealer,
    ): InternerStepResult.Completed {
        return when (val resumed = handle.consumeReadyResult(session)) {
            is JoinResumeStep.Hit -> {
                session.step(CostCenter.L2_HIT)
                InternerStepResult.Completed(resumed.node)
            }

            is JoinResumeStep.Fault -> {
                val node = when (resumed.kind) {
                    L2FaultKind.TRANSIENT -> {
                        session.step(CostCenter.L2_FAULT_TRANSIENT)
                        fallbackPlan.build(sealer)
                    }

                    L2FaultKind.CIRCUIT_OPEN -> {
                        session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                        session.markL2Bypassed()
                        fallbackPlan.build(sealer)
                    }
                }
                InternerStepResult.Completed(node)
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            handle: JoinHandle,
            fallbackPlan: ReplaySafeCanonicalFallbackPlan,
        ): PendingJoin {
            return PendingJoin(
                handle = handle,
                fallbackPlan = fallbackPlan,
            )
        }
    }
}

/**
 * Immutable replay-safe fallback plan.
 *
 * -----------------------------------------------------------------------------
 * WHY THIS EXISTS
 * -----------------------------------------------------------------------------
 *
 * A suspended join may later resume in a fresh planner session and still need to:
 * - degrade on TRANSIENT
 * - degrade and mark bypass on CIRCUIT_OPEN
 *
 * We must therefore retain enough immutable information to rebuild the canonical
 * local fallback deterministically, without retaining arbitrary executable closures.
 *
 * This object stores only immutable raw protocol payload.
 */
sealed interface ReplaySafeCanonicalFallbackPlan {

    /**
     * Builds the canonical node from immutable replay-safe raw payload.
     */
    fun build(sealer: CanonicalPayloadSealer): CanonicalPlanNode

    /**
     * Replay-safe ordinary payload fallback.
     */
    class RawPayload private constructor(
        private val payload: RawPayloadNode,
    ) : ReplaySafeCanonicalFallbackPlan {
        override fun build(sealer: CanonicalPayloadSealer): CanonicalPlanNode {
            return sealer.seal(payload)
        }

        companion object {
            @JvmStatic
            fun issue(
                payload: RawPayloadNode,
            ): RawPayload {
                return RawPayload(payload)
            }
        }
    }

    /**
     * Replay-safe cycle-break payload fallback.
     */
    class CycleBreak private constructor(
        private val payload: RawCycleBreakPayload,
    ) : ReplaySafeCanonicalFallbackPlan {
        override fun build(sealer: CanonicalPayloadSealer): CanonicalPlanNode {
            return sealer.sealCycleBreak(payload)
        }

        companion object {
            @JvmStatic
            fun issue(
                payload: RawCycleBreakPayload,
            ): CycleBreak {
                return CycleBreak(payload)
            }
        }
    }
}
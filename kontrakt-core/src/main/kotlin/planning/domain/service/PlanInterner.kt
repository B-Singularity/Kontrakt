package planning.domain.service

import ir.plan.node.CanonicalPlanNode
import ir.plan.node.RawCycleBreakPayload
import ir.plan.node.RawPayloadNode
import ir.plan.signature.PlanCacheKey
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.CanonicalPayloadSealer
import planning.domain.port.outgoing.PlanInternRepository
import planning.domain.port.outgoing.PlanInternResult
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Domain service orchestrating Tier-2 canonical interning.
 *
 * Final-form contract:
 * - generic passive assembly uses RawPayloadNode
 * - cycle truncation uses RawCycleBreakPayload
 * - canonical sealing remains centralized inside this boundary
 */
class PlanInterner private constructor(
    private val repository: PlanInternRepository,
    private val sealer: CanonicalPayloadSealer,
) {
    fun resolve(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
        builder: () -> RawPayloadNode,
    ): CanonicalPlanNode {
        if (session.isL2Bypassed()) {
            session.step(CostCenter.L2_BYPASS_READ)
            return sealer.seal(builder())
        }

        return when (val result = repository.resolveOrIntern(partitionId, key, session)) {
            is PlanInternResult.Hit -> {
                session.step(CostCenter.L2_HIT)
                result.node
            }

            is PlanInternResult.Miss -> {
                val raw = builder()
                val canonical = sealer.seal(raw)
                try {
                    result.handle.commit(canonical)
                } catch (t: Throwable) {
                    result.handle.abort(t)
                    throw t
                }
            }

            is PlanInternResult.Fault -> {
                when (result.kind) {
                    L2FaultKind.TRANSIENT -> {
                        session.step(CostCenter.L2_FAULT_TRANSIENT)
                        sealer.seal(builder())
                    }

                    L2FaultKind.CIRCUIT_OPEN -> {
                        session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                        session.markL2Bypassed()
                        sealer.seal(builder())
                    }
                }
            }
        }
    }

    fun resolveCycleBreak(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
        builder: () -> RawCycleBreakPayload,
    ): CanonicalPlanNode {
        if (session.isL2Bypassed()) {
            session.step(CostCenter.L2_BYPASS_READ)
            return sealer.sealCycleBreak(builder())
        }

        return when (val result = repository.resolveOrIntern(partitionId, key, session)) {
            is PlanInternResult.Hit -> {
                session.step(CostCenter.L2_HIT)
                result.node
            }

            is PlanInternResult.Miss -> {
                val raw = builder()
                val canonical = sealer.sealCycleBreak(raw)
                try {
                    result.handle.commit(canonical)
                } catch (t: Throwable) {
                    result.handle.abort(t)
                    throw t
                }
            }

            is PlanInternResult.Fault -> {
                when (result.kind) {
                    L2FaultKind.TRANSIENT -> {
                        session.step(CostCenter.L2_FAULT_TRANSIENT)
                        sealer.sealCycleBreak(builder())
                    }

                    L2FaultKind.CIRCUIT_OPEN -> {
                        session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                        session.markL2Bypassed()
                        sealer.sealCycleBreak(builder())
                    }
                }
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
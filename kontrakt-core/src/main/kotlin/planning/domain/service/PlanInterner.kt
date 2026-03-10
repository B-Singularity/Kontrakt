package planning.domain.service

import ir.plan.node.CanonicalPlanNode
import ir.plan.signature.PlanCacheKey
import planning.domain.fault.L2FaultKind
import planning.domain.port.outgoing.PlanInternRepository
import planning.domain.port.outgoing.PlanInternResult
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Domain service orchestrating Tier-2 interning.
 *
 * Important:
 * - the outbound port contract is currently [CanonicalPlanNode]-based
 * - therefore this service MUST remain [CanonicalPlanNode]-based as well
 * - runtime wrappers such as CommittedPlanNode are assembled outside Tier-2 interning
 */
class PlanInterner private constructor(
    private val repository: PlanInternRepository,
) {

    fun resolve(
        partitionId: PartitionId,
        key: PlanCacheKey,
        session: PlannerSession,
        builder: () -> CanonicalPlanNode,
    ): CanonicalPlanNode {
        if (session.isL2Bypassed()) {
            session.step(CostCenter.L2_BYPASS_READ)
            return builder()
        }

        return when (val result = repository.resolveOrIntern(partitionId, key, session)) {
            is PlanInternResult.Hit -> {
                session.step(CostCenter.L2_HIT)
                result.node
            }

            is PlanInternResult.Miss -> {
                val local = builder()
                try {
                    result.handle.commit(local)
                } catch (t: Throwable) {
                    result.handle.abort(t)
                    throw t
                }
            }

            is PlanInternResult.Fault -> {
                when (result.kind) {
                    L2FaultKind.TRANSIENT -> {
                        session.step(CostCenter.L2_FAULT_TRANSIENT)
                        builder()
                    }

                    L2FaultKind.CIRCUIT_OPEN -> {
                        session.step(CostCenter.L2_FAULT_CIRCUIT_OPEN)
                        session.markL2Bypassed()
                        builder()
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            repository: PlanInternRepository,
        ): PlanInterner {
            return PlanInterner(repository)
        }
    }
}
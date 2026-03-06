package planning.domain.session

import planning.domain.exception.FuelExhaustedException
import planning.domain.protocol.BudgetTrack
import planning.domain.protocol.CostCenter


/**
 * [Aggregate Root] L1 Planner Session.
 *
 * Owns worker-local L1 primitives:
 * - [L1Structures]
 * - [NodeIdIndexer]
 *
 * Guarantees:
 * 1) step(center) is the single mutation gate for cost metering.
 * 2) Track semantics are SSOT in CostCenter.track.
 * 3) Zero-residue: close() resets worker-local primitives (O(delta) where applicable).
 * 4) Budgets are enforced fail-closed by throwing [FuelExhaustedException].
 *
 * Usage (mandatory):
 * - PlannerSession(config).use { it.startSession(); ... }
 */
class PlannerSession(val config: PlannerSessionConfig) : SessionKernel, AutoCloseable {
    internal val structures = L1Structures(config)
    internal val indexer = NodeIdIndexer(config)

    private val kernel: SessionKernel = this

    // Monotonic totals (never decrease)
    private var cumulativePhysicalSteps: Long = 0L
    private var cumulativeSemanticWork: Long = 0L

    // Per-session baselines
    private var sessionStartPhysical: Long = 0L
    private var sessionStartSemantic: Long = 0L

    var isAborted: Boolean = false
        private set

    /**
     * Must be called before starting a planning operation.
     *
     * Establishes per-session baselines for both physical and semantic budgets.
     */
    fun startSession() {
        sessionStartPhysical = cumulativePhysicalSteps
        sessionStartSemantic = cumulativeSemanticWork
        isAborted = false
    }

    override fun step(center: CostCenter) {
        // Physical increments for all centers (both tracks).
        cumulativePhysicalSteps++
        val physicalUsed = cumulativePhysicalSteps - sessionStartPhysical
        if (physicalUsed > config.maxPhysicalSteps.toLong()) {
            abort("Physical budget exhausted ($physicalUsed steps)")
        }

        // Semantic increments only for SEMANTIC_ALSO, enforced per-session baseline.
        if (center.track == BudgetTrack.SEMANTIC_ALSO) {
            cumulativeSemanticWork++
            val semanticUsed = cumulativeSemanticWork - sessionStartSemantic
            if (semanticUsed > config.maxSemanticWorkUnits.toLong()) {
                abort("Semantic budget exhausted ($semanticUsed units)")
            }
        }
    }

    override fun onNodeAllocated(nodeId: Int) {
        structures.initNode(nodeId)
    }

    /**
     * Bridge method used by TransactionalFrame to trigger rollback metering.
     */
    fun performIndexerRollback(ptr: Int) {
        indexer.rollback(ptr, kernel)
    }

    private fun abort(reason: String): Nothing {
        isAborted = true
        throw FuelExhaustedException(reason)
    }

    /**
     * Zero-residue cleanup for worker-local primitives.
     *
     * Note: budget totals remain monotonic for forensic/trace stability.
     */
    private fun resetToCleanState() {
        structures.reset()
        indexer.reset(kernel)
    }

    override fun close() {
        resetToCleanState()
    }
}
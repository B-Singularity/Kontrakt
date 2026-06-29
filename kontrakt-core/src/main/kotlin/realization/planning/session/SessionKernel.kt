package realization.planning.session

import governance.budget.CostCenter

/**
 * [Kernel Port] Minimal internal callback surface for L1 primitives.
 *
 * Why this exists:
 * - L1 primitives (e.g., NodeIdIndexer) must be *pure primitives* and MUST NOT depend on
 *   the aggregate root type (PlannerSession) directly.
 * - This prevents DDD boundary contamination while still enforcing budget governance.
 *
 * Contract:
 * 1) [step] is the only legal gate for cost metering.
 * 2) [onNodeAllocated] is a strict lifecycle hook for 0-based dense-array reuse safety.
 *
 * Important:
 * - This is NOT an application-level port. It is an internal kernel abstraction between
 *   an Aggregate Root and its worker-local primitives.
 */
interface SessionKernel {
    /**
     * Record one atomic cost unit.
     *
     * The implementation MUST enforce budgets (physical and semantic as defined by CostCenter.track),
     * and MUST fail-closed (throw) when budgets are exhausted.
     */
    fun step(center: CostCenter)

    /**
     * Called strictly when a NEW node ID is allocated by the indexer.
     *
     * Purpose:
     * - Dense arrays are reused across epochs with 0-based IDs.
     * - Without mandatory initialization at allocation time, stale state from a previous
     *   session epoch can leak into the current run.
     *
     * The implementation MUST initialize all dense slots associated with [nodeId] in O(1).
     */
    fun onNodeAllocated(nodeId: Int)
}

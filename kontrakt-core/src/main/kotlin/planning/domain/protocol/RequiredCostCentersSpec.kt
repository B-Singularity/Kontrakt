package planning.domain.protocol

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Single source of truth for planner budget-track semantics.
 *
 * The planner meters two independent notions of cost:
 *
 * - [PHYSICAL_ONLY]
 *   Raw runtime work, probing, scanning, coordination, and governance overhead.
 *   This is primarily a throughput / DoS-protection track.
 *
 * - [SEMANTIC_ALSO]
 *   Operations that are part of semantic planner progress and therefore also
 *   contribute to the semantic-work budget in addition to the physical budget.
 *
 * Constitutional rule:
 * - PlannerSession.step(center) is the only legal mutation gate for runtime metering.
 * - No downstream component may reinterpret these track assignments.
 */
enum class BudgetTrack {
    PHYSICAL_ONLY,
    SEMANTIC_ALSO,
}

/**
 * Stable protocol banding for CostCenter identifiers.
 *
 * Banding is a protocol constraint, not a cosmetic grouping.
 * It prevents accidental ID reuse and provides coarse-grained auditability.
 */
enum class CostCenterBand(
    val minInclusive: Int,
    val maxInclusive: Int,
) {
    L1_SESSION(1, 99),
    GRAPH(100, 199),
    L2_CACHE(200, 299),
    ;

    fun contains(id: Int): Boolean = id in minInclusive..maxInclusive
}

/**
 * Required CostCenter protocol set.
 *
 * Design rules:
 * 1) IDs are stable protocol values. Renumbering is forbidden.
 * 2) ID 0 is reserved and may never be assigned.
 * 3) Each ID must belong to its declared [CostCenterBand].
 * 4) Track mapping is fixed here and is SSOT.
 * 5) Wave 1 authority already treats L2 join/wait metering as event-based waiter
 *    lifecycle accounting, not polling-tick accounting.
 */
enum class CostCenter(
    val id: Int,
    val band: CostCenterBand,
    val track: BudgetTrack,
) {
    // -------------------------------------------------------------------------
    // L1 Session (1..99)
    // -------------------------------------------------------------------------

    /**
     * One high-level dispatch into a planning frame / entrypoint.
     */
    FRAME_DISPATCH(1, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Phase-1 route for NodeIdIndexer: hash / initial slot selection.
     */
    NODEID_PHASE1_ROUTE(2, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One open-addressing probe-slot inspection inside NodeIdIndexer.
     *
     * Note:
     * Some documents may call this a "tick" rather than a "step".
     * The protocol invariant is one step() charge per inspected probe slot.
     */
    NODEID_PROBE_STEP(3, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One candidate canonical-signature comparison in NodeIdIndexer phase 2.
     */
    NODEID_PHASE2_SCAN(4, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Allocation of a fresh dense node id.
     *
     * This is semantic progress, so it contributes to both tracks.
     */
    NODEID_ALLOCATE(5, CostCenterBand.L1_SESSION, BudgetTrack.SEMANTIC_ALSO),

    /**
     * Epoch-wrap / stamp-clear reset path for NodeIdIndexer.
     */
    NODEID_RESET_EPOCH(6, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One transactional undo replay step during rollback.
     *
     * Rollback work is not free.
     */
    NODEID_ROLLBACK_STEP(7, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    // -------------------------------------------------------------------------
    // Graph Ops (100..199)
    // -------------------------------------------------------------------------

    /**
     * One semantic graph-expansion step.
     */
    EDGE_EXPAND(101, CostCenterBand.GRAPH, BudgetTrack.SEMANTIC_ALSO),

    /**
     * One RMQ push/update step.
     */
    RMQ_PUSH_UPDATE(102, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One RMQ query step.
     */
    RMQ_QUERY(103, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    // -------------------------------------------------------------------------
    // L2 Cache / Governance (200..299)
    // -------------------------------------------------------------------------

    /**
     * Materialize deterministic plan-cache-key input.
     */
    PLAN_CACHE_KEY_MATERIALIZE(200, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Materialize canonical-signature routing input.
     */
    CANONICAL_SIGNATURE_MATERIALIZE(201, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Deterministic shard routing.
     */
    L2_SHARD_ROUTE(202, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Partition / region lookup.
     */
    L2_REGION_LOOKUP(203, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Acquire or create an in-flight shared slot for a routing key.
     */
    L2_INFLIGHT_ACQUIRE(204, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Two-phase exact-match scan of a bucket / candidate list.
     */
    L2_BUCKET_SCAN(205, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Linearizable publication via put-if-absent or equivalent.
     *
     * This is marked semantic because canonical interning changes output sharing /
     * reuse topology at the semantic planner boundary.
     */
    L2_PUBLISH_PUT_IF_ABSENT(206, CostCenterBand.L2_CACHE, BudgetTrack.SEMANTIC_ALSO),

    /**
     * Transient Tier-2 fault accounting.
     *
     * Domain reaction degrades to miss / bypass-safe behavior.
     */
    L2_FAULT_TRANSIENT(207, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Circuit-open Tier-2 fault accounting.
     *
     * Domain reaction bypasses L2 for the remainder of the session.
     */
    L2_FAULT_CIRCUIT_OPEN(208, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Tier-2 hit accounting.
     */
    L2_HIT(209, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Bypass-read accounting when policy chooses not to use Tier-2.
     */
    L2_BYPASS_READ(210, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Pre-screened routing-table get before bucket scan / in-flight coordination.
     */
    L2_PRE_SCREEN_GET(211, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Waiter attach / slot-admission event.
     *
     * This replaces polling-era wait charging as the normative accounting unit.
     */
    L2_INFLIGHT_ATTACH(212, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Waiter resumption event after successful or exceptional slot completion.
     */
    L2_INFLIGHT_RESUME(213, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Waiter timeout event on monotonic elapsed-time deadline expiration.
     *
     * Timeout is a waiter lifecycle event only.
     * It MUST NOT fail the shared slot.
     */
    L2_INFLIGHT_TIMEOUT(214, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Speculative-builder quota exhaustion event.
     *
     * Used when a timed-out waiter cannot be promoted because the per-key quota
     * is already exhausted.
     */
    L2_INFLIGHT_QUOTA_EXHAUST(215, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Waiter cancellation cleanup event.
     *
     * Cancellation is waiter-local and MUST NOT cancel the shared slot.
     */
    L2_INFLIGHT_CANCEL(216, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Capacity-governance checkpoint for Tier-2 survival policy.
     */
    L2_CAPACITY_CHECK(217, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Circuit-open state transition.
     */
    L2_CIRCUIT_OPEN_TRANSITION(218, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    ;

    companion object {
        private const val MAX_ID: Int = 299

        /**
         * Dense decode table from protocol ID to CostCenter.
         *
         * ID 0 remains permanently reserved.
         */
        private val BY_ID: Array<CostCenter?> = arrayOfNulls(MAX_ID + 1)

        init {
            val all = values()

            if (all.any { it.id == 0 }) {
                throw PlanningProtocolIntegrityException("CostCenter ID 0 is RESERVED.")
            }

            for (cc in all) {
                if (cc.id !in 1..MAX_ID) {
                    throw PlanningProtocolIntegrityException(
                        "CostCenter ID out of bounds: name=${cc.name}, id=${cc.id}"
                    )
                }
                if (!cc.band.contains(cc.id)) {
                    throw PlanningProtocolIntegrityException(
                        "CostCenter band violation: name=${cc.name}, id=${cc.id}, band=${cc.band}"
                    )
                }
            }

            for (cc in all) {
                if (BY_ID[cc.id] != null) {
                    throw PlanningProtocolIntegrityException("Duplicate CostCenter ID: ${cc.id}")
                }
                BY_ID[cc.id] = cc
            }
        }

        @JvmStatic
        fun fromId(id: Int): CostCenter {
            if (id <= 0 || id >= BY_ID.size) {
                throw PlanningProtocolIntegrityException("Unknown CostCenter ID: $id")
            }
            return BY_ID[id]
                ?: throw PlanningProtocolIntegrityException("Unknown CostCenter ID: $id")
        }
    }
}
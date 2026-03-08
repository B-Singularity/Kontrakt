package planning.domain.protocol

import planning.domain.exception.PlanningProtocolIntegrityException


/**
 * [SSOT] Dual-Track Budgeting Protocol.
 *
 * The planner measures two independent notions of cost:
 *
 * - [PHYSICAL_ONLY]
 *   Counts raw compute / probing / polling / scanning work.
 *   This is DoS protection and throughput governance.
 *
 * - [SEMANTIC_ALSO]
 *   Counts operations that can change the deterministic output shape,
 *   not merely the amount of work performed.
 *
 * Important invariant:
 * - PlannerSession.step(center) MUST be the single mutation gate for both tracks.
 * - Track assignment is SSOT here; no other place may reinterpret cost semantics.
 */
enum class BudgetTrack {
    PHYSICAL_ONLY,
    SEMANTIC_ALSO,
}

/**
 * CostCenter ID banding.
 *
 * Banding is a protocol constraint, not a cosmetic choice.
 * It prevents accidental ID reuse and provides coarse-grained grouping.
 */
enum class CostCenterBand(val minInclusive: Int, val maxInclusive: Int) {
    L1_SESSION(1, 99),
    GRAPH(100, 199),
    L2_CACHE(200, 299),
    ;

    fun contains(id: Int): Boolean = id in minInclusive..maxInclusive
}

/**
 * [SSOT] Required Cost Centers (minimum passable set).
 *
 * Design rules:
 * 1) IDs are stable protocol values. Renumbering is forbidden.
 * 2) ID=0 is reserved.
 * 3) Each ID must belong to its declared [CostCenterBand].
 * 4) Track mapping is fixed here and is the single source of truth.
 */
enum class CostCenter(
    val id: Int,
    val band: CostCenterBand,
    val track: BudgetTrack,
) {
    // --- L1 Session (1..99) ---

    /**
     * One high-level dispatch into a planning frame / entrypoint.
     */
    FRAME_DISPATCH(1, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Phase-1 route for NodeIdIndexer: hashing / initial slot selection.
     */
    NODEID_PHASE1_ROUTE(2, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One slot probe in the NodeIdIndexer open-addressing table.
     */
    NODEID_PROBE_STEP(3, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One candidate signature comparison in Phase 2.
     */
    NODEID_PHASE2_SCAN(4, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Allocation of a new node ID.
     */
    NODEID_ALLOCATE(5, CostCenterBand.L1_SESSION, BudgetTrack.SEMANTIC_ALSO),

    /**
     * O(N) epoch reset / overflow handling for NodeIdIndexer stamp array.
     */
    NODEID_RESET_EPOCH(6, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One undo-record rollback step for transactional backtracking.
     */
    NODEID_ROLLBACK_STEP(7, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    // --- Graph Ops (100..199) ---

    /**
     * One semantic graph-expansion step.
     */
    EDGE_EXPAND(101, CostCenterBand.GRAPH, BudgetTrack.SEMANTIC_ALSO),

    /**
     * RMQ push/update step.
     */
    RMQ_PUSH_UPDATE(102, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    /**
     * RMQ query step.
     */
    RMQ_QUERY(103, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    // --- L2 Cache (200..299) ---

    /**
     * Materialize deterministic route/signature inputs for the plan-cache key.
     */
    PLAN_CACHE_KEY_MATERIALIZE(200, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Materialize canonical-signature representation for routing input.
     */
    CANONICAL_SIGNATURE_MATERIALIZE(201, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Deterministic shard routing.
     */
    L2_SHARD_ROUTE(202, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Lookup partition / region container.
     */
    L2_REGION_LOOKUP(203, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Acquire or create an in-flight slot for a routing key.
     */
    L2_INFLIGHT_ACQUIRE(204, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Scan a bucket / candidate list for an exact match.
     */
    L2_BUCKET_SCAN(205, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Publish / commit a put-if-absent result into shared state.
     *
     * Marked SEMANTIC_ALSO because canonical interning can change output sharing topology.
     */
    L2_PUBLISH_PUT_IF_ABSENT(206, CostCenterBand.L2_CACHE, BudgetTrack.SEMANTIC_ALSO),

    /**
     * Record or return a transient cache fault.
     */
    L2_FAULT_TRANSIENT(207, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Record or return a circuit-open cache fault.
     */
    L2_FAULT_CIRCUIT_OPEN(208, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Cache hit accounting.
     */
    L2_HIT(209, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Bypass read accounting when policy chooses not to use Tier-2.
     */
    L2_BYPASS_READ(210, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One pre-screened routing-table get attempt before in-flight coordination.
     */
    L2_PRE_SCREEN_GET(211, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One bounded in-flight wait / poll step.
     */
    L2_INFLIGHT_WAIT(212, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One capacity-governance checkpoint.
     */
    L2_CAPACITY_CHECK(213, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One circuit-open state transition.
     */
    L2_CIRCUIT_OPEN_TRANSITION(214, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    ;

    companion object {
        private const val MAX_ID: Int = 299

        /**
         * Dense decode table from protocol ID -> CostCenter.
         */
        @JvmField
        val BY_ID: Array<CostCenter?> = arrayOfNulls(MAX_ID + 1)

        init {
            val all = values()

            if (all.any { it.id == 0 }) {
                throw PlanningProtocolIntegrityException("ID 0 is RESERVED.")
            }

            for (cc in all) {
                if (cc.id !in 0..MAX_ID) {
                    throw PlanningProtocolIntegrityException("ID out of bounds: ${cc.id}")
                }
                if (!cc.band.contains(cc.id)) {
                    throw PlanningProtocolIntegrityException("ID band violation: ${cc.name}")
                }
            }

            for (cc in all) {
                if (BY_ID[cc.id] != null) {
                    throw PlanningProtocolIntegrityException("Duplicate ID: ${cc.id}")
                }
                BY_ID[cc.id] = cc
            }
        }

        /**
         * Decode protocol integer ID into [CostCenter].
         */
        @JvmStatic
        fun fromId(id: Int): CostCenter {
            if (id <= 0 || id >= BY_ID.size) {
                throw PlanningProtocolIntegrityException("Unknown ID: $id")
            }
            return BY_ID[id] ?: throw PlanningProtocolIntegrityException("Unknown ID: $id")
        }
    }
}
package planning.domain.protocol

import planning.domain.exception.PlanningProtocolIntegrityException


/**
 * [SSOT] Dual-Track Budgeting Protocol.
 *
 * The planner measures two independent notions of "cost":
 *
 * - [PHYSICAL_ONLY]
 *   Counts raw compute / probing / polling / scanning work.
 *   This is DoS protection and throughput governance.
 *
 * - [SEMANTIC_ALSO]
 *   Counts operations that can change the deterministic *output shape* (topology),
 *   not just the work performed.
 *
 * Why dual-track:
 * - A cache hit can reduce physical work but MUST NOT change semantic output.
 * - A bypass / circuit-open can reduce physical work but MUST preserve semantic invariants.
 *
 * Important invariant:
 * - [PlannerSession.step] MUST be the single mutation gate for both tracks.
 * - Track assignment is SSOT here; no other place is allowed to "reinterpret" cost semantics.
 */
enum class BudgetTrack {
    PHYSICAL_ONLY,
    SEMANTIC_ALSO
}

/**
 * CostCenter ID banding.
 *
 * Banding is not cosmetic: it is a protocol constraint that prevents accidental ID reuse
 * and provides coarse-grained grouping for reporting / auditing.
 */
enum class CostCenterBand(val minInclusive: Int, val maxInclusive: Int) {
    L1_SESSION(1, 99),
    GRAPH(100, 199),
    L2_CACHE(200, 299);

    fun contains(id: Int): Boolean = id in minInclusive..maxInclusive
}

/**
 * [SSOT] Required Cost Centers (minimum passable set).
 *
 * A CostCenter is a *named atomic accounting unit* for planner execution.
 *
 * Design rules:
 * 1) IDs are stable protocol values. Renaming symbols is allowed, renumbering is NOT.
 * 2) ID=0 is reserved. (Used elsewhere as sentinel space; never consume it here.)
 * 3) Each ID must belong to its declared [CostCenterBand].
 * 4) Track mapping is fixed here and is the single source of truth.
 *
 * Tick-level semantics:
 * - "PROBE_STEP / WAIT / SCAN" style centers represent a single atomic unit of work.
 *   For example, `NODEID_PROBE_STEP` is exactly one slot probe in open addressing.
 */
enum class CostCenter(
    val id: Int,
    val band: CostCenterBand,
    val track: BudgetTrack
) {
    // --- L1 Session (1..99) ---
    /**
     * One high-level dispatch into a planning frame / entrypoint.
     *
     * Note: This is not meant to be "per user call" necessarily, but rather a single
     * top-level scheduling step in the L1 session.
     */
    FRAME_DISPATCH(1, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Phase-1 route for NodeIdIndexer: hashing / initial slot selection.
     */
    NODEID_PHASE1_ROUTE(2, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One slot probe in NodeIdIndexer open addressing table.
     *
     * This is the canonical DoS/throughput metric for "table pressure".
     */
    NODEID_PROBE_STEP(3, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One candidate signature comparison (Phase 2).
     *
     * IMPORTANT: This is *per-candidate*, not per-byte.
     * DoS protection against huge signatures is enforced by maxSignatureLen in the protocol.
     */
    NODEID_PHASE2_SCAN(4, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Allocation of a new node ID.
     *
     * This is marked as [BudgetTrack.SEMANTIC_ALSO] because allocating a new node ID can
     * change the deterministic output topology (e.g., new node discovered).
     */
    NODEID_ALLOCATE(5, CostCenterBand.L1_SESSION, BudgetTrack.SEMANTIC_ALSO),

    /**
     * O(N) epoch reset / overflow handling for NodeIdIndexer stamp array.
     *
     * This should be extremely rare; metered to prevent silent catastrophic work.
     */
    NODEID_RESET_EPOCH(6, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * One undo record rollback step (L1 transactional backtracking).
     */
    NODEID_ROLLBACK_STEP(7, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    // --- Graph Ops (100..199) ---
    /**
     * One semantic expansion step of the graph (e.g., follow edge / emit child).
     */
    EDGE_EXPAND(101, CostCenterBand.GRAPH, BudgetTrack.SEMANTIC_ALSO),

    /**
     * RMQ push/update step (building/maintaining RMQ structure).
     */
    RMQ_PUSH_UPDATE(102, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    /**
     * RMQ query step (answering an RMQ query).
     */
    RMQ_QUERY(103, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    // --- L2 Cache (200..299) ---
    /**
     * Materialize plan-cache routing key (bytes -> route/signature or similar).
     */
    PLAN_CACHE_KEY_MATERIALIZE(200, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Materialize canonical signature representation (bytes -> canonical form).
     */
    CANONICAL_SIGNATURE_MATERIALIZE(201, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Route to a shard (striping / partition routing).
     */
    L2_SHARD_ROUTE(202, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Lookup region/partition container.
     *
     * This is part of the minimum required accounting set for L2 governance.
     */
    L2_REGION_LOOKUP(203, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Acquire or create an in-flight slot (gate) for a key.
     */
    L2_INFLIGHT_ACQUIRE(204, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Scan a bucket / candidate list for exact match.
     */
    L2_BUCKET_SCAN(205, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Publish / commit a put-if-absent result into shared state.
     *
     * Marked SEMANTIC_ALSO because it may impact the deterministic topology/output
     * by enabling interning effects.
     */
    L2_PUBLISH_PUT_IF_ABSENT(206, CostCenterBand.L2_CACHE, BudgetTrack.SEMANTIC_ALSO),

    /**
     * Record or return a transient cache fault (e.g., contention/timeout).
     */
    L2_FAULT_TRANSIENT(207, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Record or return a circuit-open cache fault (capacity governor open).
     */
    L2_FAULT_CIRCUIT_OPEN(208, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Cache hit accounting.
     */
    L2_HIT(209, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Bypass read accounting (when policy chooses not to use cache).
     */
    L2_BYPASS_READ(210, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY);

    companion object {
        /**
         * Upper bound for CostCenter IDs.
         *
         * Protocol note: IDs must remain in a compact bounded space so that BY_ID can be
         * a dense array for fast decoding in hot paths.
         */
        private const val MAX_ID: Int = 299

        /**
         * Dense decode table from protocol ID -> CostCenter.
         *
         * This is initialized once and then treated as immutable SSOT.
         */
        @JvmField
        val BY_ID: Array<CostCenter?> = arrayOfNulls(MAX_ID + 1)

        init {
            val all = values()

            // ID 0 is reserved by protocol law.
            if (all.any { it.id == 0 }) {
                throw PlanningProtocolIntegrityException("ID 0 is RESERVED.")
            }

            // Range + band checks.
            for (cc in all) {
                if (cc.id !in 0..MAX_ID) {
                    throw PlanningProtocolIntegrityException("ID out of bounds: ${cc.id}")
                }
                if (!cc.band.contains(cc.id)) {
                    throw PlanningProtocolIntegrityException("ID band violation: ${cc.name}")
                }
            }

            // Uniqueness check.
            for (cc in all) {
                if (BY_ID[cc.id] != null) {
                    throw PlanningProtocolIntegrityException("Duplicate ID: ${cc.id}")
                }
                BY_ID[cc.id] = cc
            }
        }

        /**
         * Decode protocol integer ID into [CostCenter].
         *
         * This is used when consuming compact traces or when bridging across layers.
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
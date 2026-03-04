package planning.domain.protocol

import planning.domain.exception.PlanningProtocolDecodingException
import planning.domain.exception.PlanningProtocolIntegrityException


/**
 * [SSOT] Dual-Track Budgeting Protocol.
 *
 * The planner measures two independent notions of "cost":
 *
 * - PHYSICAL_ONLY:
 *   Counts raw compute / probing / polling / scanning work.
 *   This is DoS protection and throughput governance.
 *
 * - SEMANTIC_ALSO:
 *   Counts operations that can change the deterministic *output shape* (topology),
 *   not just the work performed.
 *
 * Why dual-track:
 * - A cache hit can reduce physical work but MUST NOT change semantic output.
 * - A bypass / circuit-open can reduce physical work but MUST preserve semantic invariants.
 */
enum class BudgetTrack {
    PHYSICAL_ONLY,
    SEMANTIC_ALSO,
}

/**
 * ID band law.
 *
 * This is enforced mechanically at class-load time:
 * - No ID=0
 * - IDs are unique
 * - IDs must stay within their band range
 */
enum class CostBand(val idRange: IntRange) {
    L1_SESSION(1..99),
    GRAPH_OPS(100..199),
    L2_OPS(200..299),
}

/**
 * [SSOT] CostCenter list.
 *
 * Rules:
 * - IDs are stable protocol surface (serialization, traces, audits).
 * - Do not reuse IDs. Do not repurpose IDs across meanings.
 * - Add new IDs only within the correct band.
 *
 * Tick semantics must be documented and stable:
 * - *_TICK entries represent the smallest accountable atomic unit.
 */
enum class CostCenter(
    val id: Int,
    val track: BudgetTrack,
    val band: CostBand,
) {
    // ─────────────────────────────────────────────────────────────
    // L1 Session Operations (1..99)
    // ─────────────────────────────────────────────────────────────

    /**
     * One planner frame dispatch / entry step.
     */
    FRAME_DISPATCH(1, BudgetTrack.PHYSICAL_ONLY, CostBand.L1_SESSION),

    /**
     * One phase-1 route decision (e.g., bucket selection / routing math).
     * This is intentionally separate from probing ticks.
     */
    PHASE1_ROUTE(4, BudgetTrack.PHYSICAL_ONLY, CostBand.L1_SESSION),

    /**
     * One open-addressing probe attempt for NodeId indexing.
     * (One slot inspection = one tick.)
     */
    NODEID_PROBE_TICK(2, BudgetTrack.PHYSICAL_ONLY, CostBand.L1_SESSION),

    /**
     * One successful NodeId assignment / commit that affects structural identity.
     */
    NODEID_ASSIGN(3, BudgetTrack.SEMANTIC_ALSO, CostBand.L1_SESSION),

    // ─────────────────────────────────────────────────────────────
    // Graph Operations (100..199)
    // ─────────────────────────────────────────────────────────────

    /**
     * One semantic edge expansion that can change topology.
     */
    EDGE_EXPAND(101, BudgetTrack.SEMANTIC_ALSO, CostBand.GRAPH_OPS),

    /**
     * One RMQ step (range-min query) or equivalent monotonic stack step.
     */
    RMQ_STEP(102, BudgetTrack.PHYSICAL_ONLY, CostBand.GRAPH_OPS),

    // ─────────────────────────────────────────────────────────────
    // L2 Operations (200..299)
    // ─────────────────────────────────────────────────────────────

    /**
     * Key materialization (bytes assembly) step.
     */
    KEY_MATERIALIZE(200, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),

    /**
     * L2 shard routing step.
     */
    L2_ROUTE(201, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),

    /**
     * One table probe tick (one slot state read) in LongKeyTable.
     */
    L2_TABLE_PROBE_TICK(202, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),

    /**
     * One waiter polling tick in an in-flight gate / future wait loop.
     */
    L2_WAIT_TICK(203, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),

    /**
     * One bucket entry scan tick inside a shard/bucket.
     */
    L2_BUCKET_SCAN_TICK(204, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),

    /**
     * One L2 hit confirmation (semantic-neutral).
     */
    L2_HIT(205, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),

    /**
     * One interning commit that affects semantic topology (canonical instance selection).
     */
    INTERN_COMMIT(206, BudgetTrack.SEMANTIC_ALSO, CostBand.L2_OPS),

    /**
     * Optional: explicit get/put accounting (useful for governance dashboards).
     */
    L2_GET(210, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),
    L2_PUT(211, BudgetTrack.PHYSICAL_ONLY, CostBand.L2_OPS),
    ;

    companion object {
        private const val RESERVED_ID = 0

        /**
         * O(1) lookup table by ID.
         *
         * Size is based on the maximum declared ID, not on band max,
         * to avoid silent acceptance of gaps beyond the protocol surface.
         */
        private val BY_ID: Array<CostCenter?>

        init {
            val all = entries

            if (all.any { it.id == RESERVED_ID }) {
                throw PlanningProtocolIntegrityException("Fatal Protocol Violation: ID 0 is RESERVED.")
            }

            val ids = all.map { it.id }
            if (ids.toSet().size != ids.size) {
                throw PlanningProtocolIntegrityException("Fatal Protocol Violation: Duplicate CostCenter IDs detected.")
            }

            for (cc in all) {
                if (cc.id !in cc.band.idRange) {
                    throw PlanningProtocolIntegrityException(
                        "Fatal Protocol Violation: CostCenter ${cc.name} has id=${cc.id} " +
                                "outside band=${cc.band} range=${cc.band.idRange}."
                    )
                }
            }

            val maxId = all.maxOf { it.id }
            BY_ID = arrayOfNulls(maxId + 1)
            for (cc in all) {
                BY_ID[cc.id] = cc
            }
        }

        /**
         * Fail-closed decode from an integer ID.
         *
         * This is used for trace decoding and protocol surfaces.
         * Unknown IDs are fatal by design.
         */
        @JvmStatic
        fun fromId(id: Int): CostCenter {
            if (id < 0 || id >= BY_ID.size) {
                throw PlanningProtocolDecodingException("Fatal Protocol Violation: Unknown CostCenter ID $id")
            }
            return BY_ID[id]
                ?: throw PlanningProtocolDecodingException("Fatal Protocol Violation: Unknown CostCenter ID $id")
        }
    }
}
package governance.budget.contract

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Single source of truth for planner budget-track semantics.
 *
 * The planner meters two independent notions of cost:
 *
 * - PHYSICAL_ONLY
 *   Raw runtime work, probing, scanning, coordination, validation, and governance overhead.
 *
 * - SEMANTIC_ALSO
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
 * Stable order banding for CostCenter identifiers.
 *
 * Banding is a order constraint.
 * It prevents accidental ID reuse and keeps cost diagnostics auditable.
 */
enum class CostCenterBand(
    val minInclusive: Int,
    val maxInclusive: Int,
) {
    L1_SESSION(1, 99),
    GRAPH(100, 199),
    L2_CACHE(200, 299),

    /**
     * Metamodel-to-planning semantic lowering.
     *
     * This band is intentionally distinct from L2 cache governance.
     */
    TYPE_EXPANSION(300, 399),
    ;

    fun contains(id: Int): Boolean = id in minInclusive..maxInclusive

    companion object {
        private val ALL: Array<CostCenterBand> = values()
        private val MAX_REGISTERED_ID: Int = computeMaxRegisteredId()

        @JvmStatic
        fun maxRegisteredId(): Int = MAX_REGISTERED_ID

        private fun computeMaxRegisteredId(): Int {
            var max = 0
            var i = 0

            while (i < ALL.size) {
                val band = ALL[i]
                if (band.maxInclusive > max) {
                    max = band.maxInclusive
                }
                i++
            }

            return max
        }
    }
}

/**
 * Required CostCenter order set.
 *
 * Design rules:
 * 1. IDs are stable order values. Renumbering is forbidden.
 * 2. ID 0 is reserved and may never be assigned.
 * 3. Each ID must belong to its declared CostCenterBand.
 * 4. Track mapping is fixed here and is SSOT.
 * 5. Type expansion is not L2 cache governance.
 * 6. ADR-0037 cycle identity preflight is physical-only.
 */
enum class CostCenter(
    val id: Int,
    val band: CostCenterBand,
    val track: BudgetTrack,
) {
    // -------------------------------------------------------------------------
    // L1 Session (1..99)
    // -------------------------------------------------------------------------

    FRAME_DISPATCH(1, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),
    NODEID_PHASE1_ROUTE(2, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),
    NODEID_PROBE_STEP(3, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),
    NODEID_PHASE2_SCAN(4, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),
    NODEID_ALLOCATE(5, CostCenterBand.L1_SESSION, BudgetTrack.SEMANTIC_ALSO),
    NODEID_RESET_EPOCH(6, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),
    NODEID_ROLLBACK_STEP(7, CostCenterBand.L1_SESSION, BudgetTrack.PHYSICAL_ONLY),

    // -------------------------------------------------------------------------
    // Graph Ops (100..199)
    // -------------------------------------------------------------------------

    EDGE_EXPAND(101, CostCenterBand.GRAPH, BudgetTrack.SEMANTIC_ALSO),
    RMQ_PUSH_UPDATE(102, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),
    RMQ_QUERY(103, CostCenterBand.GRAPH, BudgetTrack.PHYSICAL_ONLY),

    // -------------------------------------------------------------------------
    // L2 Cache / Governance (200..299)
    // -------------------------------------------------------------------------

    PLAN_CACHE_KEY_MATERIALIZE(200, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    CANONICAL_SIGNATURE_MATERIALIZE(201, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_SHARD_ROUTE(202, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_REGION_LOOKUP(203, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_INFLIGHT_ACQUIRE(204, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_BUCKET_SCAN(205, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_PUBLISH_PUT_IF_ABSENT(206, CostCenterBand.L2_CACHE, BudgetTrack.SEMANTIC_ALSO),
    L2_FAULT_TRANSIENT(207, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_FAULT_CIRCUIT_OPEN(208, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_HIT(209, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_BYPASS_READ(210, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_PRE_SCREEN_GET(211, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_INFLIGHT_ATTACH(212, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_INFLIGHT_RESUME(213, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_INFLIGHT_TIMEOUT(214, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_INFLIGHT_QUOTA_EXHAUST(215, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_INFLIGHT_CANCEL(216, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_CAPACITY_CHECK(217, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),
    L2_CIRCUIT_OPEN_TRANSITION(218, CostCenterBand.L2_CACHE, BudgetTrack.PHYSICAL_ONLY),

    // -------------------------------------------------------------------------
    // Type Expansion / Metamodel Lowering (300..399)
    // -------------------------------------------------------------------------

    TYPE_SHAPE_RESOLUTION(300, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),
    TYPE_SHAPE_LOWERING(301, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Resolve the minimal TypeCycleIdentity needed for active-cycle detection.
     */
    TYPE_CYCLE_IDENTITY_RESOLUTION(302, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Validate TypeCycleIdentity against requested TypeReference and identity-law snapshot.
     */
    TYPE_CYCLE_IDENTITY_CONTINUITY_CHECK(303, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Retrieve already-ratified raw facts from a memoized/cache surface.
     */
    COMPOSITE_RAW_FACT_CACHE_HIT(304, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),

    /**
     * Perform actual backend raw structural fact discovery/reconciliation.
     */
    COMPOSITE_RAW_FACT_RESOLVE(305, CostCenterBand.TYPE_EXPANSION, BudgetTrack.SEMANTIC_ALSO),

    COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK(
        306,
        CostCenterBand.TYPE_EXPANSION,
        BudgetTrack.PHYSICAL_ONLY,
    ),

    COMPOSITE_ACTIVE_MEMBER_PROJECTION(307, CostCenterBand.TYPE_EXPANSION, BudgetTrack.SEMANTIC_ALSO),
    COMPOSITE_ACTIVE_MEMBER_ORDERING(308, CostCenterBand.TYPE_EXPANSION, BudgetTrack.SEMANTIC_ALSO),
    CONTAINER_EXPANSION_DECISION(309, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),
    ATOMIC_EXPANSION_DECISION(310, CostCenterBand.TYPE_EXPANSION, BudgetTrack.PHYSICAL_ONLY),
    ;

    companion object {
        private val DECODE_TABLE_UPPER_BOUND: Int = CostCenterBand.maxRegisteredId()
        private val ALL: Array<CostCenter> = values()
        private val BY_ID: Array<CostCenter?> = buildDecodeTable()

        private fun buildDecodeTable(): Array<CostCenter?> {
            val table = arrayOfNulls<CostCenter>(DECODE_TABLE_UPPER_BOUND + 1)

            var i = 0
            while (i < ALL.size) {
                val center = ALL[i]

                if (center.id == 0) {
                    throw PlanningProtocolIntegrityException("CostCenter ID 0 is reserved.")
                }

                if (center.id < 0 || center.id > DECODE_TABLE_UPPER_BOUND) {
                    throw PlanningProtocolIntegrityException(
                        "CostCenter ID out of bounds: name=${center.name}, id=${center.id}, bound=$DECODE_TABLE_UPPER_BOUND",
                    )
                }

                if (!center.band.contains(center.id)) {
                    throw PlanningProtocolIntegrityException(
                        "CostCenter band violation: name=${center.name}, id=${center.id}, band=${center.band}",
                    )
                }

                if (table[center.id] != null) {
                    throw PlanningProtocolIntegrityException(
                        "Duplicate CostCenter ID: ${center.id}",
                    )
                }

                table[center.id] = center
                i++
            }

            return table
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

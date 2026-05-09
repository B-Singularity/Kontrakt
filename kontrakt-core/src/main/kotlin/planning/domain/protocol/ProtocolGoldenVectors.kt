package planning.domain.protocol

/**
 * Golden vectors for the Planning Protocol / Constitution.
 *
 * Purpose:
 * - A order is only "real" if it is pinned by concrete vectors.
 * - These vectors must remain bit-for-bit stable across all supported runtimes.
 *
 * Coverage policy (minimum passable set):
 * 1) Encoding law
 *    - UTF-8
 *    - 4-byte little-endian length prefix
 *    - strict NFC-REJECT
 *    - surrogate rejection
 *
 * 2) Hash law
 *    - MurmurHash3 x64 128-bit
 *    - h1/h2 pinned for tail lengths 0..15
 *    - multi-block boundaries
 *    - seed diversity
 *
 * 3) Sentinel law
 *    - reserved-value remap for 0L and -1L
 *    - pass-through behavior for non-reserved values
 *
 * 4) Cost-center law
 *    - order IDs are pinned here intentionally
 *    - traces represent normative atomic execution paths
 *
 * Important:
 * - This file is a order asset, not a test class.
 * - Phase 5 verification gates consume these vectors; they do not define them.
 * - Do not replace pinned order values with enum references inside this file.
 */
object ProtocolGoldenVectors {
    /**
     * Golden vector for MurmurHash3 x64 128-bit.
     *
     * Contract:
     * - [inputHex] is the raw payload in lowercase hex
     * - [seed] is the 64-bit API seed
     * - implementation consumes the low 32 bits only
     * - [expectedH1Hex] and [expectedH2Hex] are lowercase 16-hex-word outputs
     */
    class HashVector private constructor(
        val inputHex: String,
        val seed: Long,
        val expectedH1Hex: String,
        val expectedH2Hex: String,
        val description: String,
    ) {
        companion object {
            @JvmStatic
            fun issue(
                inputHex: String,
                seed: Long,
                expectedH1Hex: String,
                expectedH2Hex: String,
                description: String,
            ): HashVector =
                HashVector(
                    inputHex = inputHex,
                    seed = seed,
                    expectedH1Hex = expectedH1Hex,
                    expectedH2Hex = expectedH2Hex,
                    description = description,
                )
        }
    }

    /**
     * Golden vector for strict encoding law.
     *
     * Contract:
     * - [expectedHex] is null when the input MUST be rejected
     * - accepted format is [lenLE32][utf8Bytes]
     */
    class EncodingVector private constructor(
        val inputString: String,
        val expectedHex: String?, // null = REJECT
        val description: String,
    ) {
        companion object {
            @JvmStatic
            fun issue(
                inputString: String,
                expectedHex: String?,
                description: String,
            ): EncodingVector =
                EncodingVector(
                    inputString = inputString,
                    expectedHex = expectedHex,
                    description = description,
                )
        }
    }

    /**
     * Sentinel family under test.
     */
    enum class SentinelKind {
        NON_ZERO,
        NON_MAX,
    }

    /**
     * Golden vector for sentinel remapping law.
     *
     * Contract:
     * - [input] is the pre-remap value
     * - [seed] is the version-bound deterministic seed
     * - [expectedRemappedHex] is the expected lowercase 16-hex 64-bit result
     *
     * Notes:
     * - For pass-through vectors, [expectedRemappedHex] equals the input bit pattern.
     * - For reserved-value vectors, [expectedRemappedHex] MUST NOT be 0L or -1L.
     */
    class SentinelVector private constructor(
        val kind: SentinelKind,
        val input: Long,
        val seed: Long,
        val expectedRemappedHex: String,
        val description: String,
    ) {
        companion object {
            @JvmStatic
            fun issue(
                kind: SentinelKind,
                input: Long,
                seed: Long,
                expectedRemappedHex: String,
                description: String,
            ): SentinelVector =
                SentinelVector(
                    kind = kind,
                    input = input,
                    seed = seed,
                    expectedRemappedHex = expectedRemappedHex,
                    description = description,
                )
        }
    }

    /**
     * Golden trace for order cost-center sequencing.
     *
     * Contract:
     * - [expectedCostCenterIds] stores order IDs directly
     * - tests must assert both exact values and exact order
     *
     * Terminology note:
     * - Current implementation names:
     *   NODEID_PROBE_STEP / L2_INFLIGHT_WAIT / L2_BUCKET_SCAN
     * - Document aliases:
     *   NODEID_PROBE_TICK / L2_WAIT_TICK / L2_SCAN_TICK
     *
     * The order law is the ID sequence.
     * The alias note exists only to eliminate document/code naming drift.
     */
    class CostCenterTraceVector private constructor(
        val name: String,
        val description: String,
        val expectedCostCenterIds: List<Int>,
    ) {
        companion object {
            @JvmStatic
            fun issue(
                name: String,
                description: String,
                expectedCostCenterIds: List<Int>,
            ): CostCenterTraceVector =
                CostCenterTraceVector(
                    name = name,
                    description = description,
                    expectedCostCenterIds = expectedCostCenterIds,
                )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Hash Vectors
    // Generated from MurmurHash3_x64_128 reference semantics.
    // h1/h2 are pinned outputs of the 128-bit result.
    // Seed uses low 32 bits only.
    // ─────────────────────────────────────────────────────────────

    val HASH_VECTORS: List<HashVector> =
        listOf(
            HashVector.issue("", 0L, "0000000000000000", "0000000000000000", "Len 0 (empty)"),
            HashVector.issue("61", 0L, "85555565f6597889", "e6b53a48510e895a", "Len 1 ('a')"),
            HashVector.issue("6161", 0L, "2c91cb24366eb7a8", "6625d6db6916695c", "Len 2 ('aa')"),
            HashVector.issue("616161", 0L, "136d696c010a2af6", "8e0915e545b2bc08", "Len 3"),
            HashVector.issue("61616161", 0L, "f61cfdbfdae0f65e", "58f93db16236ba2b", "Len 4"),
            HashVector.issue("6161616161", 0L, "416badf75f54c737", "bf9054d748a3e428", "Len 5"),
            HashVector.issue("616161616161", 0L, "fbb97d784b1c59f4", "a54c211d6c1e6b1d", "Len 6"),
            HashVector.issue("61616161616161", 0L, "c6be8a493af9714a", "40948f9d17425c71", "Len 7"),
            HashVector.issue("6161616161616161", 0L, "187f343ff3b0d249", "b11e0e63e3aa0c34", "Len 8"),
            HashVector.issue("616161616161616161", 0L, "0f3ae5442d91c557", "4ce49ced1def61db", "Len 9"),
            HashVector.issue("61616161616161616161", 0L, "e1a55f48f1c10d5f", "47268a4343d49f44", "Len 10"),
            HashVector.issue("6161616161616161616161", 0L, "f4cdd514303c5382", "41baebada3d81025", "Len 11"),
            HashVector.issue("616161616161616161616161", 0L, "d8bb9d456ed6144b", "a6bc4c4bf6887b15", "Len 12"),
            HashVector.issue("61616161616161616161616161", 0L, "d52d19e503eec6a0", "ed98335c09c83689", "Len 13"),
            HashVector.issue("6161616161616161616161616161", 0L, "b0c344f01ce073be", "e19cbe0ac3a564fe", "Len 14"),
            HashVector.issue("616161616161616161616161616161", 0L, "bd9fe677c36b6240", "46c1c1f5375b2115", "Len 15"),
            HashVector.issue(
                "61616161616161616161616161616161",
                0L,
                "ec78db0c8b199e8a",
                "84cedd7dc194e391",
                "Len 16 (1 full block)",
            ),
            HashVector.issue(
                "6161616161616161616161616161616161",
                0L,
                "7e45349e0f3b13e7",
                "48dda138e8168031",
                "Len 17 (1 full block + 1 tail byte)",
            ),
            HashVector.issue(
                "61616161616161616161616161616161616161616161616161616161616161",
                0L,
                "2f1c4cde0f73a10e",
                "db1b39fc408412ba",
                "Len 31 (1 full block + 15 tail bytes)",
            ),
            HashVector.issue(
                "616161616161616161616161616161616161616161616161616161616161616161",
                0L,
                "504857b82da63359",
                "946261f5cf3e5261",
                "Len 33 (2 full blocks + 1 tail byte)",
            ),
            HashVector.issue(
                "74657374",
                12345L,
                "b3dd93fa6464603d",
                "5a22b0ce644fb688",
                "Seed=12345, input='test'",
            ),
            HashVector.issue(
                "74657374",
                -1L,
                "fbcc84705faf0762",
                "77923427b407dd8a",
                "Seed=-1 (low32=0xFFFF_FFFF), input='test'",
            ),
        )

    // ─────────────────────────────────────────────────────────────
    // Encoding Vectors
    // accepted format: [lenLE32][utf8Bytes]
    // ─────────────────────────────────────────────────────────────

    val ENCODING_VECTORS: List<EncodingVector> =
        listOf(
            EncodingVector.issue("", "00000000", "Empty string"),
            EncodingVector.issue("abc", "03000000616263", "ASCII"),
            EncodingVector.issue("a\u0000b", "03000000610062", "Contains NULL byte (allowed)"),
            EncodingVector.issue("가", "03000000eab080", "Hangul NFC"),
            EncodingVector.issue("\u1100\u1161", null, "Hangul NFD -> Reject"),
            EncodingVector.issue("é", "02000000c3a9", "Latin precomposed NFC"),
            EncodingVector.issue("e\u0301", null, "Latin decomposed NFD -> Reject"),
            EncodingVector.issue("👍", "04000000f09f918d", "Single emoji"),
            EncodingVector.issue("✈️", "06000000e29c88efb88f", "Variation selector sequence"),
            EncodingVector.issue(
                "👨‍👩‍👧‍👦",
                "19000000f09f91a8e2808df09f91a9e2808df09f91a7e2808df09f91a6",
                "ZWJ family sequence",
            ),
            EncodingVector.issue("\uD800", null, "Unpaired high surrogate -> Reject"),
            EncodingVector.issue("\uDC00", null, "Unpaired low surrogate -> Reject"),
            EncodingVector.issue(
                "a".repeat(255),
                "ff000000" + hexRepeat("61", 255),
                "255-byte ASCII payload",
            ),
            EncodingVector.issue(
                "a".repeat(256),
                "00010000" + hexRepeat("61", 256),
                "256-byte ASCII payload",
            ),
        )

    // ─────────────────────────────────────────────────────────────
    // Sentinel Vectors
    // reserved remap + non-reserved pass-through
    // ─────────────────────────────────────────────────────────────

    val SENTINEL_VECTORS: List<SentinelVector> =
        listOf(
            SentinelVector.issue(
                kind = SentinelKind.NON_ZERO,
                input = 0L,
                seed = 123L,
                expectedRemappedHex = "a52f271a264cfc93",
                description = "remapNonZero(0L, seed=123)",
            ),
            SentinelVector.issue(
                kind = SentinelKind.NON_MAX,
                input = -1L,
                seed = 456L,
                expectedRemappedHex = "a2ae2b2f6003d27d",
                description = "remapNonMax(-1L, seed=456)",
            ),
            SentinelVector.issue(
                kind = SentinelKind.NON_ZERO,
                input = 1L,
                seed = 999L,
                expectedRemappedHex = "0000000000000001",
                description = "remapNonZero(1L, seed=999) -> pass-through",
            ),
            SentinelVector.issue(
                kind = SentinelKind.NON_MAX,
                input = 42L,
                seed = 999L,
                expectedRemappedHex = "000000000000002a",
                description = "remapNonMax(42L, seed=999) -> pass-through",
            ),
        )

    // ─────────────────────────────────────────────────────────────
    // CostCenter Golden Traces
    //
    // Current order ID map:
    //   1   FRAME_DISPATCH
    //   2   NODEID_PHASE1_ROUTE
    //   3   NODEID_PROBE_STEP         (document alias: NODEID_PROBE_TICK)
    //   4   NODEID_PHASE2_SCAN
    //   5   NODEID_ALLOCATE
    //   6   NODEID_RESET_EPOCH
    //   7   NODEID_ROLLBACK_STEP
    //   101 EDGE_EXPAND
    //   102 RMQ_PUSH_UPDATE
    //   103 RMQ_QUERY
    //   200 PLAN_CACHE_KEY_MATERIALIZE
    //   201 CANONICAL_SIGNATURE_MATERIALIZE
    //   202 L2_SHARD_ROUTE
    //   203 L2_REGION_LOOKUP
    //   204 L2_INFLIGHT_ACQUIRE
    //   205 L2_BUCKET_SCAN            (document alias: L2_SCAN_TICK)
    //   206 L2_PUBLISH_PUT_IF_ABSENT
    //   207 L2_FAULT_TRANSIENT
    //   208 L2_FAULT_CIRCUIT_OPEN
    //   209 L2_HIT
    //   210 L2_BYPASS_READ
    //   211 L2_PRE_SCREEN_GET
    //   212 L2_INFLIGHT_WAIT          (document alias: L2_WAIT_TICK)
    //   213 L2_CAPACITY_CHECK
    //   214 L2_CIRCUIT_OPEN_TRANSITION
    // ─────────────────────────────────────────────────────────────

    val COST_CENTER_TRACES: List<CostCenterTraceVector> =
        listOf(
            // L1 dispatch / indexer
            CostCenterTraceVector.issue(
                name = "frame_dispatch_atomic",
                description = "Single explicit frame dispatch",
                expectedCostCenterIds = listOf(1),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_fresh_insert",
                description = "Phase-1 routing, first probe, fresh allocation into empty slot",
                expectedCostCenterIds =
                    listOf(
                        2, // NODEID_PHASE1_ROUTE
                        3, // NODEID_PROBE_STEP
                        5, // NODEID_ALLOCATE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_exact_hit_same_signature",
                description = "Existing identity bucket with exact signature match",
                expectedCostCenterIds =
                    listOf(
                        2, // NODEID_PHASE1_ROUTE
                        3, // NODEID_PROBE_STEP
                        4, // NODEID_PHASE2_SCAN
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_same_key_append_new_signature",
                description = "Existing identity bucket but no exact signature match, allocate new dense node",
                expectedCostCenterIds =
                    listOf(
                        2, // NODEID_PHASE1_ROUTE
                        3, // NODEID_PROBE_STEP
                        4, // NODEID_PHASE2_SCAN
                        5, // NODEID_ALLOCATE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_linear_probe_once_then_insert",
                description = "One foreign occupied slot is probed before insertion",
                expectedCostCenterIds =
                    listOf(
                        2, // NODEID_PHASE1_ROUTE
                        3, // NODEID_PROBE_STEP
                        3, // NODEID_PROBE_STEP
                        5, // NODEID_ALLOCATE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_linear_probe_twice_then_insert",
                description = "Two foreign occupied slots are probed before insertion",
                expectedCostCenterIds =
                    listOf(
                        2, // NODEID_PHASE1_ROUTE
                        3, // NODEID_PROBE_STEP
                        3, // NODEID_PROBE_STEP
                        3, // NODEID_PROBE_STEP
                        5, // NODEID_ALLOCATE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_reset_epoch_atomic",
                description = "Epoch overflow recovery path",
                expectedCostCenterIds =
                    listOf(
                        6, // NODEID_RESET_EPOCH
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_single_undo_step",
                description = "Single metered undo-log replay step",
                expectedCostCenterIds =
                    listOf(
                        7, // NODEID_ROLLBACK_STEP
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "nodeid_double_undo_step",
                description = "Two metered undo-log replay steps",
                expectedCostCenterIds =
                    listOf(
                        7, // NODEID_ROLLBACK_STEP
                        7, // NODEID_ROLLBACK_STEP
                    ),
            ),
            // Graph layer
            CostCenterTraceVector.issue(
                name = "graph_edge_expand_atomic",
                description = "Single semantic edge expansion step",
                expectedCostCenterIds =
                    listOf(
                        101, // EDGE_EXPAND
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "rmq_push_update_atomic",
                description = "Single RMQ push/update step",
                expectedCostCenterIds =
                    listOf(
                        102, // RMQ_PUSH_UPDATE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "rmq_query_atomic",
                description = "Single RMQ query step",
                expectedCostCenterIds =
                    listOf(
                        103, // RMQ_QUERY
                    ),
            ),
            // Key materialization
            CostCenterTraceVector.issue(
                name = "plan_key_issue_trace",
                description = "PlanKeyFactory materialization path before deterministic hash + remap",
                expectedCostCenterIds =
                    listOf(
                        200, // PLAN_CACHE_KEY_MATERIALIZE
                        201, // CANONICAL_SIGNATURE_MATERIALIZE
                    ),
            ),
            // L2 atomic / orchestration traces
            CostCenterTraceVector.issue(
                name = "l2_shard_route_atomic",
                description = "Single deterministic shard route step",
                expectedCostCenterIds =
                    listOf(
                        202, // L2_SHARD_ROUTE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_region_lookup_atomic",
                description = "Single partition-region lookup step",
                expectedCostCenterIds =
                    listOf(
                        203, // L2_REGION_LOOKUP
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_builder_gate_atomic",
                description = "Single in-flight acquire step for builder admission",
                expectedCostCenterIds =
                    listOf(
                        204, // L2_INFLIGHT_ACQUIRE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_bucket_scan_atomic",
                description = "Single exact-match bucket scan step",
                expectedCostCenterIds =
                    listOf(
                        205, // L2_BUCKET_SCAN
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_publish_put_if_absent_atomic",
                description = "Single publish / put-if-absent commit step",
                expectedCostCenterIds =
                    listOf(
                        206, // L2_PUBLISH_PUT_IF_ABSENT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_fault_transient_atomic",
                description = "Single transient-fault accounting step",
                expectedCostCenterIds =
                    listOf(
                        207, // L2_FAULT_TRANSIENT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_fault_circuit_open_atomic",
                description = "Single circuit-open-fault accounting step",
                expectedCostCenterIds =
                    listOf(
                        208, // L2_FAULT_CIRCUIT_OPEN
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_hit_atomic",
                description = "Single cache-hit accounting step",
                expectedCostCenterIds =
                    listOf(
                        209, // L2_HIT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_bypass_read_atomic",
                description = "Single bypass-read accounting step after session seal",
                expectedCostCenterIds =
                    listOf(
                        210, // L2_BYPASS_READ
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_pre_screen_get_atomic",
                description = "Single pre-screened routing-table get step",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_single_wait_tick",
                description = "Single bounded in-flight wait / poll step",
                expectedCostCenterIds =
                    listOf(
                        212, // L2_INFLIGHT_WAIT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_capacity_check_atomic",
                description = "Single capacity-governance checkpoint",
                expectedCostCenterIds =
                    listOf(
                        213, // L2_CAPACITY_CHECK
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_circuit_open_transition_atomic",
                description = "Single circuit-open state transition",
                expectedCostCenterIds =
                    listOf(
                        214, // L2_CIRCUIT_OPEN_TRANSITION
                    ),
            ),
            // L2 composed normative flows
            CostCenterTraceVector.issue(
                name = "l2_pre_screen_exact_hit",
                description = "Pre-screen get, exact bucket scan, cache hit return",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                        205, // L2_BUCKET_SCAN
                        209, // L2_HIT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_pre_screen_miss_builder_admission",
                description = "Pre-screen miss followed by in-flight acquire for builder winner",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                        204, // L2_INFLIGHT_ACQUIRE
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_join_single_poll_then_hit",
                description = "Joiner performs one bounded poll, then re-verifies committed winner and returns hit",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                        204, // L2_INFLIGHT_ACQUIRE
                        212, // L2_INFLIGHT_WAIT
                        205, // L2_BUCKET_SCAN
                        209, // L2_HIT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_join_two_polls_then_hit",
                description = "Joiner performs two bounded polls, then re-verifies committed winner and returns hit",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                        204, // L2_INFLIGHT_ACQUIRE
                        212, // L2_INFLIGHT_WAIT
                        212, // L2_INFLIGHT_WAIT
                        205, // L2_BUCKET_SCAN
                        209, // L2_HIT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_join_single_poll_then_transient_degrade",
                description = "Joiner performs one bounded poll, fails to obtain winner, degrades as transient fault",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                        204, // L2_INFLIGHT_ACQUIRE
                        212, // L2_INFLIGHT_WAIT
                        207, // L2_FAULT_TRANSIENT
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_circuit_open_fault_path",
                description = "Pre-screen attempt reaches builder/in-flight gate and returns circuit-open fault",
                expectedCostCenterIds =
                    listOf(
                        211, // L2_PRE_SCREEN_GET
                        204, // L2_INFLIGHT_ACQUIRE
                        208, // L2_FAULT_CIRCUIT_OPEN
                    ),
            ),
            CostCenterTraceVector.issue(
                name = "l2_publish_new_winner_path",
                description = "Builder publishes a brand-new canonical winner",
                expectedCostCenterIds =
                    listOf(
                        206, // L2_PUBLISH_PUT_IF_ABSENT
                    ),
            ),
        )

    private fun hexRepeat(
        byteHex: String,
        count: Int,
    ): String {
        val normalized = byteHex.lowercase()
        return buildString(normalized.length * count) {
            repeat(count) {
                append(normalized)
            }
        }
    }
}

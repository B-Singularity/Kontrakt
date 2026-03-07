# Design Note: L1 Planner Session Primitive Data Structures

Date: 2026-02-22  
Status: Active

## Overview

To meet predictable latency, O(Δdepth) Zero-Residue Rollback, and deterministic RMQ requirements, the `PlannerSession`
MUST utilize primitive, allocation-free data structures.

This document also defines the **hot-path identity indexing** requirement:
Kotlin/JVM unsigned value classes (`ULong`) are boxed when used as generic type arguments, therefore identity routing
MUST be implemented with **primitive maps** over raw `Long` bit patterns.

> **Clarification (AMENDED):** “allocation-free” here means **no per-operation heap allocation on the hot path**:
> no boxed `ULong`/`Long` keys via generics, no `HashMap` churn, no iterator allocations in the traversal loop.
> Preallocating arrays once and reusing via epoch reset is the preferred form. Recreating the indexer per request is
> allowed **only** if it does not create a burst of allocations proportional to graph size.

> **Policy Boundary Note (AMENDED):** This document fixes **layout and invariants** (SSOT-level requirements).
> Numeric budgets (bytes/steps/semantic units) are **policy defaults** and may become environment-aware later.
> The protocol MUST remain independent of environment introspection; any environment discovery MUST be via
> ports/adapters,
> and the selected policy values are injected into `PlannerSessionConfig`.

## Core Structures

### 1. Active Path (`ActiveStack`) & Membership (`GreyMap`)

* **`NodeIdIndexer` Reset Guarantee (CRITICAL MUST):** To prevent cross-request memory leaks, the `NodeIdIndexer` MUST
  support O(1) session resets via epoch-based array invalidation OR be safely recreated per request without hot-path
  allocation spikes.

* **`ActiveStack`:** `IntArray`.
* **`GreyMap`:** `depthOfNodeId: IntArray`. Guaranteed O(1) updates and removes.

> **Contract (AMENDED):**
> * `ActiveStack[depth]` stores **dense `nodeId: Int`** (output of `NodeIdIndexer`), not `identity64`.
> * `GreyMap` membership is defined over `nodeId` using `depthOfNodeId[nodeId] != 0`.
    >   This guarantees O(1) add/remove/contains and avoids boxed-key sets.

### 2. NodeIdIndexer (Primitive Two-Phase Indexer) — AMENDED

**Goal:** map abstract node identities to dense `nodeId: Int` without:

1) false cycles (identity collisions)
2) GC storms (boxed `ULong` keys in generic maps)

#### 2.1 Rules (Normative)

* **Phase 1:** route by `nodeIdentity64: ULong` **as raw bits stored in `LongArray`** (`identity64.toLong()`).
* **Phase 2:** verify by **Canonical Signature byte-equality**.
* **BANNED:** `HashMap`, `MutableMap`, or `Map<ULong, *>` inside `NodeIdIndexer` (boxing + allocation).
* **REQUIRED:** open addressing over primitive arrays + epoch/stamp for O(1) reset.

* **BANNED (AMENDED):** `Map<Long, *>` / `MutableMap<Long, *>` inside `NodeIdIndexer` hot path (boxed `Long` keys via
  generics).
  Use primitive arrays / primitive open addressing only.

#### 2.2 Minimal Data Layout (Recommended)

```text
// Phase-1 table (open addressing)
keysBits:   LongArray   // stored identity64 bits
heads:      IntArray    // head nodeId for this identity64 chain
stamps:     IntArray    // epoch-stamp: slot is "occupied in this epoch" iff stamps[i] == epoch

// Per-node storage (dense)
nextByNodeId: IntArray              // collision chain for same identity64
sigByNodeId:  Array<CanonicalSignature?> // only used when scanning candidates (phase-2)
```

*Why chain?* Multiple distinct signatures can (rarely) share the same 64-bit identity. We must store all candidates and
verify by signature bytes to prevent false cycles.

> **Signature Storage Note (AMENDED — Hot-Path Allocation Avoidance):**
> `sigByNodeId: Array<CanonicalSignature?>` is illustrative. A production SOTA layout SHOULD avoid per-node object
> allocation
> by storing signature bytes in a pooled slab:
>
> - `sigSlab: ByteArray`
> - `sigOffsets: IntArray`
> - `sigLengths: IntArray`
>
> Phase-2 equality then becomes byte-equality against the slab region.
> This preserves the normative rule (**absolute byte-equality**) while remaining allocation-free on the hot path.

#### 2.3 Kotlin Reference Shape (Illustrative)

```kotlin
internal class NodeIdIndexer(
    private val capacityPow2: Int,
    private val maxNodeIdCap: Int
) {
    private val mask = (1 shl capacityPow2) - 1
    private val keysBits = LongArray(1 shl capacityPow2)
    private val heads = IntArray(1 shl capacityPow2) { -1 }
    private val stamps = IntArray(1 shl capacityPow2) { 0 }

    private var epoch: Int = 1
    private var nextId: Int = 0

    // dense per-node
    private var nextByNodeId = IntArray(maxNodeIdCap) { -1 }
    private var sigByNodeId = arrayOfNulls<CanonicalSignature>(maxNodeIdCap)

    fun reset() {
        epoch++
        nextId = 0
        if (epoch == Int.MAX_VALUE) {
            java.util.Arrays.fill(stamps, 0)
            epoch = 1
        }
    }

    fun intern(identity64: ULong, signature: CanonicalSignature): Int {
        // AMENDED: avoid ULong as generic key — store raw Long bits
        val keyBits = identity64.toLong()

        // open addressing lookup
        var idx = mix64(keyBits) and mask
        while (true) {
            if (stamps[idx] != epoch) {
                // empty slot in this epoch -> insert key
                stamps[idx] = epoch
                keysBits[idx] = keyBits
                val id = allocateNode(signature)
                heads[idx] = id
                return id
            }

            if (keysBits[idx] == keyBits) {
                // scan chain and verify by signature bytes (Phase-2)
                var cur = heads[idx]
                while (cur != -1) {
                    if (sigByNodeId[cur]!!.bytesEquals(signature)) return cur
                    cur = nextByNodeId[cur]
                }
                // miss under same identity64 -> append
                val id = allocateNode(signature)
                nextByNodeId[id] = heads[idx]
                heads[idx] = id
                return id
            }

            idx = (idx + 1) and mask
        }
    }

    private fun allocateNode(signature: CanonicalSignature): Int {
        if (nextId >= maxNodeIdCap) throw CapacityExceededException("MaxNodeIdCap")
        val id = nextId++
        sigByNodeId[id] = signature
        nextByNodeId[id] = -1
        return id
    }

    private fun mix64(x: Long): Int {
        // deterministic mix (SSOT-able). Example: xorshift* style.
        var z = x
        z = (z xor (z ushr 33)) * -0xae502812aa7333L
        z = (z xor (z ushr 33)) * -0x3b314601e57a13adL
        z = z xor (z ushr 33)
        return z.toInt()
    }
}

internal fun CanonicalSignature.bytesEquals(other: CanonicalSignature): Boolean {
    // MUST be byte-equality over canonical bytes
    return this.bytes.contentEquals(other.bytes)
}
```

> **Note:** the exact `mix64` constants are illustrative. The only requirement is determinism.

### 3. Range Minimum Query (RMQ): Parallel 1D Doubling Tables

We find the minimum edge AND its index (`argmin`) utilizing `incomingEdgeRankAtDepth`.

* **Unsigned Comparison Rule & Single Source of Truth (CRITICAL MUST):** `edgeRank` is a 64-bit unsigned ordering key
  (`UInt64`). Because JVM lacks native primitive unsigned arrays, it is stored in a `LongArray`. ALL comparisons MUST
  use the following deterministic comparator:

```kotlin
// CRITICAL MUST: single source of truth for ordering
fun less(rankA: Long, idxA: Int, rankB: Long, idxB: Int): Boolean {
    val c = java.lang.Long.compareUnsigned(rankA, rankB)
    return c < 0 || (c == 0 && idxA < idxB)
}
```

* **Memory Lifecycle & Holistic Bytes Cap (CRITICAL MUST):** The `PlannerSession` state structures MUST be
  **worker-local pooled**. `MaxDepthCap` and `MaxNodeIdCap` MUST be explicitly reverse-calculated from a
  `MaxPlannerBytesPerWorker` limit. This calculation MUST explicitly sum the byte footprint of the parallel structures
  (`flatMinEdgeRankUp`, `flatArgminUp`, `incomingEdgeRankAtDepth`, `activeStack`, `depthOfNodeId`, `floorLog2`, and
  `NodeIdIndexer` internals).

* **Sentinel, Index Rules & Integer Guard (CRITICAL MUST):**
    * **Sentinel Reservation:** The `depth` domain is `[0..MaxDepthCap]`, where `0` is a sentinel and is NEVER queried.
      All pushes MUST start at `depth >= 1`.
    * **+INF Representation:** The sentinel `+INF` rank MUST be represented as the maximum unsigned value
      (`ULong.MAX_VALUE`), or equivalently `-1L` when stored in a `Long` array with unsigned comparison. The valid range
      for `edgeRank` is strictly `[0 .. ULong.MAX_VALUE - 1]`.
    * **Overflow Guard:** `MaxDepthCap` MUST be mathematically constrained to `<= Int.MAX_VALUE - 2` to absolutely
      guarantee that the virtual back-edge index (`currentDepth + 1`) is always representable as a positive 32-bit
      signed integer without overflow.

> **RMQ Initialization Law (AMENDED — Sentinel Safety, MUST):**
> RMQ tables MUST NOT use JVM default zeros as meaning-bearing values.
> The following sentinel initialization is mandatory before first use (and must be preserved under pooling):
>
> - `incomingEdgeRankAtDepth[*]` MUST initialize to `-1L` (`+INF` under unsigned compare).
> - `flatMinEdgeRankUp[*]` MUST initialize to `-1L` (`+INF`).
> - `flatArgminUp[*]` MUST initialize to `Int.MAX_VALUE` (`EMPTY`).
>
> Rationale: `0L` is a valid unsigned rank and would corrupt ordering if treated as sentinel.

* **Push DP Update & Mechanical `backEdge` Inclusion (CRITICAL MUST):**
    * **DP Population:** During the DP update phase, the `less` comparator MUST be used to evaluate the lexicographical
      tuple `(edgeRank, argmin)`.
    * **Empty Segment Guard:** RMQ queries are ONLY issued when `cycleStartIndex + 1 <= currentDepth`. Otherwise (e.g.,
      immediate self-loop), the segment is empty. An empty segment MUST bypass table lookup and return
      `(rank = -1L, argmin = Int.MAX_VALUE)`.
    * Query parallel arrays for `minStackEdgeRank` and `minStackArgmin` (if segment is not empty).
    * The `backEdge` is virtually assigned `stackIndex = currentDepth + 1`.
    * Evaluate `less(minStackEdgeRank, minStackArgmin, backEdgeRank, currentDepth + 1)`. Because `currentDepth + 1` is
      mathematically the largest index in the cycle, the stack edge will correctly win any true `edgeRank` tie.

### 4. Absolute Zero-Residue Unwinding (Success & Abort)

To prevent Heisenbugs in pooled worker arrays, the state MUST be cleanly unwound on **ANY** exit path (including Hard
Aborts) via a `finally` block.

```kotlin
fun resetToCleanState(currentDepth: Int) {
    // 1. O(depth) GreyMap cleanup strictly along the active stack
    for (i in currentDepth downTo 1) {
        depthOfNodeId[activeStack[i]] = 0
    }

    // 2. NodeIdIndexer Reset (e.g., epoch increment)
    nodeIdIndexer.reset()

    // 3. Telemetry emission (Non-throwing)
    try {
        telemetrySink.recordSessionEnd(/* payload */)
    } catch (e: Exception) { /* Best-effort ignore */
    }

    // 4. O(1) pointer reset
    this.currentDepth = 0 // Return to sentinel
}
```

---

## Amendment: Fuel/CostCenter Accounting Integration (CRITICAL MUST)

The `PlannerSession.step(costCenter)` MUST be invoked at every hot-path operation that can be used for CPU-DoS or
can differ between Hot/Cold cache states.

### Required CostCenters (minimum set)

**Traversal / Frames**

* `FRAME_DISPATCH`
* `EDGE_EXPAND`
* `RMQ_PUSH_UPDATE`
* `RMQ_QUERY`
* `CANONICAL_SIGNATURE_MATERIALIZE`
* `PLAN_CACHE_KEY_MATERIALIZE`

**NodeIdIndexer (Primitive Two-Phase Routing)**

* `NODEID_PHASE1_ROUTE`
* `NODEID_PROBE_STEP` (each open-addressing probe step)
* `NODEID_PHASE2_SCAN` (each candidate signature compare)
* `NODEID_ALLOCATE` (dense id allocation / chain append)
* `NODEID_RESET_EPOCH` (epoch wrap / stamp clear path)
* `NODEID_ROLLBACK_STEP` (each undo replay step during transactional
  rollback)  <!-- AMENDED: rollback metering is mandatory -->

> **Tick Naming Note (AMENDED):** In some documents the atomic probe unit is referred to as `NODEID_PROBE_TICK`.
> The required invariant is: **one `step()` per open-addressing probe slot inspection**.
> Whether the enum constant is named `*_TICK` or `*_STEP` is secondary; the tick semantics are normative.

**L2 (Partitioned Tier-2 Governance)**

* `L2_REGION_LOOKUP`
* `L2_SHARD_ROUTE`
* `L2_PRE_SCREEN_GET`
* `L2_INFLIGHT_ACQUIRE`
* `L2_INFLIGHT_WAIT` (each wait tick / bounded join attempt)
* `L2_BUCKET_SCAN` (two-phase equality scan)
* `L2_PUBLISH_PUT_IF_ABSENT`
* `L2_CAPACITY_CHECK`
* `L2_CIRCUIT_OPEN_TRANSITION`

**Bypass / Fault**

* `L2_BYPASS_READ`
* `L2_FAULT_TRANSIENT`
* `L2_FAULT_CIRCUIT_OPEN`

### Governance Rules

* `MaxFinalizeSteps` MUST be strictly monotonic and MUST NOT be rolled back during transaction unwinds.
* Join/wait operations MUST consume fuel, preventing indefinite waits.
* If the session enters L2 bypass mode (`CircuitOpen`), L2 cost centers MUST still be recorded (for telemetry and
  audits),
  but interning is skipped.

> **Budget Counter Symmetry Note (AMENDED):** If physical budgets are enforced per-session using a baseline, semantic
> budgets MUST follow the same per-session baseline rule unless a global cumulative semantic budget is explicitly
> specified as policy. Budget policy MUST be fixed in an ADR / execution strategy note to avoid silent asymmetry.

### Budget Defaults Are Policy, Not Protocol

The following values in `PlannerSessionConfig` are **policy defaults**:

- `maxPlannerBytes`
- `maxPhysicalSteps`
- `maxSemanticWorkUnits`

They are **NOT** protocol (SSOT) constants.
The protocol defines:

- the **unit of measurement** (CostCenter tick semantics),
- the **dual-track mapping** (PhysicalOnly vs SemanticAlso),
- and the **invariants** (monotonic counters, zero-residue reset, fail-closed behavior).

The numeric budgets are allowed to evolve based on:

- runtime environment constraints (heap size, worker count),
- product-level performance targets,
- and empirically validated workload characteristics.

Any change to policy defaults MUST NOT change deterministic semantic output, and MUST preserve cache-blind semantic
invariants.

> **MVP Policy Surface Note (AMENDED):** The MVP SHOULD remain “zero-config” for most users.
> If a high-level policy knob is exposed (e.g., `resourceProfile = AUTO | SMALL_HEAP | DEFAULT | SERVER`),
> it MUST map to these internal budgets without exposing low-level knobs unless production incidents require it.
# Design Note: L1 Planner Session Primitive Data Structures

Date: 2026-02-22
Status: Active

## Overview

To meet predictable latency, O(Δdepth) Zero-Residue Rollback, and deterministic RMQ requirements, the `PlannerSession`
MUST utilize primitive, allocation-free data structures.

## Core Structures

### 1. Active Path (`ActiveStack`) & Membership (`GreyMap`)

* **`NodeIdIndexer` Reset Guarantee (CRITICAL MUST):** To prevent cross-request memory leaks, the `NodeIdIndexer` MUST
  support O(1) session resets via epoch-based array invalidation OR be safely recreated per request without hot-path
  allocation spikes.
* **`ActiveStack`:** `IntArray`.
* **`GreyMap`:** `depthOfNodeId: IntArray`. Guaranteed O(1) updates and removes.

### 2. Range Minimum Query (RMQ): Parallel 1D Doubling Tables

We find the minimum edge AND its index (`argmin`) utilizing `incomingEdgeRankAtDepth`.

* **Unsigned Comparison Rule & Single Source of Truth (CRITICAL MUST):** `edgeRank` is a 64-bit unsigned ordering key (
  `UInt64`). Because JVM lacks native primitive unsigned arrays, it is stored in a `LongArray`. ALL comparisons MUST use
  the following deterministic comparator:

```kotlin
// CRITICAL MUST: single source of truth for ordering
fun less(rankA: Long, idxA: Int, rankB: Long, idxB: Int): Boolean {
    val c = java.lang.Long.compareUnsigned(rankA, rankB)
    return c < 0 || (c == 0 && idxA < idxB)
}
```

* **Memory Lifecycle & Holistic Bytes Cap (CRITICAL MUST):** The `PlannerSession` state structures MUST be *
  *worker-local pooled**. `MaxDepthCap` and `MaxNodeIdCap` MUST be explicitly reverse-calculated from a
  `MaxPlannerBytesPerWorker` limit. This calculation MUST explicitly sum the byte footprint of the parallel structures (
  `flatMinEdgeRankUp`, `flatArgminUp`, `incomingEdgeRankAtDepth`, `activeStack`, `depthOfNodeId`, `floorLog2`, and
  `NodeIdIndexer` internals).

* **Sentinel, Index Rules & Integer Guard (CRITICAL MUST):**
    * **Sentinel Reservation:** The `depth` domain is `[0..MaxDepthCap]`, where `0` is a sentinel and is NEVER queried.
      All pushes MUST start at `depth >= 1`.
    * **+INF Representation:** The sentinel `+INF` rank MUST be represented as the maximum unsigned value (
      `ULong.MAX_VALUE`), or equivalently `-1L` when stored in a `Long` array with unsigned comparison. The valid range
      for `edgeRank` is strictly `[0 .. ULong.MAX_VALUE - 1]`.
    * **Overflow Guard:** `MaxDepthCap` MUST be mathematically constrained to `<= Int.MAX_VALUE - 2` to absolutely
      guarantee that the virtual back-edge index (`currentDepth + 1`) is always representable as a positive 32-bit
      signed integer without overflow.

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

### 3. Absolute Zero-Residue Unwinding (Success & Abort)

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
# File: docs/design/l2-plan-interner-partitioned-tier2-with-governance.md

# Design Note: L2 Plan Interner (Partition → Shard → Bucket(2-Phase)) with Governance

Date: 2026-03-01  
Status: Draft (Normative shape for implementation; details may refine without changing invariants)  
Scope: `kontrakt-planning` adapter implementing `PlanInternRepository`  
References: Constitution Protocols (#3, #4, #8, #10), ADR-0031

> **AMENDED (ULong/Long Boxing Avoidance for Hot Routing):**
> Kotlin/JVM value classes (`ULong`) are boxed as type arguments, and `Long` is also boxed when used as a generic key
> (`Map<Long, *>`, `ConcurrentHashMap<Long, *>`).  
> Therefore, **hot-path routing by 64-bit keys** (`planKey64`, identity64-derived keys) MUST use *
*primitive/atomic-array
> tables** (open addressing) over raw `Long` bit patterns — not generic maps keyed by `Long`/`ULong`.

## 0. Goals & Non-Goals

### Goals

1. **Enterprise Survival:** prevent OOM by capacity governance; enable bulk reclamation (partition drop).
2. **Throughput:** reduce contention via sharding; eliminate GC storms via in-flight gating for hot keys.
3. **Correctness:** linearizable publication; exact-instance return; no dirty reads.
4. **Determinism:** semantics cache-blind. Gate ON/OFF, shard count changes MUST NOT alter topology or semantic budget.

### Non-Goals

* Global LRU eviction (not needed if partition bulk-drop is the primary reclamation unit).
* Perfect byte-accurate memory accounting (approximate bytes + hard entry caps are acceptable).

---

## 1. Data Model (Concrete Fields)

### 1.1 Top-Level

```text
regions: ConcurrentHashMap<PartitionKey, PartitionRegion>
```

**PartitionRegion fields**

* `val shards: Array<Shard>` (fixed size, power-of-two recommended)
* `val circuit: AtomicReference<CircuitState>` where `CircuitState ∈ {CLOSED, OPEN}`
* `val approxBytes: LongAdder` (optional)
* `val entryCount: LongAdder`
* `val closed: AtomicBoolean` (set true on bulk-drop)

> Note: `regions` is not a hot per-node structure; `PartitionKey` cardinality is expected to be small. Boxing here is
> not
> a primary risk surface compared to the per-request hot routing keyed by `planKey64`.

### 1.2 Shard

Each shard is a contention-reduction strip.

**Shard fields (AMENDED)**

* `val buckets: LongKeyTable<Bucket>`  *(primitive/atomic open addressing; see §1.2.1)*
* `val inflight: LongKeyTable<InFlight>` *(primitive/atomic open addressing + tombstones; see §1.2.1)*
* `val shardEntryCount: LongAdder` (optional)

#### 1.2.1 LongKeyTable<V> (Primitive / Atomic Routing Table) — AMENDED

**Purpose:** route `planKey64: Long` → `Bucket` / `InFlight` with:

* **no boxed keys** (`Long`/`ULong` in generics),
* high throughput under contention (striped writers, lock-free reads),
* optional **remove** support (needed for `inflight` cleanup),
* deterministic behavior (no per-run randomness).

**Normative properties**

* Keys are stored as raw `Long` bit patterns.
* Table uses **open addressing**.
* Table supports states: `EMPTY`, `OCCUPIED`, `TOMBSTONE`.
* `putIfAbsent` is linearizable w.r.t. itself under stripe lock (see below).
* Reads may be lock-free; correctness does NOT depend on read linearizability (worst-case: more misses → more work,
  still deterministic).

**Minimal layout (illustrative)**

```text
// capacity is power-of-two
keysBits:   AtomicLongArray       // 0L reserved as EMPTY sentinel (see §2.1 remap)
states:     AtomicIntegerArray    // 0=EMPTY, 1=OCCUPIED, 2=TOMBSTONE
values:     AtomicReferenceArray<V?> // V reference
size:       LongAdder

stripes:    Array<Any>            // writer striping locks (power-of-two)
resizeLock: Any                   // single resizer
```

**API shape (illustrative)**

* `fun get(keyBits: Long): V?`
* `fun putIfAbsent(keyBits: Long, value: V): V?`  *(returns existing if present; else null and installs)*
* `fun removeIfSame(keyBits: Long, value: V): Boolean` *(for inflight cleanup; uses tombstones)*
* `fun forEachValue(action: (V) -> Unit)` *(used only for partition drop wake-up; may be linear scan)*

> **Why allow lock-free reads?**  
> The algorithm already has deterministic fallbacks (miss → build → publish). A racy read can only increase duplicate
> work, not break correctness, because **exact match** is re-verified in the bucket lock, and publication is guarded.

### 1.3 Bucket (2-Phase Exact Match)

We avoid storing a giant `Map<PlanCacheKey, Node>` by:

* routing by `planKey64` (fast)
* verifying exact equality by full 7-tuple comparison (phase-2)

**Bucket fields**

* `val lock: Any` (monitor lock; only held for short scans/inserts)
* `var entries: ArrayList<Entry>` (small; collisions and co-residents limited)

**Entry fields**

* `val key: PlanCacheKey` (full 7-tuple; MUST include equalityKey bytes or equivalent)
* `val node: CanonicalPlanNode`
* `val approxBytes: Int` (optional; estimate for governance)

### 1.4 In-Flight Slot

**InFlight fields**

* `val future: CompletableFuture<CanonicalPlanNode>` (or equivalent)
* `val startedAtNanos: Long`
* `val waiters: LongAdder` (telemetry)

> Rationale: per-key joining avoids bucket-level locking during expensive builds.

---

## 2. Deterministic Routing Functions (SSOT)

### 2.1 planKey64 derivation

* MUST be deterministic and version-bound (depends on the 7-tuple including versions and partitionKey).
* MUST NOT use JVM identity hash or per-run randomness.

```text
planKey64 = Hash64(
  workAccountingVersion | normalizationVersion | edgeOrderingVersion |
  capabilityProfileVersion | entropyVersion | partitionKey | equalityKeyFingerprint
)
```

> **AMENDED (0L Sentinel Reservation for Primitive Tables):**
> `LongKeyTable` reserves `0L` as the EMPTY sentinel in `keysBits`. Therefore:
> * `planKey64 MUST NOT be 0L`.
> * If `Hash64(...) == 0L`, it MUST be deterministically remapped to a non-zero value using a version-bound function
    >   (e.g., `planKey64 = remapNonZero(planKey64, normalizationVersion)`).

### 2.2 Shard routing

```text
shardIndex = planKey64 & (shards.size - 1)
```

---

## 3. Operations (Algorithms + Atomicity + Step Accounting)

### 3.1 getOrIntern(session, planCacheKey, builder): CanonicalPlanNode

**Step 0: Governance pre-check**

1. `session.step(L2_REGION_LOOKUP)`
2. region = `regions.computeIfAbsent(partitionKey) { new PartitionRegion(...) }`
3. If `region.closed == true` OR `region.circuit == OPEN` => `session.step(L2_BYPASS_READ)` and return `builder()` (
   non-interned immutable)

**Step 1: Shard + pre-screen get (routing-table read + bucket scan)**

1. `session.step(L2_SHARD_ROUTE)`
2. shard = `region.shards[shardIndex]`
3. `session.step(L2_PRE_SCREEN_GET)`
4. bucket = `shard.buckets.get(planKey64)`  *(AMENDED: primitive table get; no boxed Long keys)*
5. if bucket exists, do short scan:
    * `session.step(L2_BUCKET_SCAN)`
    * `synchronized(bucket.lock) { find exact key match by full 7-tuple; if hit -> return node }`

**Step 2: In-flight acquisition (hot-key gate)**

1. `session.step(L2_INFLIGHT_ACQUIRE)`
2. existing = `shard.inflight.putIfAbsent(planKey64, new InFlight())` *(AMENDED: primitive table putIfAbsent)*
    * if `existing == null` => we won => builder thread
    * else => joiner, slot = existing

**Step 3A: Builder path**

1. `session.step(L2_CAPACITY_CHECK)`
2. if `region.entryCount >= maxEntries` (or bytes cap) => transition OPEN:
    * `session.step(L2_CIRCUIT_OPEN_TRANSITION)`
    * `region.circuit.set(OPEN)`
    * complete slot exceptionally or with bypass result (policy)
    * `shard.inflight.removeIfSame(planKey64, slot)` *(AMENDED: tombstone removal)*
    * return `builder()` (bypass; still immutable)
3. node = `builder()` (build outside locks)
4. publish:
    * `session.step(L2_PUBLISH_PUT_IF_ABSENT)`
    * bucket = `shard.buckets.putIfAbsent(planKey64, new Bucket()) ?: shard.buckets.get(planKey64)`  
      *(AMENDED: primitive table; tolerate benign duplicate Bucket allocations if racing)*
    * `synchronized(bucket.lock)`:
        - re-scan exact key; if found, winner = existing
        - else append entry; increment counters
5. `slot.future.complete(winner)`
6. `shard.inflight.removeIfSame(planKey64, slot)` (best-effort)
7. return winner

**Atomicity guarantee:** publication is linearizable at the bucket lock insertion point; waiters only observe after
future completion.

**Step 3B: Joiner path (bounded)**

1. Loop with bounded fuel:
    * `session.step(L2_INFLIGHT_WAIT)`
    * try `slot.future.getNow(null)`; if done return
    * else optionally `LockSupport.parkNanos(pollNanos)` (pollNanos may be 0..small)
    * if fuel low or wait exceeds threshold => degrade:
        - either bypass and `builder()` (no intern)
        - or fallback to pre-screened race (build + publish) depending on policy
2. If slot completes exceptionally => treat as `Fault(Transient)`:
    * `session.step(L2_FAULT_TRANSIENT)`
    * degrade to miss: attempt builder path without gate (or bypass if circuit is open)

**Blocking note:** joiner waiting is not a global lock; it is per-key joining. Degrade + fuel-bounding prevents
unbounded blocking.

> **AMENDED (No Boxed-Key Gate):**
> The in-flight gate MUST NOT be implemented with `ConcurrentHashMap<Long, InFlight>` or `Map<ULong, InFlight>` because
> those keys are boxed and create GC pressure under hot-key storms. The shard-local `LongKeyTable<InFlight>` exists
> specifically to keep hot routing allocation-free.

---

## 4. Fault Governance

### 4.1 Transient faults

* Examples: telemetry sink failure, sporadic map exceptions (rare), unexpected runtime exception during build.
* Policy: complete slot exceptionally, remove inflight, treat as miss (Domain sees cache miss).

### 4.2 CircuitOpen faults

* Trigger: capacity breach, region closed (bulk drop), or explicit admin policy.
* Policy: bypass L2 for session remainder (Domain-level). Persistent payload store is bypassed as well (ADR-0031).

---

## 5. Partition Bulk Drop (Enterprise Lifecycle)

### 5.1 API requirement (adapter-level)

* L2 adapter MUST expose an operation: `dropPartition(partitionKey)` (not visible to domain core if not desired).
* Implementation:
    1. region = `regions.remove(partitionKey)`
    2. if region != null:
        * `region.closed.set(true)`
        * for each shard: `shard.inflight.forEachValue { it.future.completeExceptionally(PartitionDropped(...)) }`  
          *(AMENDED: scan primitive inflight table; wake waiters)*
        * (optional) clear shard tables by dropping region reference (bulk GC)

### 5.2 Concurrency rule

* A drop MAY race with in-flight builds. Outcomes are allowed:
    * builder returns a valid immutable node but it is not interned (post-drop)
    * joiners awaken and rebuild/bypass
* Forbidden: joiners waiting forever; thus inflight futures MUST be completed on drop.

---

## 6. Telemetry (Required Signals)

* `duplicateBuildRatio = (buildersStarted - publishesWon) / buildersStarted`
* `hotKeyRate = inflightWaiters / l2Lookups`
* `circuitOpenCount`, `partitionDropCount`
* `avgJoinWaitNanos`, `maxJoinWaitNanos`

Telemetry is used to decide **AUTO gating**: default is Pre-Screened Race; enable gate if hotKey/dup ratio exceeds
thresholds.

* `avgTableProbeSteps`, `maxTableProbeSteps`
* `tableResizeCount`

---

## 7. Verification Checklist (CI / Stress)

1. Cold vs Hot cache: topology and `treeSemanticCostUpperBound` identical.
2. Gate OFF vs ON: same semantics; only operational counters differ.
3. Partition drop mid-flight: no deadlocks, no hangs, no dirty reads.
4. Linearizability: no partial nodes observable; publication happens-before completion.
5. Zero-residue: after hard abort, reused workers show clean session state (depth=0, empty greymap).

6. **Boxing Regression Guard (AMENDED):**
    * Bench / allocation tests MUST assert no `Long`/`ULong` key boxing allocations on the hot routing path:
      `shard.buckets.*`, `shard.inflight.*`, and `NodeIdIndexer` operations.
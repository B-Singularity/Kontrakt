# Design Note: L2 Plan Interner (Partition → Shard → Bucket(2-Phase)) with Governance

Date: 2026-03-01

Status: Accepted

Scope: `kontrakt-planning` adapter implementing `PlanInternRepository`  
References: Constitution Protocols (#3, #4, #8, #10), ADR-0031, ADR-0032

> **AMENDED (ULong/Long Boxing Avoidance for Hot Routing):**
> Kotlin/JVM value classes (`ULong`) are boxed as type arguments, and `Long` is also boxed when used as a generic key
> (`Map<Long, *>`, `ConcurrentHashMap<Long, *>`).  
> Therefore, **hot-path routing by 64-bit keys** (`planKey64`, identity64-derived keys) MUST use *
*primitive/atomic-array
> tables** (open addressing) over raw `Long` bit patterns — not generic maps keyed by `Long`/`ULong`.

> **AMENDED (2026-03-21):**
> The amendments below clarify attach terminal-signal completeness, post-insertion attach reconciliation,
> completion-continuation execution-path safety, speculative-builder reservation release,
> attach-rejection vs quota-exhaustion distinction, close-gate terminalization completeness,
> request-bounded restart semantics for joined waiters, adapter-owned completion-dispatch lifecycle,
> implementation-order dependency for waiter-state CAS before dispatch attachment,
> reserve/confirm/rollback governance accounting,
> and wall-clock separation from waiter-local join governance.
> These amendments do **not** introduce new semantic policy; they make explicit the implementation obligations already
> implied by this note, ADR-0031, ADR-0032, and the compiler-core protocols.

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
> not a primary risk surface compared to the per-request hot routing keyed by `planKey64`.

### 1.2 Shard

Each shard is a contention-reduction strip.

**Shard fields (AMENDED)**

* `val buckets: LongKeyTable<Bucket>`  *(primitive/atomic open addressing; see §1.2.1)*
* `val inflight: LongKeyTable<InFlightSlot>` *(primitive-key routing to object identity shells; see §1.2.1)*
* `val shardEntryCount: LongAdder` (optional)

#### 1.2.1 LongKeyTable<V> (Primitive / Atomic Routing Table) — AMENDED

**Purpose:** route `planKey64: Long` → `Bucket` / `InFlightSlot` with:

* **no boxed keys** (`Long`/`ULong` in generics),
* high throughput under contention (striped writers, lock-free reads),
* optional **remove** support (needed for `inflight` cleanup),
* deterministic behavior (no per-run randomness).

**Clarification (Object Identity Shells):**
The primitive table requirement applies to the **routing key surface**.
It does **not** require the lifecycle host value to be an index-slab cell.
For the initial compliant implementation, `LongKeyTable` MAY route directly to object identity shells such as
`InFlightSlot`, provided that lifecycle truth remains inside primitive authority fields owned by those objects.

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

**InFlightSlot fields**

* identity shell object per routed shared key
* `@Volatile var slotWord: Long`  
  shared-slot authority surface; MUST encode at least:
    - `SharedSlotState`
    - attached waiter count
    - implementation-defined hot flags
* `val startedAtNanos: Long`
* `@Volatile var successNode: CanonicalPlanNode?`
* `@Volatile var terminalFailure: Throwable?`
* `@Volatile var waiterHead: WaiterCell?`
* `@Volatile var builderHead: BuilderHandleCell?`
* `@Volatile var commitRightWord: Int`
* `@Volatile var leaseWord: Long`

**Normative rules**

* `InFlightSlot` MUST be an explicit lifecycle host, not a thin `future + counter` helper.
* waiter timeout / waiter cancellation MUST NOT change shared-slot terminal state.
* `SUCCESS` MUST correspond only to a publication that has already linearized at the bucket insertion point.
* `DROPPED` MUST be used for partition close / bulk-drop terminalization.
* attached waiter count MUST be part of the shared-slot authority surface rather than an external telemetry-only adder.
* the initial compliant implementation SHOULD use an object identity shell for `InFlightSlot`; this note does not
  require index-slab lifecycle hosts.

> Rationale: per-key joining still avoids bucket-level locking during expensive builds, but lifecycle ownership now
> resides in explicit slot/waiter/builder state rather than in `CompletableFuture` continuation state.

#### 1.4.1 Attach Terminal-Signal Completeness (AMENDED)

A waiter attachment is considered successful only if the implementation guarantees that the attachment will later
receive
exactly one terminal outcome:

* normal resume,
* exceptional completion caused by shared-slot terminalization,
* waiter timeout,
* or waiter cancellation.

No successfully attached waiter may remain in a state where no terminal signal is any longer reachable.

#### 1.4.2 Post-Insertion Attach Reconciliation (AMENDED)

After waiter-list insertion, the implementation MUST re-verify shared-slot state.

If the shared slot has already transitioned out of `PENDING` at that point, the implementation MUST either:

* immediately deliver the terminal signal to that attachment, or
* remove the attachment and reject the attach.

An implementation MUST NOT leave a post-insertion attachment in a state where terminalization is no longer reachable.

#### 1.4.3 Speculative-Builder Reservation Lifecycle (AMENDED)

If timeout/degrade handling promotes a waiter into a speculative builder under governance quota, that promotion MUST
acquire a speculative-builder reservation.

That reservation MUST be released exactly once when the speculative build attempt terminates, whether by:

* successful publish,
* abort,
* or unrecoverable fault.

The release of speculative-builder reservation MUST NOT depend on the shared slot’s own terminal transition.

#### 1.4.4 Slot-Owned Speculative Lease Enforcement (AMENDED)

Speculative-builder reservations are **slot-owned leases**, not handle-owned state.

Normative consequences:

* lease issuance MUST originate from the shared slot,
* normal speculative-builder completion MAY release the lease directly,
* but shared-slot terminalization (`SUCCESS`, `FAILED`, `DROPPED`) MUST also force-release any still-live speculative
  leases owned by that slot,
* therefore lease release correctness MUST remain sound even if the terminating path is `tryDrop(...)` during bulk
  partition close.

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
2. existing = `shard.inflight.putIfAbsent(planKey64, new InFlightSlot())` *(AMENDED: primitive-key routing to
   lifecycle-host object)*
    * if `existing == null` => we won => builder thread
    * else => joiner, slot = existing

**Step 3A: Builder path**

1. `session.step(L2_CAPACITY_CHECK)`
2. if `region.entryCount >= maxEntries` (or bytes cap) => transition OPEN:
    * `session.step(L2_CIRCUIT_OPEN_TRANSITION)`
    * `region.circuit.set(OPEN)`
    * terminalize the slot through lawful shared failure or bypass result visibility (policy)
    * `shard.inflight.removeIfSame(planKey64, slot)` *(AMENDED: tombstone removal)*
    * return `builder()` (bypass; still immutable)
3. node = `builder()` (build outside locks)
4. claim commit-right before authoritative publication
5. publish:
    * `session.step(L2_PUBLISH_PUT_IF_ABSENT)`
    * bucket = `shard.buckets.putIfAbsent(planKey64, new Bucket()) ?: shard.buckets.get(planKey64)`  
      *(AMENDED: primitive table; tolerate benign duplicate Bucket allocations if racing)*
    * `synchronized(bucket.lock)`:
        - re-scan exact key; if found, winner = existing
        - else append entry; increment counters
6. publish winner payload into the slot
7. execute release publication boundary
8. CAS `slot.slotWord` from `PENDING` to `SUCCESS`
9. mark terminal delivery pending if required and enqueue / schedule waiter delivery through the adapter-owned
   completion-dispatch path
10. release commit-right
11. `shard.inflight.removeIfSame(planKey64, slot)` (best-effort after terminal visibility)
12. return winner

**Atomicity guarantee:** publication is linearizable at the bucket lock insertion point; waiters observe only after
shared-slot terminal visibility becomes authoritative.

**Step 3B: Joiner path (attach-then-yield, bounded by monotonic deadline)**

1. Fast-path shared-state read:
    * `session.step(L2_INFLIGHT_ATTACH)`
    * read `slot.slotWord`
    * if shared state is already terminal, consume the terminal signal immediately and return / fail accordingly
2. If not terminal, attempt waiter attach subject to governance:
    * reject attach if `region.closed == true` or shared-slot state is already `DROPPED`
    * reject attach if the attached waiter count encoded in `slot.slotWord` is already `>= maxWaitersPerKey`
3. On successful attach:
    * a fresh `WaiterCell` is created for that attach episode
    * the waiter transitions to `ATTACHED`
    * the current worker thread MUST be released; waiting MUST proceed via completion continuation / callback rather
      than bounded `parkNanos` polling
    * the wait deadline is `slot.startedAtNanos + joinWaitTimeoutNanos`, interpreted as a **monotonic elapsed-time
      deadline**
    * after insertion, shared-slot state MUST be re-checked to satisfy post-insertion attach reconciliation
4. Completion path:
    * if shared terminal visibility is observed, the waiter transitions to `RESUMED`
    * if the shared terminal payload represents failure or drop, the waiter still converges through `RESUMED`
      with an exceptional outcome payload
5. Timeout path:
    * `session.step(L2_INFLIGHT_TIMEOUT)`
    * timeout transitions only the waiter to `TIMED_OUT`; it MUST NOT fail the shared slot
    * timeout MUST NOT automatically promote the waiter to builder
    * speculative builder promotion is allowed only if slot-owned speculative quota permits it
    * speculative builder promotion MUST record `session.step(L2_INFLIGHT_QUOTA_EXHAUST)` when quota is exhausted
    * if quota is exhausted, policy MUST choose one of:
        - bypass and `builder()` (no intern), or
        - fail-fast / degraded miss according to policy
6. Cancellation path:
    * waiter cancellation transitions only that waiter to `CANCELLED`
    * waiter cancellation MUST NOT cancel the shared slot

**Non-blocking note:** joiner waiting is not a global lock and MUST NOT monopolize a worker thread while attached.
Correctness still comes exclusively from bucket exact re-check plus publication-before-completion.

> **AMENDED (Identity Shell Clarification):**
> Primitive/atomic routing tables are mandatory for hot **key routing**.
> They do **not** imply that async lifecycle hosts such as `InFlightSlot`, `WaiterCell`, or builder handles
> must themselves be replaced by index-slabs in the initial implementation.
> For this note, the preferred initial model is:
> - primitive key routing,
> - object identity shells for async lifecycle hosts,
> - primitive authority words inside those identity shells,
> - no immediate pooling before grace completion.

> **AMENDED (No Boxed-Key Gate):**
> The in-flight gate MUST NOT be implemented with `ConcurrentHashMap<Long, InFlight>` or `Map<ULong, InFlight>` because
> those keys are boxed and create GC pressure under hot-key storms. The shard-local `LongKeyTable<InFlight>` exists
> specifically to keep hot routing allocation-free.

#### 3.1.1 Completion Continuation Execution-Path Safety (AMENDED)

Completion continuation execution on the builder publication path MUST NOT re-enter the L2 shard path.

Implementations MAY dispatch waiter continuations to a separate executor or equivalent completion queue to prevent lock
inversion or publication-path contamination.

This requirement does not alter publication-before-completion ordering; it only constrains how waiter continuations may
execute after terminalization becomes observable.

#### 3.1.2 Attach Rejection vs Quota Exhaustion Distinction (AMENDED)

Ordinary attach rejection and speculative-builder quota exhaustion are distinct events.

Examples of ordinary attach rejection include:

* region already closed,
* shared slot already terminalized,
* waiter cap reached.

`L2_INFLIGHT_QUOTA_EXHAUST` applies only to speculative-builder quota denial after timeout/degrade handling.
It MUST NOT be used as a generic label for all attach rejection paths.

#### 3.1.3 Request-Bounded Restart Rule for Joined Waiters (AMENDED)

If a joined waiter is resumed by **fresh-session restart** rather than by suspended-session continuation, restart
boundedness MUST be enforced by carrying forward **request-scoped physical-step budget**.

Normative consequences:

* this note does **not** introduce a retry-count policy surface,
* this note does **not** introduce a new retry fault kind,
* boundedness is achieved by reducing the remaining request-scoped physical budget carried into any restarted session,
* if the remaining request-scoped physical budget is exhausted, the system MUST terminate through the already-existing
  hard-abort / fail-closed budget path rather than through a new L2-specific retry fault.

#### 3.1.4 Explicit Boundary-Orchestrated Restart Rule (AMENDED)

This design does **not** rely on any central-dispatcher restart model.

If a joined waiter is resumed by fresh-session restart, that restart remains an explicit boundary/orchestrator decision
made by the caller that receives the joined/await signal.

This note therefore constrains L2 join semantics and terminalization guarantees, but does not define a hidden framework
that centrally replays planning sessions.

---

## 4. Fault Governance

### 4.1 Transient faults

* Examples: telemetry sink failure, sporadic map exceptions (rare), unexpected runtime exception during build.
* Policy: transition shared slot to `FAILED`, complete slot exceptionally, remove inflight, treat as miss (Domain sees
  cache miss).

### 4.2 CircuitOpen faults

* Trigger: capacity breach, region closed (bulk drop), or explicit admin policy.
* Policy: bypass L2 for session remainder (Domain-level). Persistent payload store is bypassed as well (ADR-0031).
* On partition close / drop, shared in-flight slots MUST transition to `DROPPED` before region reclamation.

---

## 5. Partition Bulk Drop (Enterprise Lifecycle)

### 5.1 API requirement (adapter-level)

* L2 adapter MUST expose an operation: `dropPartition(partitionKey)` (not visible to domain core if not desired).
* Implementation:
    1. region = `regions.remove(partitionKey)`
    2. if region != null:
        * `region.closed.compareAndSet(false, true)` MUST be the one-way close transition
        * after close is observed, no new inflight admission is permitted
        * for each shard: snapshot / scan `shard.inflight` and transition every visible slot to `DROPPED`
        * complete every visible slot exceptionally with `PartitionDropped(...)`
        * region reclamation / bulk GC MUST occur only after the wake-up sweep has run

### 5.2 Concurrency rule

* A drop MAY race with in-flight builds. Outcomes are allowed:
    * builder returns a valid immutable node but it is not interned (post-drop)
    * joiners awaken and rebuild/bypass
* Forbidden: joiners waiting forever; thus inflight futures MUST be completed on drop.

### 5.3 Attach / Drop Race Rule

* Waiter attach and partition drop MAY race.
* If `DROPPED` wins before attach linearizes, attach MUST fail immediately with dropped terminalization.
* If attach linearizes before `DROPPED`, the later drop sweep MUST wake that waiter exceptionally before region
  reclamation.
* No waiter that was successfully attached before reclamation may remain indefinitely pending.

### 5.4 Close-Gate Terminalization Completeness (AMENDED)

A close-gate publication MUST occur before final region reclamation.

The implementation MUST ensure that any in-flight slot visible after close publication is terminalized before final
region removal.

This MAY be achieved by:

* stable repeated sweep,
* post-insert close check with immediate drop,
* or an equivalent linearizable mechanism.

This requirement also applies to any slot created after close-gate publication but before final reclamation; such slots
MUST be terminalized before region removal completes.

---

## 6. Telemetry (Required Signals)

* `duplicateBuildRatio = (buildersStarted - publishesWon) / buildersStarted`
* `hotKeyRate = inflightWaiters / l2Lookups`
* `circuitOpenCount`, `partitionDropCount`
* `avgJoinWaitNanos`, `maxJoinWaitNanos`
* `joinTimeoutCount`
* `waiterAttachRejectedCount`
* `speculativeQuotaExhaustCount`
* `cancelledWaiterCount`

Telemetry is used to decide **AUTO gating**: default is Pre-Screened Race; enable gate if hotKey/dup ratio exceeds
thresholds.

* `avgTableProbeSteps`, `maxTableProbeSteps`
* `tableResizeCount`

#### 6.1 Telemetry Signal Distinction Rule (AMENDED)

Telemetry for ordinary waiter-attach rejection and telemetry for speculative-builder quota exhaustion MUST remain
distinct signals and MUST NOT be merged into a single counter or event kind.

---

## 7. Verification Checklist (CI / Stress)

1. Cold vs Hot cache: topology and `treeSemanticCostUpperBound` identical.
2. Gate OFF vs ON: same semantics; only operational counters differ.
3. Partition drop mid-flight: no deadlocks, no hangs, no dirty reads.
4. Linearizability: no partial nodes observable; publication happens-before completion.
5. Zero-residue: after hard abort, reused workers show clean session state (depth=0, empty greymap).
6. Async join observe-after-completion: no waiter observes a winner before completion.
7. Waiter timeout does not fail shared slot.
8. Cancelled waiter does not cancel shared slot.
9. Partition drop / attach race: every attached waiter is either resumed or exceptionally completed before reclamation.
10. Per-key speculative quota prevents unbounded duplicate-builder storms.

11. **Boxing Regression Guard (AMENDED):**
    * Bench / allocation tests MUST assert no `Long`/`ULong` key boxing allocations on the hot routing path:
      `shard.buckets.*`, `shard.inflight.*`, and `NodeIdIndexer` operations.

12. **Request-Bounded Restart Equivalence (AMENDED):**
    * If joined waiters resume through fresh-session restart, restart-boundedness MUST be enforced through
      carried-forward
      request-scoped physical budget, not through any new retry fault kind or retry-count policy surface.

13. **Speculative-Lease Release Correctness (AMENDED):**
    * Any speculative-builder reservation acquired from a slot MUST be released either by normal speculative-builder
      termination or by slot terminalization, including `DROPPED` caused by partition close.

14. **Completion/Timeout CAS Safety (AMENDED):**
    * Timeout-triggered waiter state transition and completion-triggered waiter state transition MUST race only through
      waiter-state CAS, with exactly one terminal waiter state winning.

15. **Wall-Clock Separation Compliance (AMENDED):**
    * Join waiter timeout, session elapsed watchdog, and protocol fuel accounting MUST remain distinct in implementation
      and MUST NOT be collapsed into one mixed timing/step budget.

---

## 8. Governance Budget Boundary & Bridge Alignment (AMENDED)

### 8.1 L2 Governance Budget vs L1 Structural Budget

L2 governance budgets and L1 planner-session structural budgets MUST remain distinct.

**L2 governance budgets** control repository survival and retention behavior, including:

- `maxEntries`
- `maxApproxBytes`
- partition retention / bulk-drop thresholds
- `CircuitOpen` transition thresholds

**L1 structural budgets** control worker-local planner structures, including:

- `MaxNodeIdCap`
- `MaxDepthCap`
- `indexerTableCap`
- `undoLogCap`
- `maxSignatureBytes`

Normative rule:

- L2 MUST NOT reinterpret `maxApproxBytes` as an L1 structural byte ledger.
- L2 MUST NOT directly expose internal planner sizing parameters to users/operators.
- L2 MAY influence whether interning is attempted, but MUST NOT redefine worker-local primitive layout on the hot path.

### 8.2 Bridge Alignment Rule

If an L2 lookup path causes a planner session to be created or reused, the session MUST be created from already-resolved
inputs:

- resolved numeric runtime budget,
- resolved planner-session caps,
- immutable version tuple,
- implementation-internal resolved calibration (if any).

L2 MUST treat those values as injected inputs.

L2 MUST NOT, on its own hot path:

- inspect runtime heap state,
- inspect container memory,
- recompute environment-derived planner budgets,
- dynamically resize planner-session primitive capacities.

### 8.3 Zero-Config Surface Preservation

The external execution surface SHOULD remain zero-config by default.

If a high-level policy surface is exposed, it SHOULD remain at the level of:

- `AUTO`
- `SMALL_HEAP`
- `DEFAULT`
- `SERVER`

L2 MUST NOT require the user to understand or provide internal budget-allocation parameters such as:

- signature reserve ratios,
- depth divisors,
- undo density,
- planner-internal structural split ratios.

Those remain internal policy/calibration concerns outside the public L2 contract.

---

## 9. Governance Estimate Law (`maxApproxBytes`) (AMENDED)

### 9.1 Meaning of `maxApproxBytes`

`maxApproxBytes` is a governance estimate used for repository pressure decisions.

It is:

- approximate,
- conservative,
- retention-oriented,
- allowed to slightly over- or under-estimate real repository payload size within implementation policy.

It is **NOT**:

- a semantic identity criterion,
- an exact planner-session structural byte ledger,
- an L1 `MaxPlannerBytesPerWorker` substitute,
- a correctness authority for canonical equality.

### 9.2 Allowed Consequences of Estimate Error

An estimate error MAY change:

- when a partition is dropped,
- when `CircuitOpen` is triggered,
- reuse rate,
- retention duration,
- memory survival characteristics.

An estimate error MUST NOT change:

- final topology,
- semantic budget outcome,
- exact identity verification result,
- canonical equality outcome.

### 9.3 Implementation Rule

Any use of `maxApproxBytes` MUST remain strictly inside governance logic:

- capacity checks,
- bulk-drop decisions,
- circuit transitions,
- telemetry.

It MUST NOT participate in exact-match verification.

### 9.4 Reserve / Confirm / Rollback Accounting Rule (AMENDED)

If approximate-byte accounting is performed during publication, the implementation MUST support the distinction between:

- reservation before publication attempt,
- confirmation on actual insertion,
- rollback when exact re-check returns an already-existing winner and no new insertion occurs.

This accounting distinction belongs to governance logic only and MUST NOT affect exact-match correctness.

---

## 10. Identity Hierarchy Clarification (AMENDED)

### 10.1 Routing Identity is Non-Authoritative

The following are routing identities:

- `planKey64`
- other identity64-derived routing keys used only for shard / bucket location

Routing identities are:

- deterministic,
- version-bound,
- collision-tolerant,
- non-authoritative.

A routing hit is only a pre-screen signal.

### 10.2 Semantic Identity is Authoritative

Authoritative identity remains the full semantic tuple carried by the bucket entry:

- full `PlanCacheKey`
- canonical signature bytes or equivalent exact identity material

Normative rule:

- a bucket candidate MUST be verified by exact full-tuple comparison before reuse.

### 10.3 Collision Is Not a Fault

Routing-key collision under the same bucket is a normal condition, not a repository fault.

Allowed behavior on collision:

- continue bucket scan,
- exact mismatch -> continue scan or miss,
- publish new entry if no exact match exists.

Forbidden interpretation:

- treating routing collision itself as corruption,
- treating routing collision as grounds for semantic failure.

Only storage/governance/state-transition failures may surface as repository faults.

---

## 11. Additional Compliance Checks (AMENDED)

The following checks are required in addition to the existing verification checklist.

### 11.1 Budget Delegation Boundary

`BudgetDelegationBoundaryTest`

Verifies:

- L2 never computes environment-derived planner budgets on the hot path,
- planner-session creation uses injected resolved values only,
- L1 structural sizing remains outside L2 governance logic.

### 11.2 Approx-Bytes Governance Separation

`ApproxBytesGovernanceOnlyTest`

Verifies:

- `maxApproxBytes` affects retention/governance only,
- it does not affect exact verification,
- it does not affect semantic output.

### 11.3 Routing vs Authority Separation

`RoutingVsSemanticIdentitySeparationTest`

Verifies:

- collisions under `planKey64` are handled by exact scan,
- routing collisions do not produce false hits,
- correctness is recovered exclusively by authoritative verification.

### 11.4 L1 / L2 Budget Separation

`L1L2BudgetSeparationTest`

Verifies:

- changing L2 governance caps does not mutate resolved L1 planner caps,
- changing L1 planner caps does not redefine L2 exact-match semantics,
- both layers remain linked only through injected runtime policy, not through hidden hot-path introspection.

### 11.5 Warm / Cold / Governance-Churn Equivalence

`GovernanceChurnSemanticEquivalenceTest`

Verifies semantic equivalence under:

- warm vs cold cache,
- partition drop timing differences,
- shard-count differences,
- gate `OFF` / `ON` / `AUTO`,
- `CircuitOpen` / bypass transitions.

Only operational metrics may differ.

### 11.6 Async Join Contract

`AsyncJoinObserveAfterCompletionTest`

Verifies:

- waiters only observe after completion,
- completion resumes attached waiters without worker-thread polling,
- exceptional completion is propagated deterministically.

### 11.7 Timeout / Slot Independence

`WaiterTimeoutDoesNotFailSharedSlotTest`

Verifies:

- waiter timeout does not change shared-slot terminal state,
- other attached waiters may still observe later success,
- timeout semantics remain non-semantic governance.

### 11.8 Cancel / Slot Independence

`CancelledWaiterDoesNotCancelSharedSlotTest`

Verifies:

- one waiter cancellation does not cancel the shared slot,
- remaining waiters still receive terminal completion,
- slot lifecycle remains builder-owned.

### 11.9 Drop / Attach Race Wake-up Completeness

`PartitionDropAttachRaceWakeupCompletenessTest`

Verifies:

- attach vs drop races are linearizable,
- attached waiters do not remain indefinitely pending,
- dropped terminalization completes before region reclamation.

### 11.10 Speculative Quota Guard

`PerKeySpeculativeQuotaPreventsStormTest`

Verifies:

- timeout/degrade paths do not permit unbounded speculative builders,
- quota exhaustion results in bypass or fail-fast only,
- semantic output remains unchanged.

### 11.11 Completion Executor Lifecycle (AMENDED)

`CompletionExecutorLifecycleTest`

Verifies:

- adapter-owned completion-dispatch resources are created once per adapter lifecycle,
- partition drop does not implicitly destroy adapter-owned completion-dispatch infrastructure,
- adapter shutdown does not discard pending completion work before corresponding slot terminalization is made visible.

### 11.12 Implementation Order Dependency (AMENDED)

`WaiterStateCasBeforeDispatchInfrastructureTest`

Verifies:

- waiter-state CAS semantics are established as the single source of truth before timeout-scheduler and
  completion-dispatch infrastructure are attached,
- therefore completion/timeouts cannot race outside the waiter-state machine.

---

## 12. Session-Fixed Governance Epoch Rule (AMENDED)

### 12.1 Stable Resolution Time Only

Adaptive governance resolution (including `AUTO` mode) MUST occur only at a **stable policy-resolution boundary**
outside the planner hot path, for example:

- process bootstrap,
- explicit policy refresh,
- worker-pool generation rollover,
- another equivalent runtime-boundary installation point.

The following are forbidden on the L2 hot path:

- reading environment state in order to recompute governance values,
- mutating `joinWaitTimeoutNanos` mid-session,
- mutating `maxWaitersPerKey` mid-session,
- mutating `maxSpeculativeBuildersPerKey` mid-session,
- mutating quota / fail-fast policy in response to live telemetry for the currently running session.

### 12.2 Session-Fixed Snapshot

Each planning session MUST observe a single fixed resolved governance snapshot for its entire lifetime.

Normative rule:

- a session starts with one resolved L2 governance snapshot,
- all join/wait/degrade decisions for that session MUST use that snapshot only,
- a newer governance snapshot may apply only to subsequently created sessions.

### 12.3 Consequence

If telemetry gathered during session `S` suggests that a better timeout/quota policy exists, the runtime MAY compute
a new governance snapshot, but that new snapshot MUST NOT alter the behavior of session `S`.
It may only affect later sessions.

### 12.4 Determinism Rationale

This rule prevents mid-session policy drift from changing:

- which waiter times out,
- whether speculative promotion becomes available,
- whether quota exhaustion is reached,
- whether bypass vs fail-fast is selected,

for an already-running session.

---

## 13. Adaptive Resolver Stability & Cold-Start Rules (AMENDED)

### 13.1 Stability Requirement

If `AUTO` mode or any adaptive policy resolver uses telemetry feedback, it MUST include a stability mechanism to avoid
epoch-to-epoch oscillation.

Allowed implementation techniques include:

- exponential moving average (EMA),
- hysteresis bands,
- minimum hold epochs,
- clamped update steps,
- equivalent smoothing/stability controls.

The exact formula is implementation policy, but the following anti-pattern is forbidden:

- one-threshold instantaneous policy flipping on every epoch.

### 13.2 Cold-Start Rule

If no usable historical telemetry exists, the resolver MUST choose deterministic conservative defaults.

Cold-start defaults SHOULD favor:

- non-aggressively short join timeouts,
- low speculative-builder quota,
- fail-fast or bypass-safe behavior when quota is exhausted,
- stable initial behavior over premature aggressiveness.

### 13.3 Telemetry Scope

Adaptive telemetry is an **input to the next governance snapshot**, not a signal that mutates the current one.

### 13.4 Semantic Boundary

Any adaptive policy change MUST preserve the semantic/cache-blind guarantees already stated in this note.
Adaptive control may change throughput, waiting, retention, or survival behavior only.

---

## 14. Telemetry Emission Boundary & Publication Safety (AMENDED)

### 14.1 Adapter-Internal Telemetry Sink

L2 telemetry emission MAY be implemented through an adapter-internal sink abstraction
(e.g., `L2TelemetrySink`) or equivalent adapter-local mechanism.

Normative rule:

- telemetry emission from L2 MUST NOT require the Domain Core to depend on telemetry storage infrastructure,
- telemetry emission failure MUST remain best-effort / non-throwing,
- telemetry payloads SHOULD remain numeric/event-oriented and MUST NOT retain planner object graphs.

### 14.2 Store / Sink Responsibility Split

An implementation MAY distinguish:

- a hot-path-facing adapter-local telemetry sink,
- and a slower policy/operations telemetry store used by the adaptive resolver.

If this split exists:

- the hot path records events into the sink,
- the resolver reads aggregated snapshots from the store,
- neither operation may mutate the already-installed governance snapshot of the current session.

### 14.3 Policy Registry Publication

If resolved governance snapshots are installed into a runtime registry for future sessions, that registry MUST provide
safe cross-thread publication.

Minimum acceptable implementations include:

- `@Volatile` snapshot reference, or
- `AtomicReference<PolicyEpoch>` (preferred when monotonic installation checks are needed).

If epoch identifiers are used, they MUST increase monotonically.

### 14.4 One-Way Installation Rule

Installing a newer policy snapshot MUST NOT silently roll back to an older snapshot.
If concurrent installation is possible, the runtime SHOULD enforce monotonic epoch progression.

### 14.5 Adapter-Owned Completion Dispatch Lifecycle (AMENDED)

If a completion mailbox / completion executor / timeout scheduler is used to implement non-blocking joined-waiter
delivery, that infrastructure MUST be adapter-owned rather than slot-owned or partition-owned.

Normative consequences:

- completion-dispatch resources are created and destroyed at adapter lifecycle boundaries,
- partition drop MUST NOT implicitly destroy adapter-owned completion-dispatch infrastructure,
- adapter shutdown MUST first prevent new join admission, then force slot terminalization visibility, then permit
  bounded completion drain before final shutdown,
- abrupt executor termination that discards pending completion work before corresponding terminalization has been made
  visible is forbidden.

---

## 15. Wall-Clock Separation Clarification (AMENDED)

### 15.1 Join Wait Timeout Is Not Protocol Fuel

`joinWaitTimeoutNanos` is a waiter-local governance deadline.

It is:

- monotonic elapsed-time based,
- non-semantic,
- local to waiter lifecycle,
- independent of `step(costCenter)` protocol accounting.

Normative rule:

- `joinWaitTimeoutNanos` MUST NOT be reinterpreted as protocol fuel,
- `step(costCenter)` MUST NOT be reinterpreted as elapsed-time allowance.

### 15.2 Session Elapsed Watchdog Is a Separate Boundary Concern

If the runtime introduces a session-level elapsed watchdog such as `maxSessionElapsedNanos`, that watchdog belongs to
runtime/session orchestration rather than to L2 waiter governance.

It therefore MUST NOT be folded into:

- `joinWaitTimeoutNanos`,
- speculative-builder quota semantics,
- `CircuitOpen` exact-match behavior,
- bucket exact re-check correctness,
- restart boundedness for joined waiters.

### 15.3 Restart Boundedness Must Not Depend on Wall Clock

If joined waiters resume through fresh-session restart, restart boundedness MUST be enforced through carried-forward
request-scoped physical-step budget rather than through elapsed wall-clock thresholds.

Elapsed wall-clock limits MAY still abort execution at the runtime boundary, but they MUST NOT become the semantic or
governance authority for L2 retry/restart behavior.

### 15.4 Timeout Semantics Separation

The following three ledgers are distinct and MUST remain distinct:

- `step(costCenter)` = protocol fuel / structural work accounting
- `joinWaitTimeoutNanos` = waiter-local L2 governance deadline
- `maxSessionElapsedNanos` (or equivalent) = runtime/session boundary watchdog

Implementations MUST NOT collapse these into one mixed timing/budget model.

```kotlin
// Illustrative separation only.
class ResolvedJoinGovernance private constructor(
    val joinWaitTimeoutNanos: Long,
    val maxWaitersPerKey: Int,
    val maxSpeculativeBuildersPerKey: Int,
    val failFastOnQuotaExhaustion: Boolean,
)

class ResolvedWallClockPolicy private constructor(
    val maxSessionElapsedNanos: Long,
)
```

---